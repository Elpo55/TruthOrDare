package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val truthsAnswered: Int = 0,
    val actionsCompleted: Int = 0,
    val passesUsed: Int = 0
)

@Entity(tableName = "history")
data class HistoryEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val playerName: String,
    val type: String, // "ACTION", "VERITE", "EVENEMENT"
    val content: String,
    val result: String, // "REALISE", "PASSE"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_challenges")
data class CustomChallengeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "ACTION", "VERITE"
    val level: String, // "SOFT", "NORMAL", "CHAOS"
    val content: String
)
