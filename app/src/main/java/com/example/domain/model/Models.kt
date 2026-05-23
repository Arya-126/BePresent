package com.example.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long,
    val durationMinutes: Int,
    val category: String,
    val isCompleted: Boolean,
    val pointsEarned: Int
)

@Entity(tableName = "app_usage_logs")
data class AppUsageLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val appName: String,
    val category: String, // Social, Productivity, Gaming, Utility
    val timeSpentMinutes: Long,
    val timestamp: Long,
    val dateString: String // YYYY-MM-DD
)

@Entity(tableName = "unlocked_rewards")
data class UserReward(
    @PrimaryKey val id: String, // e.g. "badge_pioneer", "theme_cyberpunk"
    val title: String,
    val description: String,
    val costPoints: Int,
    val isUnlocked: Boolean = false,
    val iconName: String, // e.g. "star", "palette", "shield"
    val category: String // "Badge", "Theme", "Armor"
)

@Entity(tableName = "focus_drift_events")
data class FocusDriftEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val closedApp: String, // Closed a productivity app (e.g. Gmail)
    val openedApp: String,  // Fast opened a distracted app (e.g. TikTok)
    val timeDifferenceSeconds: Long
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Single-row configuration
    val totalPoints: Int = 120, // Pre-load points for demo ease!
    val currentStreak: Int = 3,
    val longestStreak: Int = 5,
    val streakArmorCount: Int = 1, // Protects streak if neglected
    val dailyScreenTimeLimitMinutes: Int = 180,
    val lastResetDateStr: String = ""
)
