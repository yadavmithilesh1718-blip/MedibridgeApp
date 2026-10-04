package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipient_organizations")
data class RecipientOrganization(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String, // "Verified NGO", "Community Clinic", "Charitable Hospital", "Licensed Pharmacy"
    val licenseNumber: String,
    val isVerified: Boolean = true,
    val address: String,
    val city: String,
    val distanceKm: Double,
    val contactPerson: String,
    val contactPhone: String,
    val contactEmail: String,
    val acceptedCategories: String, // comma-separated e.g. "Antibiotics, Pain Relief, Chronic Care"
    val urgentNeeds: String, // e.g. "Insulin, Metformin, Amoxicillin"
    val rating: Float = 4.9f,
    val donationsReceivedCount: Int = 12
)
