package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.api.callGeminiCoach
import com.example.data.repository.BePresentRepository
import com.example.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class BePresentViewModel(private val repository: BePresentRepository) : ViewModel() {

    // --- SCREEN TIME STATES ---
    val allLogs: StateFlow<List<AppUsageLog>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDriftEvents: StateFlow<List<FocusDriftEvent>> = repository.allDriftEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .map { it ?: UserProfile() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    // Today's total screen time in minutes calculated dynamically
    val todayScreenTimeMinutes: StateFlow<Long> = allLogs.map { logs ->
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(Date())
        logs.filter { it.dateString == todayStr }.sumOf { it.timeSpentMinutes }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    // --- POMODORO TIMER STATES ---
    enum class TimerMode { FOCUS, SHORT_BREAK, LONG_BREAK }
    enum class TimerState { IDLE, RUNNING, PAUSED }

    private val _timerMode = MutableStateFlow(TimerMode.FOCUS)
    val timerMode: StateFlow<TimerMode> = _timerMode.asStateFlow()

    private val _timerState = MutableStateFlow(TimerState.IDLE)
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private val _timeLeftSeconds = MutableStateFlow(25 * 60)
    val timeLeftSeconds: StateFlow<Int> = _timeLeftSeconds.asStateFlow()

    private var timerJob: Job? = null

    // Blocklist simulation state
    private val _blockedApps = MutableStateFlow(listOf("TikTok", "Instagram", "Clash Royale"))
    val blockedApps: StateFlow<List<String>> = _blockedApps.asStateFlow()

    private val _selectedBlockedApps = MutableStateFlow(setOf("TikTok", "Instagram"))
    val selectedBlockedApps: StateFlow<Set<String>> = _selectedBlockedApps.asStateFlow()

    // Fullscreen unskippable interstitial block overlay
    private val _triggerDeepFocusOverlay = MutableStateFlow<String?>(null)
    val triggerDeepFocusOverlay: StateFlow<String?> = _triggerDeepFocusOverlay.asStateFlow()

    // --- REWARDS STORE STATES ---
    val allRewards: StateFlow<List<UserReward>> = repository.allRewards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _storeStatusMessage = MutableStateFlow<String?>(null)
    val storeStatusMessage: StateFlow<String?> = _storeStatusMessage.asStateFlow()

    // Active visual customization theme (unlocked by users)
    private val _currentAppTheme = MutableStateFlow("Default")
    val currentAppTheme: StateFlow<String> = _currentAppTheme.asStateFlow()

    // --- AI COACH STATES ---
    private val _coachAdvice = MutableStateFlow(
        "👋 Welcome back! I am your AI Digital Coach. Let me evaluate your habits.\n\n" +
        "⚡ Click the 'Analyze Screen Time' button below, and I will parse your core Room logs to detect focus drift loops, study windows, and provide hyper-personalized recommendations!"
    )
    val coachAdvice: StateFlow<String> = _coachAdvice.asStateFlow()

    private val _isCoachLoading = MutableStateFlow(false)
    val isCoachLoading: StateFlow<Boolean> = _isCoachLoading.asStateFlow()

    // --- INIT ---
    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.seedDemoDataIfEmpty()
        }
    }

    // --- TIMER FUNCTIONS ---
    fun setTimerMode(mode: TimerMode) {
        _timerMode.value = mode
        _timerState.value = TimerState.IDLE
        timerJob?.cancel()
        _timeLeftSeconds.value = when (mode) {
            TimerMode.FOCUS -> 25 * 60
            TimerMode.SHORT_BREAK -> 5 * 60
            TimerMode.LONG_BREAK -> 15 * 60
        }
    }

    fun startTimer() {
        if (_timerState.value == TimerState.RUNNING) return
        _timerState.value = TimerState.RUNNING
        timerJob = viewModelScope.launch {
            while (_timeLeftSeconds.value > 0) {
                delay(1000)
                _timeLeftSeconds.value -= 1
            }
            onTimerComplete()
        }
    }

    fun pauseTimer() {
        if (_timerState.value != TimerState.RUNNING) return
        _timerState.value = TimerState.PAUSED
        timerJob?.cancel()
    }

    fun resetTimer() {
        _timerState.value = TimerState.IDLE
        timerJob?.cancel()
        setTimerMode(_timerMode.value)
    }

    private fun onTimerComplete() {
        _timerState.value = TimerState.IDLE
        viewModelScope.launch(Dispatchers.IO) {
            val profile = repository.getProfileDirect()
            if (_timerMode.value == TimerMode.FOCUS) {
                // Earn points & XP!
                val pointsAdded = 40
                val updatedProfile = profile.copy(
                    totalPoints = profile.totalPoints + pointsAdded,
                    currentStreak = profile.currentStreak + 1,
                    longestStreak = maxOf(profile.currentStreak + 1, profile.longestStreak)
                )
                repository.updateProfile(updatedProfile)

                // Save completed focus session
                repository.addSession(
                    FocusSession(
                        startTime = System.currentTimeMillis(),
                        durationMinutes = 25,
                        category = "Study Session",
                        isCompleted = true,
                        pointsEarned = pointsAdded
                    )
                )
                _storeStatusMessage.value = "🎉 Great job! Focus timer completed. Earned +40 Points and leveled up streak!"
            } else {
                _storeStatusMessage.value = "☕ Break finished. Ready to focus again?"
            }
            setTimerMode(TimerMode.FOCUS)
        }
    }

    // Blocklist simulation toggles
    fun toggleAppBlock(appName: String) {
        val current = _selectedBlockedApps.value.toMutableSet()
        if (current.contains(appName)) {
            current.remove(appName)
        } else {
            current.add(appName)
        }
        _selectedBlockedApps.value = current
    }

    // Simulate Launching an App—triggers focus intercept
    fun simulateAppLaunch(appName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_timerState.value == TimerState.RUNNING && _timerMode.value == TimerMode.FOCUS && _selectedBlockedApps.value.contains(appName)) {
                // Deep Focus active and app is blocked: intercept with fullscreen unskippable overlay!
                _triggerDeepFocusOverlay.value = appName

                // Log a focus drift behavior incident!
                repository.addDriftEvent(
                    FocusDriftEvent(
                        timestamp = System.currentTimeMillis(),
                        closedApp = "BePresent Focus Window",
                        openedApp = appName,
                        timeDifferenceSeconds = 3 // Fast opened!
                    )
                )

                // Add simulated usage log so charts dynamically grow!
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val todayStr = sdf.format(Date())
                repository.addLog(
                    AppUsageLog(
                        appName = appName,
                        category = "Social",
                        timeSpentMinutes = 15,
                        timestamp = System.currentTimeMillis(),
                        dateString = todayStr
                    )
                )
            } else {
                // Standard app launch simulated outside focus—just logs usage minutes
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val todayStr = sdf.format(Date())
                repository.addLog(
                    AppUsageLog(
                        appName = appName,
                        category = if (appName == "Instagram" || appName == "TikTok") "Social" else "Gaming",
                        timeSpentMinutes = 10,
                        timestamp = System.currentTimeMillis(),
                        dateString = todayStr
                    )
                )
                _storeStatusMessage.value = "📱 Simulated launching $appName. Screen time updated."
            }
        }
    }

    fun dismissDeepFocusOverlay() {
        _triggerDeepFocusOverlay.value = null
    }

    // --- REWARDS STORE FUNCTIONS ---
    fun redeemReward(reward: UserReward) {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = repository.getProfileDirect()
            if (profile.totalPoints < reward.costPoints) {
                _storeStatusMessage.value = "❌ Insufficient points! You need ${reward.costPoints} points."
                return@launch
            }

            // Deduct Points
            val updatedProfile = profile.copy(totalPoints = profile.totalPoints - reward.costPoints)
            repository.updateProfile(updatedProfile)

            // Unlock item
            repository.updateReward(reward.copy(isUnlocked = true))

            _storeStatusMessage.value = "✅ Unlocked: ${reward.title}!"

            // Equip themes automatically
            if (reward.category == "Theme") {
                _currentAppTheme.value = reward.id
            } else if (reward.category == "Armor") {
                // Streak shield added
                val currentProfile = repository.getProfileDirect()
                repository.updateProfile(currentProfile.copy(streakArmorCount = currentProfile.streakArmorCount + 1))
            }
        }
    }

    fun buyStreakArmor() {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = repository.getProfileDirect()
            if (profile.totalPoints < 80) {
                _storeStatusMessage.value = "❌ Need 80 points to buy Streak Shield Armor!"
                return@launch
            }
            val updatedProfile = profile.copy(
                totalPoints = profile.totalPoints - 80,
                streakArmorCount = profile.streakArmorCount + 1
            )
            repository.updateProfile(updatedProfile)
            _storeStatusMessage.value = "🛡️ Purchased Streak Shield Armor! Points deducted (-80)."
        }
    }

    fun healBrokenStreak() {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = repository.getProfileDirect()
            if (profile.streakArmorCount <= 0) {
                _storeStatusMessage.value = "❌ No Streak Shield Armor available! Purchase one first inside the rewards tab."
                return@launch
            }
            // Heal! Say, return current streak to its longest or pre-reset levels
            val healedStreak = if (profile.currentStreak == 0) 3 else profile.currentStreak + 2
            val updatedProfile = profile.copy(
                streakArmorCount = profile.streakArmorCount - 1,
                currentStreak = healedStreak,
                longestStreak = maxOf(healedStreak, profile.longestStreak)
            )
            repository.updateProfile(updatedProfile)
            _storeStatusMessage.value = "❤️ Streak Healed! Spent 1 Streak Shield Armor. Defdemoralized!"
        }
    }

    fun resetStoreMessage() {
        _storeStatusMessage.value = null
    }

    // --- COOPERATIVE DYNAMIC AI COACH ---
    fun generateCoachAnalysis() {
        viewModelScope.launch(Dispatchers.IO) {
            _isCoachLoading.value = true
            _coachAdvice.value = "🧠 Evaluating logs, analyzing productivity patterns, and consulting neural coaches..."

            val profileState = userProfile.value
            val totalTodayScreenTime = todayScreenTimeMinutes.value
            val currentLogs = allLogs.value
            val driftEvents = allDriftEvents.value

            val logsSummary = currentLogs.groupBy { it.category }
                .map { (cat, items) -> "- $cat: ${items.sumOf { it.timeSpentMinutes }} minutes (${items.joinToString { "${it.appName} (${it.timeSpentMinutes}m)" }})" }
                .joinToString("\n")

            val driftSummary = if (driftEvents.isEmpty()) {
                "No focus drift loops detected! Amazing focus discipline."
            } else {
                driftEvents.joinToString("\n") { "- Intercepted attempt to launch ${it.openedApp} shortly after working in ${it.closedApp}." }
            }

            val systemPrompt = "You are a witty, extremely assertive, and playful Digital Habit-Formation & Screen Time Coach for BePresent. " +
                    "Your style is inspired by modern biohackers and productivity mentors: caring, highly intelligent, slightly sarcastic, and extremely practical. " +
                    "Analyze the user's data and structure your report with clear sections: HABIT DIAGNOSTICS, DRIFT LOOP WARN, and COACH'S PRESCRIPTION. Avoid robotic, corporate talk."

            val prompt = """
                Here is the user's current Screen Time statistics from our Room database:
                - Profile Points: ${profileState.totalPoints} XP
                - Current Streak: ${profileState.currentStreak} Days
                - Streak Shield Armors Available: ${profileState.streakArmorCount} 
                - Today's Total Screen Time: $totalTodayScreenTime minutes (Limit is ${profileState.dailyScreenTimeLimitMinutes} minutes)
                
                Category breakdown of logs in Database:
                $logsSummary
                
                Focus Drift Incidents (opening distracting apps within seconds of study alerts):
                $driftSummary
                
                Provide a personalized, hyper-practical weekly habit report. 
                Keep it concise (around 3 brief sections).
                If they have focus drifts, roast their drift habits playfully but offer a solid prescription. Introduce a specific study window recommendations.
            """.trimIndent()

            val advice = callGeminiCoach(prompt, systemPrompt)
            _coachAdvice.value = advice
            _isCoachLoading.value = false
        }
    }
}

class BePresentViewModelFactory(private val repository: BePresentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BePresentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BePresentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
