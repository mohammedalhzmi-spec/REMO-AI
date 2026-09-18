package com.ai.geminiapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val email: String,
    val password: String,
    val createdAt: Long = System.currentTimeMillis()
)
