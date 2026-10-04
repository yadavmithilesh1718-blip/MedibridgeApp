package com.example.data.model

data class OcrScanResult(
    val medicineName: String,
    val genericName: String,
    val category: String,
    val dosage: String,
    val quantity: Int,
    val unit: String,
    val expiryDate: String,
    val batchNumber: String,
    val manufacturer: String,
    val packagingCondition: String,
    val storageRequirement: String,
    val verificationScore: Int,
    val verificationNotes: String,
    val estimatedValueUsd: Double,
    val isSafeForDonation: Boolean,
    val daysUntilExpiry: Long
)

data class SmartMatchRecommendation(
    val organizationId: Long,
    val organizationName: String,
    val organizationType: String,
    val distanceKm: Double,
    val matchScorePercent: Int,
    val matchingReason: String,
    val matchingRequestName: String? = null,
    val urgencyLevel: String? = null
)

data class ImpactSummary(
    val totalMedicinesSavedCount: Int,
    val totalUnitsRedistributed: Int,
    val totalEstimatedValueUsd: Double,
    val verifiedNgoCount: Int,
    val livesTouchedEstimated: Int,
    val activeShortExpiryAlerts: Int
)

data class PresetSampleMedicine(
    val title: String,
    val subtitle: String,
    val medicineName: String,
    val genericName: String,
    val category: String,
    val dosage: String,
    val quantity: Int,
    val unit: String,
    val expiryDate: String,
    val batchNumber: String,
    val manufacturer: String,
    val packagingCondition: String,
    val storageRequirement: String,
    val estimatedValueUsd: Double
)
