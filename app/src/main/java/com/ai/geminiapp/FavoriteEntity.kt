package com.ai.geminiapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
