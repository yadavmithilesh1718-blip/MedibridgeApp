package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicine_donations")
data class MedicineDonation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicineName: String,
    val genericName: String = "",
    val category: String,
    val dosage: String,
    val quantity: Int,
    val unit: String, // Tablets, Strips, Bottles, Vials, Boxes
    val expiryDate: String, // YYYY-MM-DD
    val batchNumber: String,
    val manufacturer: String,
    val packagingCondition: String, // "Sealed Box", "Intact Blister Strip", "Original Bottle Sealed"
    val storageRequirement: String, // "Room Temperature", "Refrigerated (2-8°C)", "Cool & Dry"
    val donorName: String,
    val donorPhone: String,
    val donorAddress: String,
    val notes: String = "",
    val imagePath: String? = null,
    val status: String = "AI_VERIFIED", // SUBMITTED, AI_VERIFIED, MATCHED, PICKUP_SCHEDULED, REDISTRIBUTED
    val aiVerificationScore: Int = 95, // 0 - 100
    val aiVerificationNotes: String = "Label & batch validated. Expiry safe for redistribution (>6 months).",
    val matchedOrganizationId: Long? = null,
    val matchedOrganizationName: String? = null,
    val estimatedValueUsd: Double = 15.0,
    val createdAt: Long = System.currentTimeMillis()
)
