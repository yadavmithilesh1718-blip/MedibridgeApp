package com.example.data.model

data class UserProfile(
    val fullName: String = "Dr. Emily Watson",
    val email: String = "emily.watson@communityhealth.org",
    val phone: String = "+1 (555) 789-3321",
    val address: String = "500 Medical Center Way, Suite 100",
    val donorTier: String = "Gold Healthcare Champion",
    val badgeIcon: String = "🏆",
    val memberSince: String = "January 2024",
    val totalDonationsLogged: Int = 5,
    val totalUnitsRedistributed: Int = 200,
    val estimatedEconomicSavedUsd: Double = 165.0
)

data class DeliveryOutcomeReport(
    val receiptId: String,
    val medicineName: String,
    val quantity: Int,
    val unit: String,
    val batchNumber: String,
    val recipientOrgName: String,
    val recipientType: String,
    val deliveryTimestamp: String,
    val receivingPharmacist: String,
    val pharmacistLicense: String,
    val packagingIntegrityConfirmed: Boolean,
    val coldChainCompliant: Boolean,
    val patientImpactSummary: String,
    val verifiedSafetyScore: Int
)

enum class MessageRole {
    USER,
    ASSISTANT
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: MessageRole,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val mapsLocationNote: String? = null
)

data class NgoMatchAlert(
    val id: String = java.util.UUID.randomUUID().toString(),
    val organizationId: Long,
    val organizationName: String,
    val medicineName: String,
    val category: String,
    val quantity: Int,
    val unit: String,
    val distanceKm: Double,
    val locationName: String,
    val matchReason: String,
    val donationId: Long,
    val timestamp: Long = System.currentTimeMillis()
)
