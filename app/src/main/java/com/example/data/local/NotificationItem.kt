package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val type: String, // "EXPIRY_ALERT", "MATCH_FOUND", "PICKUP_SCHEDULED", "REDISTRIBUTED", "AI_VERIFICATION"
    val relatedEntityId: Long? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
