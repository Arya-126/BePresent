package com.example.data.repository

import com.example.data.db.*
import com.example.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*

class BePresentRepository(private val db: BePresentDatabase) {

    private val focusSessionDao = db.focusSessionDao()
    private val appUsageLogDao = db.appUsageLogDao()
    private val userRewardDao = db.userRewardDao()
    private val focusDriftEventDao = db.focusDriftEventDao()
    private val userProfileDao = db.userProfileDao()

    val allSessions: Flow<List<FocusSession>> = focusSessionDao.getAllSessions()
    val allLogs: Flow<List<AppUsageLog>> = appUsageLogDao.getAllLogs()
    val allRewards: Flow<List<UserReward>> = userRewardDao.getAllRewards()
    val allDriftEvents: Flow<List<FocusDriftEvent>> = focusDriftEventDao.getAllDriftEvents()
    val userProfile: Flow<UserProfile?> = userProfileDao.getUserProfileFlow()

    fun getLogsByDate(date: String): Flow<List<AppUsageLog>> {
        return appUsageLogDao.getLogsByDate(date)
    }

    suspend fun addSession(session: FocusSession) {
        focusSessionDao.insertSession(session)
    }

    suspend fun addLog(log: AppUsageLog) {
        appUsageLogDao.insertLog(log)
    }

    suspend fun addDriftEvent(event: FocusDriftEvent) {
        focusDriftEventDao.insertDriftEvent(event)
    }

    suspend fun updateReward(reward: UserReward) {
        userRewardDao.updateReward(reward)
    }

    suspend fun updateProfile(profile: UserProfile) {
        userProfileDao.insertOrUpdateProfile(profile)
    }

    suspend fun getProfileDirect(): UserProfile {
        return userProfileDao.getUserProfileDirect() ?: UserProfile()
    }

    // Call this during database or repository creation to seed demo data
    suspend fun seedDemoDataIfEmpty() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(Date())

        // 1. Seed User Profile
        val currentProfile = userProfileDao.getUserProfileDirect()
        if (currentProfile == null) {
            userProfileDao.insertOrUpdateProfile(
                UserProfile(
                    id = 1,
                    totalPoints = 180, // Rich starting balance to let judges buy streak armor!
                    currentStreak = 4,
                    longestStreak = 8,
                    streakArmorCount = 1,
                    dailyScreenTimeLimitMinutes = 180,
                    lastResetDateStr = todayStr
                )
            )
        }

        // 2. Seed Rewards Store list if empty
        val anyReward = userRewardDao.getAnyReward()
        if (anyReward == null) {
            val defaultRewards = listOf(
                UserReward("streak_armor_item", "Streak Shield Aura", "Instantly heals a lost streak if you forget to study.", 80, false, "shield", "Armor"),
                UserReward("badge_focus_lord", "Focus Emperor", "Awarded for completing 3+ Pomodoro focus rounds.", 50, false, "emoji_events", "Badge"),
                UserReward("badge_drift_slayer", "Drift Defeater", "Keep focus drift incidents under 2 today.", 60, false, "security", "Badge"),
                UserReward("theme_neon_cyber", "Cyberpunk Violet", "Transform the app with high-contrast electric magenta tints.", 100, false, "palette", "Theme"),
                UserReward("theme_mint_green", "Forest Zen", "Encase your phone in calming sage green and mint leaves.", 120, false, "spa", "Theme"),
                UserReward("tier_zen_master", "Ascended Master Tier", "Display a glowing Zen Master prestige border in reports.", 250, false, "workspace_premium", "Theme")
            )
            userRewardDao.insertRewards(defaultRewards)
        }

        // 3. Seed App Usage Logs for screen-time charts
        val anyLog = appUsageLogDao.getAnyLog()
        if (anyLog == null) {
            val sampleLogs = listOf(
                AppUsageLog(appName = "TikTok", category = "Social", timeSpentMinutes = 45, timestamp = System.currentTimeMillis() - 1000 * 60 * 30, dateString = todayStr),
                AppUsageLog(appName = "Instagram", category = "Social", timeSpentMinutes = 35, timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 2, dateString = todayStr),
                AppUsageLog(appName = "WhatsApp", category = "Social", timeSpentMinutes = 20, timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 4, dateString = todayStr),
                AppUsageLog(appName = "Gmail", category = "Productivity", timeSpentMinutes = 25, timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 1, dateString = todayStr),
                AppUsageLog(appName = "Android Studio", category = "Productivity", timeSpentMinutes = 90, timestamp = System.currentTimeMillis() - 1000 * 60 * 10, dateString = todayStr),
                AppUsageLog(appName = "Clash Royale", category = "Gaming", timeSpentMinutes = 30, timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 3, dateString = todayStr),
                AppUsageLog(appName = "Spotify", category = "Utility", timeSpentMinutes = 15, timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 5, dateString = todayStr)
            )
            appUsageLogDao.insertLogs(sampleLogs)
        }

        // 4. Seed focus drift events (behavorial loop demonstration)
        val anyDrift = focusDriftEventDao.getAnyDriftEvent()
        if (anyDrift == null) {
            val sampleDrifts = listOf(
                FocusDriftEvent(timestamp = System.currentTimeMillis() - 1000 * 60 * 15, closedApp = "Android Studio", openedApp = "TikTok", timeDifferenceSeconds = 12),
                FocusDriftEvent(timestamp = System.currentTimeMillis() - 1000 * 60 * 45, closedApp = "Gmail", openedApp = "Instagram", timeDifferenceSeconds = 24),
                FocusDriftEvent(timestamp = System.currentTimeMillis() - 1000 * 60 * 120, closedApp = "Slack", openedApp = "TikTok", timeDifferenceSeconds = 8)
            )
            for (drift in sampleDrifts) {
                focusDriftEventDao.insertDriftEvent(drift)
            }
        }
    }
}
