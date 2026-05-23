package com.example.data.db

import androidx.room.*
import com.example.domain.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<FocusSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSession)
}

@Dao
interface AppUsageLogDao {
    @Query("SELECT * FROM app_usage_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<AppUsageLog>>

    @Query("SELECT * FROM app_usage_logs WHERE dateString = :date ORDER BY timeSpentMinutes DESC")
    fun getLogsByDate(date: String): Flow<List<AppUsageLog>>

    @Query("SELECT * FROM app_usage_logs LIMIT 1")
    suspend fun getAnyLog(): AppUsageLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AppUsageLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<AppUsageLog>)

    @Query("DELETE FROM app_usage_logs")
    suspend fun clearAllLogs()
}

@Dao
interface UserRewardDao {
    @Query("SELECT * FROM unlocked_rewards ORDER BY category ASC")
    fun getAllRewards(): Flow<List<UserReward>>

    @Query("SELECT * FROM unlocked_rewards LIMIT 1")
    suspend fun getAnyReward(): UserReward?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRewards(rewards: List<UserReward>)

    @Update
    suspend fun updateReward(reward: UserReward)
}

@Dao
interface FocusDriftEventDao {
    @Query("SELECT * FROM focus_drift_events ORDER BY timestamp DESC")
    fun getAllDriftEvents(): Flow<List<FocusDriftEvent>>

    @Query("SELECT * FROM focus_drift_events LIMIT 1")
    suspend fun getAnyDriftEvent(): FocusDriftEvent?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDriftEvent(event: FocusDriftEvent)
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserProfileDirect(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)
}

@Database(
    entities = [
        FocusSession::class,
        AppUsageLog::class,
        UserReward::class,
        FocusDriftEvent::class,
        UserProfile::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BePresentDatabase : RoomDatabase() {
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun appUsageLogDao(): AppUsageLogDao
    abstract fun userRewardDao(): UserRewardDao
    abstract fun focusDriftEventDao(): FocusDriftEventDao
    abstract fun userProfileDao(): UserProfileDao
}
