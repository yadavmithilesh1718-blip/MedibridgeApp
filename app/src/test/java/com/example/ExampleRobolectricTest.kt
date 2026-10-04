package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ai.GeminiAiService
import com.example.data.local.MedicineDonation
import com.example.data.local.MedicineRequest
import com.example.data.local.RecipientOrganization
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("MediBridge", appName)
    }

    @Test
    fun `ai smart matching matches critical needs`() = runTest {
        val aiService = GeminiAiService()
        val donation = MedicineDonation(
            medicineName = "Amoxicillin Trihydrate",
            category = "Antibiotics",
            dosage = "500mg",
            quantity = 10,
            unit = "Strips",
            expiryDate = "2027-04-30",
            batchNumber = "AMX-89301",
            manufacturer = "Sandoz",
            packagingCondition = "Intact Blister Strip",
            storageRequirement = "Room Temperature",
            donorName = "Test Donor",
            donorPhone = "555-1234",
            donorAddress = "123 Main St"
        )

        val org = RecipientOrganization(
            id = 1,
            name = "Hope Health Foundation NGO",
            type = "Verified NGO",
            licenseNumber = "NGO-102",
            address = "Downtown",
            city = "Metro",
            distanceKm = 2.0,
            contactPerson = "Dr. Test",
            contactPhone = "555-9999",
            contactEmail = "test@hope.org",
            acceptedCategories = "Antibiotics, Pain Relief",
            urgentNeeds = "Amoxicillin"
        )

        val request = MedicineRequest(
            id = 1,
            organizationId = 1,
            organizationName = "Hope Health Foundation NGO",
            medicineName = "Amoxicillin 500mg",
            category = "Antibiotics",
            quantityNeeded = 50,
            unit = "Strips",
            urgencyLevel = "CRITICAL"
        )

        val matches = aiService.findSmartMatches(donation, listOf(org), listOf(request))
        assertTrue(matches.isNotEmpty())
        assertEquals(1, matches.first().organizationId)
        assertTrue(matches.first().matchScorePercent >= 90)
    }
}
