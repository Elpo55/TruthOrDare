package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM players ORDER BY id ASC")
    fun getAllPlayersFlow(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players ORDER BY id ASC")
    suspend fun getAllPlayers(): List<PlayerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: PlayerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayers(players: List<PlayerEntity>)

    @Update
    suspend fun updatePlayer(player: PlayerEntity)

    @Delete
    suspend fun deletePlayer(player: PlayerEntity)

    @Query("DELETE FROM players")
    suspend fun deleteAllPlayers()
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    fun getAllHistoryFlow(): Flow<List<HistoryEntryEntity>>

    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    suspend fun getAllHistory(): List<HistoryEntryEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHistoryEntry(entry: HistoryEntryEntity)

    @Query("DELETE FROM history")
    suspend fun deleteAllHistory()
}

@Dao
interface CustomChallengeDao {
    @Query("SELECT * FROM custom_challenges")
    fun getAllCustomChallengesFlow(): Flow<List<CustomChallengeEntity>>

    @Query("SELECT * FROM custom_challenges")
    suspend fun getAllCustomChallenges(): List<CustomChallengeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomChallenge(challenge: CustomChallengeEntity)

    @Delete
    suspend fun deleteCustomChallenge(challenge: CustomChallengeEntity)
    
    @Query("DELETE FROM custom_challenges")
    suspend fun deleteAllCustomChallenges()
}
