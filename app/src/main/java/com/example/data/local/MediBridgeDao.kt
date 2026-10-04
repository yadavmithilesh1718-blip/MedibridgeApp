package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MediBridgeDao {

    // --- Donations ---
    @Query("SELECT * FROM medicine_donations ORDER BY createdAt DESC")
    fun getAllDonations(): Flow<List<MedicineDonation>>

    @Query("SELECT * FROM medicine_donations WHERE id = :id")
    suspend fun getDonationById(id: Long): MedicineDonation?

    @Query("SELECT * FROM medicine_donations WHERE status = :status ORDER BY createdAt DESC")
    fun getDonationsByStatus(status: String): Flow<List<MedicineDonation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonation(donation: MedicineDonation): Long

    @Update
    suspend fun updateDonation(donation: MedicineDonation)

    @Delete
    suspend fun deleteDonation(donation: MedicineDonation)

    @Query("UPDATE medicine_donations SET status = :status, matchedOrganizationId = :orgId, matchedOrganizationName = :orgName WHERE id = :id")
    suspend fun updateDonationMatch(id: Long, status: String, orgId: Long?, orgName: String?)

    @Query("UPDATE medicine_donations SET status = :status WHERE id = :id")
    suspend fun updateDonationStatus(id: Long, status: String)

    // --- Recipient Organizations ---
    @Query("SELECT * FROM recipient_organizations ORDER BY distanceKm ASC")
    fun getAllOrganizations(): Flow<List<RecipientOrganization>>

    @Query("SELECT * FROM recipient_organizations WHERE id = :id")
    suspend fun getOrganizationById(id: Long): RecipientOrganization?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrganizations(organizations: List<RecipientOrganization>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrganization(organization: RecipientOrganization): Long

    // --- Medicine Requests ---
    @Query("SELECT * FROM medicine_requests ORDER BY CASE urgencyLevel WHEN 'CRITICAL' THEN 1 WHEN 'HIGH' THEN 2 ELSE 3 END, requestedDate DESC")
    fun getAllRequests(): Flow<List<MedicineRequest>>

    @Query("SELECT * FROM medicine_requests WHERE status = 'OPEN' ORDER BY requestedDate DESC")
    fun getOpenRequests(): Flow<List<MedicineRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequests(requests: List<MedicineRequest>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: MedicineRequest): Long

    @Update
    suspend fun updateRequest(request: MedicineRequest)

    // --- Notifications ---
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationItem>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationItem): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: Long)
}
