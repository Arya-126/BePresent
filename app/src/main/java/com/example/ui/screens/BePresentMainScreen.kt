package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.BePresentViewModel
import com.example.ui.viewmodel.BePresentViewModel.TimerMode
import com.example.ui.viewmodel.BePresentViewModel.TimerState
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

@Composable
fun BePresentMainScreen(
    viewModel: BePresentViewModel,
    modifier: Modifier = Modifier
) {
    val activeTheme by viewModel.currentAppTheme.collectAsStateWithLifecycle()
    
    // Dynamically shift color tokens depending on user's purchased theme customization!
    val primaryColor = when (activeTheme) {
        "theme_neon_cyber" -> Color(0xFFD946EF) // Cyber Neo Magenta
        "theme_mint_green" -> Color(0xFF2DD4BF) // Calm Teal
        else -> PremiumEmerald
    }
    
    val secondaryColor = when (activeTheme) {
        "theme_neon_cyber" -> Color(0xFF8B5CF6) // Royal Violet
        "theme_mint_green" -> Color(0xFF34D399) // Clean Sage
        else -> AccentMint
    }

    val currentTab = remember { mutableStateOf(0) }
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val statusMessage by viewModel.storeStatusMessage.collectAsStateWithLifecycle()
    val showOverlayByApp by viewModel.triggerDeepFocusOverlay.collectAsStateWithLifecycle()

    // Status toast controller
    LaunchedEffect(statusMessage) {
        if (statusMessage != null) {
            delay(3500)
            viewModel.resetStoreMessage()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("main_screen"),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            WellnessTopBar(
                userProfile = profile,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                onHealStreak = { viewModel.healBrokenStreak() }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SlateCard,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("app_navigation_bar")
            ) {
                val tabs = listOf(
                    Triple("Stats", Icons.Default.Analytics, 0),
                    Triple("Focus", Icons.Default.Timer, 1),
                    Triple("Store", Icons.Default.Star, 2),
                    Triple("Coach", Icons.Default.Psychology, 3)
                )

                tabs.forEach { (label, icon, index) ->
                    val isSelected = currentTab.value == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab.value = index },
                        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected) primaryColor else SoftGray
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = SlateCard.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.testTag("tab_item_$label")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen content transition
            Crossfade(
                targetState = currentTab.value,
                animationSpec = tween(220),
                modifier = Modifier.fillMaxSize()
            ) { targetIndex ->
                when (targetIndex) {
                    0 -> DashboardTab(viewModel, primaryColor, secondaryColor)
                    1 -> FocusTimerTab(viewModel, primaryColor, secondaryColor)
                    2 -> RewardsStoreTab(viewModel, primaryColor, secondaryColor)
                    3 -> AiCoachTab(viewModel, primaryColor, secondaryColor)
                }
            }

            // High Fidelity Status Alert Popup card (using compile-safe AnimatedVisibility)
            AnimatedVisibility(
                visible = statusMessage != null,
                enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
            ) {
                val msg = statusMessage ?: ""
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (msg.contains("❌")) Icons.Default.Warning else Icons.Default.Check,
                            contentDescription = "Alert icon",
                            tint = if (msg.contains("❌")) WarningRose else primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = msg,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Fullscreen Unskippable Intercept Interstitial Overlay (Simulating real accessibility service block)
            showOverlayByApp?.let { appName ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DeepSpaceDb.copy(alpha = 0.96f))
                        .clickable(enabled = false) {}, // Intercept touch events
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth()
                            .border(1.dp, WarningRose.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                            .testTag("interstitial_block_overlay"),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = SlateCard)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(WarningRose.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Block,
                                    contentDescription = "Blocked!",
                                    tint = WarningRose,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "STAY PRESENT!",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = WarningRose,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Shield Active • Deep Focus Session Running",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftGray
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Divider(color = SoftGray.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Your selection of Deep Focus blocks launching $appName. Focus on what truly matters to nurture your BePresent streak!",
                                fontSize = 14.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                lineHeight = 22.sp
                            )
                            Spacer(modifier = Modifier.height(28.dp))
                            Button(
                                onClick = { viewModel.dismissDeepFocusOverlay() },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("dismiss_block_overlay_btn")
                            ) {
                                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Return", tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Return to Focus Workspace", fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- APP TOP BAR ---
@Composable
fun WellnessTopBar(
    userProfile: UserProfile,
    primaryColor: Color,
    secondaryColor: Color,
    onHealStreak: () -> Unit
) {
    Surface(
        color = DeepSpaceDb,
        tonalElevation = 4.dp,
        modifier = Modifier.testTag("wellness_top_bar")
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Branding
                Column {
                    Text(
                        text = "BePresent",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = primaryColor,
                        fontFamily = FontFamily.SansSerif
                    )
                    Text(
                        text = "Attention Guardian",
                        fontSize = 11.sp,
                        color = SoftGray,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Points & Achievements Metrics Pills
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Points balance
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(RewardAmber.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Points indicator",
                            tint = RewardAmber,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${userProfile.totalPoints} XP",
                            color = RewardAmber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Habit streak
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(primaryColor.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = "Streak",
                            tint = primaryColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${userProfile.currentStreak}d",
                            color = primaryColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Streak Armor Indicator
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (userProfile.streakArmorCount > 0) primaryColor.copy(alpha = 0.2f)
                                else WarningRose.copy(alpha = 0.15f)
                            )
                            .clickable {
                                if (userProfile.currentStreak == 0 && userProfile.streakArmorCount > 0) {
                                    onHealStreak()
                                }
                            }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Streak Armor shield count",
                            tint = if (userProfile.streakArmorCount > 0) secondaryColor else WarningRose,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}


// ==========================================
// 📊 TAB 1: ADVANCED USAGE DASHBOARD
// ==========================================
@Composable
fun DashboardTab(
    viewModel: BePresentViewModel,
    primaryColor: Color,
    secondaryColor: Color
) {
    val logs by viewModel.allLogs.collectAsStateWithLifecycle()
    val driftEvents by viewModel.allDriftEvents.collectAsStateWithLifecycle()
    val todayMinutes by viewModel.todayScreenTimeMinutes.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("dashboard_tab"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // Core real-time ring indicator
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Screen Time Budget Usage",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // Visual Circular Ring for visual polish
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(110.dp)
                        ) {
                            val dailyLimit = if (profile.dailyScreenTimeLimitMinutes <= 0) 180f else profile.dailyScreenTimeLimitMinutes.toFloat()
                            val proportion = min(1f, todayMinutes.toFloat() / dailyLimit)
                            val animatedProportion by animateFloatAsState(
                                targetValue = proportion,
                                animationSpec = tween(1200, easing = LinearOutSlowInEasing),
                                label = "ScreenTimeAnimation"
                            )
                            
                            Canvas(modifier = Modifier.size(90.dp)) {
                                drawArc(
                                    color = Color.White.copy(alpha = 0.08f),
                                    startAngle = 0f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = 12.dp.toPx())
                                )
                                drawArc(
                                    brush = Brush.sweepGradient(
                                        listOf(primaryColor, secondaryColor, primaryColor)
                                    ),
                                    startAngle = -90f,
                                    sweepAngle = animatedProportion * 360f,
                                    useCenter = false,
                                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$todayMinutes",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "Mins Used",
                                    fontSize = 10.sp,
                                    color = SoftGray,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Statistics Panel
                        Column {
                            DetailStatsRow(label = "Daily Limit", value = "${profile.dailyScreenTimeLimitMinutes}m", tint = primaryColor)
                            DetailStatsRow(label = "Weekly Avg", value = "210m", tint = secondaryColor)
                            DetailStatsRow(label = "Streak Tier", value = "Bronze", tint = RewardAmber)
                        }
                    }
                }
            }
        }

        // Custom Canvas Donut & Bar Charts representation
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Category Usage Breakdown",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (logs.isEmpty()) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(120.dp), contentAlignment = Alignment.Center
                        ) {
                            Text("No usage recorded yet.", color = SoftGray)
                        }
                    } else {
                        // Drawing precise Canvas slices representing Social vs Productivity vs Gaming
                        val groups = logs.groupBy { it.category }
                        val totals = logs.sumOf { it.timeSpentMinutes }.toFloat()
                        val safeTotals = if (totals <= 0f) 1f else totals

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.size(80.dp)) {
                                    var startAngle = -90f
                                    groups.forEach { (cat, items) ->
                                        val minutes = items.sumOf { it.timeSpentMinutes }.toFloat()
                                        val sweep = (minutes / safeTotals) * 360f
                                        val sliceColor = getCategoryColor(cat)
                                        drawArc(
                                            color = sliceColor,
                                            startAngle = startAngle,
                                            sweepAngle = sweep,
                                            useCenter = false,
                                            style = Stroke(width = 16.dp.toPx())
                                        )
                                        startAngle += sweep
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Category Legend details
                            Column(modifier = Modifier.weight(1.3f)) {
                                groups.forEach { (cat, items) ->
                                    val minutes = items.sumOf { it.timeSpentMinutes }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(getCategoryColor(cat), CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "$cat: ${minutes}m",
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Enhanced Focus Drift Analytical Feeds
        item {
            Text(
                text = "⚡ Behavioral Analysis (Focus Drift)",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (driftEvents.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateCard.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Safe! No distracting focus drift incidents detected today. Stay sharp!",
                            color = SoftGray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(driftEvents) { event ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    modifier = Modifier.border(
                        1.dp,
                        WarningRose.copy(alpha = 0.25f),
                        RoundedCornerShape(12.dp)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Drift Warning",
                            tint = WarningRose,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Focus Drift Intercepted",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Closed Work Session ➔ Opened ${event.openedApp} ${event.timeDifferenceSeconds}s later!",
                                fontSize = 11.sp,
                                color = SoftGray
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(WarningRose.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("DRIFT", color = WarningRose, fontWeight = FontWeight.Black, fontSize = 9.sp)
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// ⏱️ TAB 2: POMODORO TIMER & SHIELD INTERCEPT
// ==========================================
@Composable
fun FocusTimerTab(
    viewModel: BePresentViewModel,
    primaryColor: Color,
    secondaryColor: Color
) {
    val timerMode by viewModel.timerMode.collectAsStateWithLifecycle()
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val timeLeftSeconds by viewModel.timeLeftSeconds.collectAsStateWithLifecycle()
    val blockedAppsList by viewModel.blockedApps.collectAsStateWithLifecycle()
    val selectedBlockedApps by viewModel.selectedBlockedApps.collectAsStateWithLifecycle()

    val minutes = timeLeftSeconds / 60
    val seconds = timeLeftSeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("focus_timer_tab"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Toggle tabs for Modes
        item {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(32.dp))
                    .background(SlateCard)
                    .padding(4.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(
                    TimerMode.FOCUS,
                    TimerMode.SHORT_BREAK,
                    TimerMode.LONG_BREAK
                ).forEach { mode ->
                    val isSelected = timerMode == mode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (isSelected) primaryColor else Color.Transparent)
                            .clickable { viewModel.setTimerMode(mode) }
                            .padding(vertical = 10.dp)
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (mode) {
                                TimerMode.FOCUS -> "Deep Focus"
                                TimerMode.SHORT_BREAK -> "Short Break"
                                TimerMode.LONG_BREAK -> "Long Break"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isSelected) Color.Black else Color.White
                        )
                    }
                }
            }
        }

        // Circular Rotating Timer Representation
        item {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(vertical = 20.dp)
                    .size(210.dp)
            ) {
                // Outer tracking ring
                Canvas(modifier = Modifier.size(195.dp)) {
                    drawArc(
                        color = Color.White.copy(alpha = 0.05f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 10.dp.toPx())
                    )
                }

                // Glowing circular ring
                val totalSecs = when (timerMode) {
                    TimerMode.FOCUS -> 25 * 60
                    TimerMode.SHORT_BREAK -> 5 * 60
                    TimerMode.LONG_BREAK -> 15 * 60
                }
                val arcProportion = timeLeftSeconds.toFloat() / totalSecs.toFloat()
                
                Canvas(modifier = Modifier.size(195.dp)) {
                    drawArc(
                        color = if (timerState == TimerState.RUNNING) primaryColor else SoftGray.copy(alpha = 0.3f),
                        startAngle = -90f,
                        sweepAngle = arcProportion * 360f,
                        useCenter = false,
                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formattedTime,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (timerState == TimerState.RUNNING) "FOCUSING" else "IDLE WORKSPACE",
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (timerState == TimerState.RUNNING) secondaryColor else SoftGray
                    )
                }
            }
        }

        // Control Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (timerState != TimerState.RUNNING) {
                    Button(
                        onClick = { viewModel.startTimer() },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("start_timer_btn")
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start", tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Start focus", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                } else {
                    Button(
                        onClick = { viewModel.pauseTimer() },
                        colors = ButtonDefaults.buttonColors(containerColor = WarningRose),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("pause_timer_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Pause, contentDescription = "Pause", tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pause Session", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                OutlinedButton(
                    onClick = { viewModel.resetTimer() },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("reset_timer_btn")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset Workspace", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Simulated Blocklist apps settings
        item {
            Divider(color = SoftGray.copy(alpha = 0.15f), modifier = Modifier.padding(top = 12.dp))
        }

        item {
            Text(
                text = "🛡️ Active Shield Configuration",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        // Lists of blocked Apps
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "Configure standard apps you wish to automatically block during any focus timer increments:",
                        fontSize = 11.sp,
                        color = SoftGray,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    blockedAppsList.forEach { app ->
                        val isBlocked = selectedBlockedApps.contains(app)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleAppBlock(app) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isBlocked,
                                onCheckedChange = { viewModel.toggleAppBlock(app) },
                                colors = CheckboxDefaults.colors(checkedColor = primaryColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(app, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.weight(1f))
                            if (isBlocked) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(primaryColor.copy(alpha = 0.12f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("BLOCKED", color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Simulate Action Center (For live testing and showing behavior limits)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, secondaryColor.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "📱 Simulated Launch Simulator (Live Intercept Demo)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Turn on the Deep Focus Timer above and try opening TikTok or Instagram below to verify the real-time blocking interception overlay is active!",
                        fontSize = 11.sp,
                        color = SoftGray,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.simulateAppLaunch("TikTok") },
                            colors = ButtonDefaults.buttonColors(containerColor = WarningRose),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("sim_tiktok_btn")
                        ) {
                            Text("Open TikTok", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.simulateAppLaunch("Instagram") },
                            colors = ButtonDefaults.buttonColors(containerColor = WarningRose),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("sim_instagram_btn")
                        ) {
                            Text("Open Instagram", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.simulateAppLaunch("Android Studio") },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("sim_studio_btn")
                        ) {
                            Text("Open Studio", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 💎 TAB 3: GAMIFIED REWARDS SHOP
// ==========================================
@Composable
fun RewardsStoreTab(
    viewModel: BePresentViewModel,
    primaryColor: Color,
    secondaryColor: Color
) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val rewardsList by viewModel.allRewards.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("rewards_tab"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // Points & Streak Armor controller Panel
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Gamified Rewards Engine",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Points Balance", fontSize = 11.sp, color = SoftGray)
                            Text("${profile.totalPoints} XP", fontSize = 28.sp, fontWeight = FontWeight.Black, color = RewardAmber)
                        }

                        Column {
                            Text("Streak Armor Protection", fontSize = 11.sp, color = SoftGray)
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Shield",
                                    tint = secondaryColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${profile.streakArmorCount} Charges",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    Divider(color = SoftGray.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Streak Armor Operations
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.buyStreakArmor() },
                            colors = ButtonDefaults.buttonColors(containerColor = secondaryColor),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("buy_armor_btn")
                        ) {
                            Text("Buy Shield (-80 XP)", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.healBrokenStreak() },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            enabled = profile.streakArmorCount > 0,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("heal_streak_btn")
                        ) {
                            Text("Heal Lost Streak", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Store rewards grid categories header
        item {
            Text(
                text = "💎 Unlock Custom Themes & Badges",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Store grid list items
        if (rewardsList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp), contentAlignment = Alignment.Center
                ) {
                    Text("Fetching store listings...", color = SoftGray)
                }
            }
        } else {
            items(rewardsList.chunked(2)) { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.forEach { reward ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SlateCard),
                            modifier = Modifier
                                .weight(1f)
                                .height(170.dp)
                                .border(
                                    BorderStroke(
                                        1.dp,
                                        if (reward.isUnlocked) primaryColor.copy(alpha = 0.35f) else Color.Transparent
                                    ),
                                    RoundedCornerShape(14.dp)
                                )
                                .testTag("reward_item_${reward.id}")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            if (reward.isUnlocked) primaryColor.copy(alpha = 0.12f)
                                            else SoftGray.copy(alpha = 0.1f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getRewardIcon(reward.iconName),
                                        contentDescription = reward.title,
                                        tint = if (reward.isUnlocked) primaryColor else SoftGray
                                    )
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = reward.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = reward.description,
                                        fontSize = 9.sp,
                                        color = SoftGray,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 12.sp,
                                        maxLines = 2
                                    )
                                }

                                if (reward.isUnlocked) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(primaryColor.copy(alpha = 0.15f))
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text("EQUIPPED", color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(primaryColor)
                                            .clickable { viewModel.redeemReward(reward) }
                                            .padding(horizontal = 12.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Locked", tint = Color.Black, modifier = Modifier.size(10.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${reward.costPoints} XP", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }
                    if (pair.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

fun getRewardIcon(iconName: String): ImageVector {
    return when (iconName) {
        "shield" -> Icons.Default.Shield
        "palette" -> Icons.Default.Palette
        "emoji_events" -> Icons.Default.EmojiEvents
        "security" -> Icons.Default.Security
        "spa" -> Icons.Default.Spa
        "workspace_premium" -> Icons.Default.WorkspacePremium
        else -> Icons.Default.Extension
    }
}


// ==========================================
// 🧠 TAB 4: AI DIGITAL HABIT COACH
// ==========================================
@Composable
fun AiCoachTab(
    viewModel: BePresentViewModel,
    primaryColor: Color,
    secondaryColor: Color
) {
    val adviceText by viewModel.coachAdvice.collectAsStateWithLifecycle()
    val isCoachLoading by viewModel.isCoachLoading.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("coach_tab"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        
        // Assistant Robot Header block
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            shape = RoundedCornerShape(18.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(primaryColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "AI Coach Avatar",
                        tint = primaryColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text("BePresent Guardian AI", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 15.sp)
                    Text("Dynamic analytic digital coach", color = SoftGray, fontSize = 11.sp)
                }
            }
        }

        // Live Chat Reports advice bubble
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCard)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                if (isCoachLoading) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = primaryColor)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Consulting Gemini Attention Engine...",
                            color = SoftGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                text = adviceText,
                                fontSize = 14.sp,
                                color = Color.White,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }
        }

        // Floating dynamic run analyze button
        Button(
            onClick = { viewModel.generateCoachAnalysis() },
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            shape = RoundedCornerShape(14.dp),
            enabled = !isCoachLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(bottom = 8.dp)
                .testTag("analyze_habits_btn")
        ) {
            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Intelligence", tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Analyze Screen Time logs with AI", fontWeight = FontWeight.ExtraBold, color = Color.Black, fontSize = 14.sp)
        }
    }
}

@Composable
fun DetailStatsRow(label: String, value: String, tint: Color) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(tint, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = "$label: ", fontSize = 11.sp, color = SoftGray)
        Text(text = value, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

fun getCategoryColor(category: String): Color {
    return when (category) {
        "Social" -> WarningRose
        "Productivity" -> PremiumEmerald
        "Gaming" -> Color(0xFFF97316) // Gaming Orange
        "Utility" -> SoftGray
        else -> AccentMint
    }
}
