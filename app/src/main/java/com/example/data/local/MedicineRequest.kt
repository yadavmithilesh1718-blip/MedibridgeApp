package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicine_requests")
data class MedicineRequest(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val organizationId: Long,
    val organizationName: String,
    val medicineName: String,
    val category: String,
    val quantityNeeded: Int,
    val unit: String,
    val urgencyLevel: String, // "CRITICAL", "HIGH", "MODERATE"
    val fulfilledQuantity: Int = 0,
    val status: String = "OPEN", // "OPEN", "PARTIALLY_FULFILLED", "FULFILLED"
    val purposeNotes: String = "",
    val requestedDate: Long = System.currentTimeMillis()
)
