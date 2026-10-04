package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MedicineDonation::class,
        RecipientOrganization::class,
        MedicineRequest::class,
        NotificationItem::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MediBridgeDatabase : RoomDatabase() {

    abstract fun mediBridgeDao(): MediBridgeDao

    companion object {
        @Volatile
        private var INSTANCE: MediBridgeDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): MediBridgeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MediBridgeDatabase::class.java,
                    "medibridge_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.mediBridgeDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: MediBridgeDao) {
            // Seed Verified Recipient Organizations
            val orgs = listOf(
                RecipientOrganization(
                    id = 1,
                    name = "Hope Health Foundation NGO",
                    type = "Verified NGO",
                    licenseNumber = "NGO-MED-2024-8841",
                    isVerified = true,
                    address = "42 Civic Center Rd, Medical District",
                    city = "Central Metro",
                    distanceKm = 2.4,
                    contactPerson = "Dr. Anita Sharma",
                    contactPhone = "+1 (555) 234-5678",
                    contactEmail = "donate@hopehealth.org",
                    acceptedCategories = "Antibiotics, Pain Relief, Chronic Care, Pediatric",
                    urgentNeeds = "Amoxicillin, Azithromycin, Metformin, Oral Rehydration",
                    rating = 4.9f,
                    donationsReceivedCount = 142
                ),
                RecipientOrganization(
                    id = 2,
                    name = "CarePoint Free Community Clinic",
                    type = "Community Clinic",
                    licenseNumber = "CLINIC-REG-39120",
                    isVerified = true,
                    address = "118 Green Valley Blvd, Suite B",
                    city = "Eastside",
                    distanceKm = 3.8,
                    contactPerson = "Marcus Vance, RN",
                    contactPhone = "+1 (555) 345-6789",
                    contactEmail = "coordinator@carepointclinic.org",
                    acceptedCategories = "Cardiovascular, Respiratory, Diabetes, Pain Relief",
                    urgentNeeds = "Insulin Glargine, Salbutamol Inhalers, Amlodipine",
                    rating = 4.8f,
                    donationsReceivedCount = 98
                ),
                RecipientOrganization(
                    id = 3,
                    name = "Seva Charitable Mission Hospital",
                    type = "Charitable Hospital",
                    licenseNumber = "HOSP-CHAR-77102",
                    isVerified = true,
                    address = "705 Healing Hands Way",
                    city = "South Park",
                    distanceKm = 5.1,
                    contactPerson = "Dr. Rajesh Kulkarni",
                    contactPhone = "+1 (555) 456-7890",
                    contactEmail = "pharmacy@sevamission.org",
                    acceptedCategories = "Antibiotics, Surgical Supplies, Oncology Support, Vitamins",
                    urgentNeeds = "Ceftriaxone, Paracetamol IV, Multivitamins, Iron Supplements",
                    rating = 5.0f,
                    donationsReceivedCount = 310
                ),
                RecipientOrganization(
                    id = 4,
                    name = "Apex Licensed Pharmacy Redistribution",
                    type = "Licensed Pharmacy",
                    licenseNumber = "PHARM-LIC-99042",
                    isVerified = true,
                    address = "12 Main Street Commercial Complex",
                    city = "Downtown",
                    distanceKm = 1.2,
                    contactPerson = "Elena Rostova, PharmD",
                    contactPhone = "+1 (555) 567-8901",
                    contactEmail = "support@apexpharmcare.com",
                    acceptedCategories = "General Medicines, Over-The-Counter, First Aid, Supplements",
                    urgentNeeds = "Ibuprofen 400mg, Antacids, Cetirizine, Bandages",
                    rating = 4.7f,
                    donationsReceivedCount = 76
                )
            )
            dao.insertOrganizations(orgs)

            // Seed Urgent Requests
            val requests = listOf(
                MedicineRequest(
                    id = 1,
                    organizationId = 1,
                    organizationName = "Hope Health Foundation NGO",
                    medicineName = "Amoxicillin 500mg",
                    category = "Antibiotics",
                    quantityNeeded = 100,
                    unit = "Strips",
                    urgencyLevel = "CRITICAL",
                    fulfilledQuantity = 30,
                    status = "OPEN",
                    purposeNotes = "For free community rural mobile camp treating bacterial infections."
                ),
                MedicineRequest(
                    id = 2,
                    organizationId = 2,
                    organizationName = "CarePoint Free Community Clinic",
                    medicineName = "Metformin 500mg",
                    category = "Chronic Care",
                    quantityNeeded = 150,
                    unit = "Strips",
                    urgencyLevel = "HIGH",
                    fulfilledQuantity = 60,
                    status = "OPEN",
                    purposeNotes = "For low-income senior citizens living with Type 2 Diabetes."
                ),
                MedicineRequest(
                    id = 3,
                    organizationId = 2,
                    organizationName = "CarePoint Free Community Clinic",
                    medicineName = "Salbutamol Inhaler (100mcg)",
                    category = "Respiratory",
                    quantityNeeded = 40,
                    unit = "Canisters",
                    urgencyLevel = "CRITICAL",
                    fulfilledQuantity = 10,
                    status = "OPEN",
                    purposeNotes = "Urgent asthma relief support for pediatric patients."
                ),
                MedicineRequest(
                    id = 4,
                    organizationId = 3,
                    organizationName = "Seva Charitable Mission Hospital",
                    medicineName = "Paracetamol 650mg",
                    category = "Pain Relief",
                    quantityNeeded = 200,
                    unit = "Strips",
                    urgencyLevel = "MODERATE",
                    fulfilledQuantity = 120,
                    status = "OPEN",
                    purposeNotes = "Post-op fever and pain management for underprivileged ward."
                )
            )
            dao.insertRequests(requests)

            // Seed Active & Completed Donations
            val donations = listOf(
                MedicineDonation(
                    id = 1,
                    medicineName = "Amoxicillin & Potassium Clavulanate",
                    genericName = "Amoxicillin + Clavulanic Acid (625mg)",
                    category = "Antibiotics",
                    dosage = "625mg",
                    quantity = 30,
                    unit = "Strips",
                    expiryDate = "2027-02-28",
                    batchNumber = "AMC-2024-X8",
                    manufacturer = "GSK Pharmaceuticals",
                    packagingCondition = "Intact Blister Strip",
                    storageRequirement = "Room Temperature (15-25°C)",
                    donorName = "Sarah Jenkins",
                    donorPhone = "+1 (555) 789-0123",
                    donorAddress = "84 Oakridge Terrace, Apt 4B",
                    notes = "Unopened unused prescribed surplus after treatment ended.",
                    status = "MATCHED",
                    aiVerificationScore = 98,
                    aiVerificationNotes = "Blister seal 100% intact. Expiry date valid for 16+ months. Manufacturer batch clear.",
                    matchedOrganizationId = 1,
                    matchedOrganizationName = "Hope Health Foundation NGO",
                    estimatedValueUsd = 45.0
                ),
                MedicineDonation(
                    id = 2,
                    medicineName = "Metformin Hydrochloride ER",
                    genericName = "Metformin 500mg Extended Release",
                    category = "Chronic Care",
                    dosage = "500mg",
                    quantity = 60,
                    unit = "Tablets",
                    expiryDate = "2026-11-15",
                    batchNumber = "MTF-9921-A",
                    manufacturer = "Sun Pharma",
                    packagingCondition = "Original Bottle Sealed",
                    storageRequirement = "Cool & Dry Place",
                    donorName = "David Chen",
                    donorPhone = "+1 (555) 890-1234",
                    donorAddress = "310 Pine Street",
                    notes = "Factory seal unbroken, stored in cool home medicine cabinet.",
                    status = "PICKUP_SCHEDULED",
                    aiVerificationScore = 96,
                    aiVerificationNotes = "Tamper evident seal intact. Verified batch code. Safely redistributable.",
                    matchedOrganizationId = 2,
                    matchedOrganizationName = "CarePoint Free Community Clinic",
                    estimatedValueUsd = 28.0
                ),
                MedicineDonation(
                    id = 3,
                    medicineName = "Paracetamol Tablets IP",
                    genericName = "Acetaminophen 650mg",
                    category = "Pain Relief",
                    dosage = "650mg",
                    quantity = 50,
                    unit = "Strips",
                    expiryDate = "2026-08-30",
                    batchNumber = "PCM-8830-K",
                    manufacturer = "Cipla Ltd",
                    packagingCondition = "Intact Blister Strip",
                    storageRequirement = "Room Temperature",
                    donorName = "Priya Patel",
                    donorPhone = "+1 (555) 901-2345",
                    donorAddress = "55 Maple Avenue",
                    notes = "Successfully redistributed to Seva Charitable Hospital ward.",
                    status = "REDISTRIBUTED",
                    aiVerificationScore = 99,
                    aiVerificationNotes = "Delivered to verified pharmacy team. Safe redistribution confirmed.",
                    matchedOrganizationId = 3,
                    matchedOrganizationName = "Seva Charitable Mission Hospital",
                    estimatedValueUsd = 35.0
                )
            )
            for (donation in donations) {
                dao.insertDonation(donation)
            }

            // Seed Initial Notifications
            val notifs = listOf(
                NotificationItem(
                    id = 1,
                    title = "Pickup Scheduled for Metformin",
                    message = "Volunteer courier assigned from CarePoint Free Clinic. Scheduled pickup tomorrow 10:00 AM.",
                    type = "PICKUP_SCHEDULED",
                    relatedEntityId = 2,
                    timestamp = System.currentTimeMillis() - 3600000L
                ),
                NotificationItem(
                    id = 2,
                    title = "AI Verification Complete (98% Score)",
                    message = "Your Amoxicillin 625mg donation passed OCR verification and matches Hope Health Foundation.",
                    type = "AI_VERIFICATION",
                    relatedEntityId = 1,
                    timestamp = System.currentTimeMillis() - 86400000L
                ),
                NotificationItem(
                    id = 3,
                    title = "Expiry Shelf-Life Reminder",
                    message = "Paracetamol batch approaching 60-day safety redistribution threshold. Successfully matched in time!",
                    type = "EXPIRY_ALERT",
                    relatedEntityId = 3,
                    timestamp = System.currentTimeMillis() - 172800000L
                )
            )
            for (notif in notifs) {
                dao.insertNotification(notif)
            }
        }
    }
}
