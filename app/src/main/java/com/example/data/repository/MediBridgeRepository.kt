package com.example.data.repository

import com.example.data.local.MediBridgeDao
import com.example.data.local.MedicineDonation
import com.example.data.local.MedicineRequest
import com.example.data.local.NotificationItem
import com.example.data.local.RecipientOrganization
import kotlinx.coroutines.flow.Flow

class MediBridgeRepository(private val dao: MediBridgeDao) {

    val allDonations: Flow<List<MedicineDonation>> = dao.getAllDonations()
    val allOrganizations: Flow<List<RecipientOrganization>> = dao.getAllOrganizations()
    val allRequests: Flow<List<MedicineRequest>> = dao.getAllRequests()
    val allNotifications: Flow<List<NotificationItem>> = dao.getAllNotifications()
    val unreadNotificationsCount: Flow<Int> = dao.getUnreadCount()

    suspend fun getDonationById(id: Long): MedicineDonation? = dao.getDonationById(id)

    suspend fun insertDonation(donation: MedicineDonation): Long {
        val id = dao.insertDonation(donation)
        // Auto-create verification notification
        dao.insertNotification(
            NotificationItem(
                title = "Donation Submitted: ${donation.medicineName}",
                message = "AI safety check completed with score ${donation.aiVerificationScore}%. Ready for NGO matching.",
                type = "AI_VERIFICATION",
                relatedEntityId = id
            )
        )
        return id
    }

    suspend fun updateDonation(donation: MedicineDonation) = dao.updateDonation(donation)

    suspend fun deleteDonation(donation: MedicineDonation) = dao.deleteDonation(donation)

    suspend fun matchDonationWithOrganization(donationId: Long, org: RecipientOrganization) {
        dao.updateDonationMatch(
            id = donationId,
            status = "PENDING_PICKUP",
            orgId = org.id,
            orgName = org.name
        )
        dao.insertNotification(
            NotificationItem(
                title = "Matched with ${org.name}",
                message = "${org.type} accepted your donation match. Courier pickup is now pending.",
                type = "MATCH_FOUND",
                relatedEntityId = donationId
            )
        )
    }

    suspend fun advanceDonationStatus(donationId: Long, currentStatus: String, orgName: String? = null) {
        val nextStatus = when (currentStatus) {
            "SUBMITTED" -> "VERIFIED"
            "VERIFIED", "AI_VERIFIED" -> "PENDING_PICKUP"
            "PENDING_PICKUP", "MATCHED" -> "IN_TRANSIT"
            "IN_TRANSIT", "PICKUP_SCHEDULED" -> "DELIVERED"
            else -> currentStatus
        }
        dao.updateDonationStatus(donationId, nextStatus)

        val notificationTitle = when (nextStatus) {
            "VERIFIED" -> "AI Safety Verified"
            "PENDING_PICKUP" -> "Pickup Scheduled"
            "IN_TRANSIT" -> "Medicine In Transit"
            "DELIVERED" -> "Successfully Delivered & Redistributed!"
            else -> "Status Updated: $nextStatus"
        }
        val notificationMsg = when (nextStatus) {
            "VERIFIED" -> "Packaging integrity and shelf-life verified. Ready for clinic matching."
            "PENDING_PICKUP" -> "Courier dispatch assigned. Awaiting package collection."
            "IN_TRANSIT" -> "Courier has picked up package and is en route to recipient clinic."
            "DELIVERED" -> "Medicine safely received by verified clinic & dispensed to patients in need!"
            else -> "Donation status moved to $nextStatus."
        }

        dao.insertNotification(
            NotificationItem(
                title = notificationTitle,
                message = notificationMsg,
                type = nextStatus,
                relatedEntityId = donationId
            )
        )
    }

    suspend fun insertRequest(request: MedicineRequest): Long = dao.insertRequest(request)

    suspend fun markNotificationAsRead(id: Long) = dao.markNotificationAsRead(id)

    suspend fun markAllNotificationsAsRead() = dao.markAllNotificationsAsRead()

    suspend fun deleteNotification(id: Long) = dao.deleteNotification(id)
}
