package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiAiService
import com.example.data.local.MedicineDonation
import com.example.data.local.MedicineRequest
import com.example.data.local.NotificationItem
import com.example.data.local.RecipientOrganization
import com.example.data.model.ChatMessage
import com.example.data.model.DeliveryOutcomeReport
import com.example.data.model.ImpactSummary
import com.example.data.model.MessageRole
import com.example.data.model.NgoMatchAlert
import com.example.data.model.OcrScanResult
import com.example.data.model.PresetSampleMedicine
import com.example.data.model.SmartMatchRecommendation
import com.example.data.model.UserProfile
import com.example.data.repository.MediBridgeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class MediBridgeViewModel(
    private val repository: MediBridgeRepository,
    private val aiService: GeminiAiService
) : ViewModel() {

    // Database streams
    val allDonations: StateFlow<List<MedicineDonation>> = repository.allDonations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrganizations: StateFlow<List<RecipientOrganization>> = repository.allOrganizations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRequests: StateFlow<List<MedicineRequest>> = repository.allRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotifications: StateFlow<List<NotificationItem>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = repository.unreadNotificationsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Reactive Impact Statistics
    val impactSummary: StateFlow<ImpactSummary> = combine(
        allDonations,
        allOrganizations
    ) { donations, orgs ->
        var totalUnits = 0
        var totalValue = 0.0
        var shortExpiryAlerts = 0
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val now = System.currentTimeMillis()

        for (donation in donations) {
            totalUnits += donation.quantity
            totalValue += donation.estimatedValueUsd
            try {
                val exp = sdf.parse(donation.expiryDate)
                if (exp != null) {
                    val days = TimeUnit.MILLISECONDS.toDays(exp.time - now)
                    if (days in 1..90) {
                        shortExpiryAlerts++
                    }
                }
            } catch (_: Exception) {}
        }

        ImpactSummary(
            totalMedicinesSavedCount = donations.size,
            totalUnitsRedistributed = totalUnits,
            totalEstimatedValueUsd = totalValue,
            verifiedNgoCount = orgs.size,
            livesTouchedEstimated = (totalUnits * 2.5).toInt(),
            activeShortExpiryAlerts = shortExpiryAlerts
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ImpactSummary(0, 0, 0.0, 0, 0, 0))

    // User Profile State
    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    // Multi-turn Gemini Chatbot State
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                role = MessageRole.ASSISTANT,
                text = "Hello Dr. Watson! I am your MediBridge Assistant. I can help verify medicine safety, review delivery reports in your Donation History, or locate verified clinic drop-off centers using Google Maps.",
                mapsLocationNote = "Google Maps Healthcare Partner Grounding Active"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatSending = MutableStateFlow(false)
    val isChatSending: StateFlow<Boolean> = _isChatSending.asStateFlow()

    fun getDeliveryReport(donation: MedicineDonation): DeliveryOutcomeReport {
        return aiService.generateDeliveryReport(donation)
    }

    fun sendChatMessage(prompt: String) {
        if (prompt.isBlank()) return
        val userMsg = ChatMessage(role = MessageRole.USER, text = prompt.trim())
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            _isChatSending.value = true
            try {
                val responseMsg = aiService.sendChatQuery(_chatMessages.value, userMsg.text)
                _chatMessages.value = _chatMessages.value + responseMsg
            } finally {
                _isChatSending.value = false
            }
        }
    }

    // NGO Partner Portal & Real-time Urgent Match Alert State
    private val _selectedPartnerViewId = MutableStateFlow(1L) // Default: Hope Health Foundation NGO
    val selectedPartnerViewId: StateFlow<Long> = _selectedPartnerViewId.asStateFlow()

    private val _activeNgoMatchAlert = MutableStateFlow<NgoMatchAlert?>(null)
    val activeNgoMatchAlert: StateFlow<NgoMatchAlert?> = _activeNgoMatchAlert.asStateFlow()

    init {
        // Initialize an active urgent match notification for the default NGO partner
        viewModelScope.launch {
            kotlinx.coroutines.delay(800)
            triggerMatchAlertForPartner(1L)
        }
    }

    fun selectPartnerView(orgId: Long) {
        _selectedPartnerViewId.value = orgId
        triggerMatchAlertForPartner(orgId)
    }

    fun dismissNgoMatchAlert() {
        _activeNgoMatchAlert.value = null
    }

    fun triggerMatchAlertForPartner(orgId: Long) {
        val org = allOrganizations.value.firstOrNull { it.id == orgId } ?: return
        val matchingDonation = allDonations.value.firstOrNull { d ->
            (org.acceptedCategories.contains(d.category, ignoreCase = true) ||
                    org.urgentNeeds.contains(d.medicineName, ignoreCase = true)) &&
                    d.status != "DELIVERED"
        } ?: allDonations.value.firstOrNull()

        if (matchingDonation != null) {
            _activeNgoMatchAlert.value = NgoMatchAlert(
                organizationId = org.id,
                organizationName = org.name,
                medicineName = "${matchingDonation.medicineName} (${matchingDonation.dosage})",
                category = matchingDonation.category,
                quantity = matchingDonation.quantity,
                unit = matchingDonation.unit,
                distanceKm = org.distanceKm,
                locationName = org.city,
                matchReason = "Matches geographic pickup radius (${String.format(Locale.US, "%.1f", org.distanceKm)} km) & authorized ${matchingDonation.category} requirement",
                donationId = matchingDonation.id
            )
        }
    }

    fun getUrgentMatchCountForOrg(org: RecipientOrganization): Int {
        return allDonations.value.count { d ->
            (org.acceptedCategories.contains(d.category, ignoreCase = true) ||
                    org.urgentNeeds.contains(d.medicineName, ignoreCase = true)) &&
                    d.status != "DELIVERED" && d.status != "REDISTRIBUTED"
        }
    }

    fun getUrgentMatchesForOrg(org: RecipientOrganization): List<MedicineDonation> {
        return allDonations.value.filter { d ->
            (org.acceptedCategories.contains(d.category, ignoreCase = true) ||
                    org.urgentNeeds.contains(d.medicineName, ignoreCase = true)) &&
                    d.status != "DELIVERED" && d.status != "REDISTRIBUTED"
        }
    }

    fun acceptAndClaimDonationForNgo(donationId: Long, org: RecipientOrganization) {
        viewModelScope.launch {
            repository.matchDonationWithOrganization(donationId, org)
            dismissNgoMatchAlert()
        }
    }

    // Donate Form State
    val samplePresets = aiService.samplePresets

    private val _medicineName = MutableStateFlow("Amoxicillin Trihydrate")
    val medicineName: StateFlow<String> = _medicineName.asStateFlow()

    private val _genericName = MutableStateFlow("Amoxicillin (500mg)")
    val genericName: StateFlow<String> = _genericName.asStateFlow()

    private val _category = MutableStateFlow("Antibiotics")
    val category: StateFlow<String> = _category.asStateFlow()

    private val _dosage = MutableStateFlow("500mg")
    val dosage: StateFlow<String> = _dosage.asStateFlow()

    private val _quantity = MutableStateFlow("10")
    val quantity: StateFlow<String> = _quantity.asStateFlow()

    private val _unit = MutableStateFlow("Strips")
    val unit: StateFlow<String> = _unit.asStateFlow()

    private val _expiryDate = MutableStateFlow("2027-04-30")
    val expiryDate: StateFlow<String> = _expiryDate.asStateFlow()

    private val _batchNumber = MutableStateFlow("AMX-89301")
    val batchNumber: StateFlow<String> = _batchNumber.asStateFlow()

    private val _manufacturer = MutableStateFlow("Sandoz Pharmaceuticals")
    val manufacturer: StateFlow<String> = _manufacturer.asStateFlow()

    private val _packagingCondition = MutableStateFlow("Intact Blister Strip")
    val packagingCondition: StateFlow<String> = _packagingCondition.asStateFlow()

    private val _storageRequirement = MutableStateFlow("Room Temperature (15-25°C)")
    val storageRequirement: StateFlow<String> = _storageRequirement.asStateFlow()

    private val _donorName = MutableStateFlow("Dr. Emily Watson")
    val donorName: StateFlow<String> = _donorName.asStateFlow()

    private val _donorPhone = MutableStateFlow("+1 (555) 789-3321")
    val donorPhone: StateFlow<String> = _donorPhone.asStateFlow()

    private val _donorAddress = MutableStateFlow("720 Riverside Dr, Apt 12A")
    val donorAddress: StateFlow<String> = _donorAddress.asStateFlow()

    private val _notes = MutableStateFlow("Surplus unopened antibiotics, preserved in original blister.")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _isAnalyzingWithAi = MutableStateFlow(false)
    val isAnalyzingWithAi: StateFlow<Boolean> = _isAnalyzingWithAi.asStateFlow()

    private val _aiScanResult = MutableStateFlow<OcrScanResult?>(null)
    val aiScanResult: StateFlow<OcrScanResult?> = _aiScanResult.asStateFlow()

    private val _suggestedMatches = MutableStateFlow<List<SmartMatchRecommendation>>(emptyList())
    val suggestedMatches: StateFlow<List<SmartMatchRecommendation>> = _suggestedMatches.asStateFlow()

    private val _selectedMatchOrg = MutableStateFlow<RecipientOrganization?>(null)
    val selectedMatchOrg: StateFlow<RecipientOrganization?> = _selectedMatchOrg.asStateFlow()

    // Status filter for management screen
    private val _selectedStatusFilter = MutableStateFlow<String?>("ALL")
    val selectedStatusFilter: StateFlow<String?> = _selectedStatusFilter.asStateFlow()

    fun updateMedicineName(v: String) { _medicineName.value = v }
    fun updateCategory(v: String) { _category.value = v }
    fun updateDosage(v: String) { _dosage.value = v }
    fun updateQuantity(v: String) { _quantity.value = v }
    fun updateUnit(v: String) { _unit.value = v }
    fun updateExpiryDate(v: String) { _expiryDate.value = v }
    fun updateBatchNumber(v: String) { _batchNumber.value = v }
    fun updateManufacturer(v: String) { _manufacturer.value = v }
    fun updatePackagingCondition(v: String) { _packagingCondition.value = v }
    fun updateStorageRequirement(v: String) { _storageRequirement.value = v }
    fun updateDonorName(v: String) { _donorName.value = v }
    fun updateDonorPhone(v: String) { _donorPhone.value = v }
    fun updateDonorAddress(v: String) { _donorAddress.value = v }
    fun updateNotes(v: String) { _notes.value = v }
    fun setStatusFilter(status: String?) { _selectedStatusFilter.value = status }
    fun selectMatchOrg(org: RecipientOrganization?) { _selectedMatchOrg.value = org }

    fun loadPresetSample(preset: PresetSampleMedicine) {
        _medicineName.value = preset.medicineName
        _genericName.value = preset.genericName
        _category.value = preset.category
        _dosage.value = preset.dosage
        _quantity.value = preset.quantity.toString()
        _unit.value = preset.unit
        _expiryDate.value = preset.expiryDate
        _batchNumber.value = preset.batchNumber
        _manufacturer.value = preset.manufacturer
        _packagingCondition.value = preset.packagingCondition
        _storageRequirement.value = preset.storageRequirement
        runAiOcrAnalysis()
    }

    fun runAiOcrAnalysis() {
        viewModelScope.launch {
            _isAnalyzingWithAi.value = true
            try {
                val result = aiService.analyzeMedicinePackage(
                    medicineNameInput = _medicineName.value,
                    packagingConditionInput = _packagingCondition.value,
                    expiryDateInput = _expiryDate.value,
                    batchNumberInput = _batchNumber.value
                )
                _aiScanResult.value = result
                _genericName.value = result.genericName
                _category.value = result.category
                _dosage.value = result.dosage
                _storageRequirement.value = result.storageRequirement

                // Compute smart matches against verified NGOs and requests
                val tempDonation = MedicineDonation(
                    medicineName = result.medicineName,
                    category = result.category,
                    dosage = result.dosage,
                    quantity = _quantity.value.toIntOrNull() ?: 1,
                    unit = _unit.value,
                    expiryDate = result.expiryDate,
                    batchNumber = result.batchNumber,
                    manufacturer = result.manufacturer,
                    packagingCondition = result.packagingCondition,
                    storageRequirement = result.storageRequirement,
                    donorName = _donorName.value,
                    donorPhone = _donorPhone.value,
                    donorAddress = _donorAddress.value
                )

                val matches = aiService.findSmartMatches(
                    donation = tempDonation,
                    organizations = allOrganizations.value,
                    requests = allRequests.value
                )
                _suggestedMatches.value = matches
                if (matches.isNotEmpty()) {
                    val topOrg = allOrganizations.value.firstOrNull { it.id == matches.first().organizationId }
                    _selectedMatchOrg.value = topOrg
                }
            } finally {
                _isAnalyzingWithAi.value = false
            }
        }
    }

    fun submitDonation(onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            val scan = _aiScanResult.value
            val qty = _quantity.value.toIntOrNull() ?: 1
            val selectedOrg = _selectedMatchOrg.value

            val donation = MedicineDonation(
                medicineName = _medicineName.value,
                genericName = _genericName.value,
                category = _category.value,
                dosage = _dosage.value,
                quantity = qty,
                unit = _unit.value,
                expiryDate = _expiryDate.value,
                batchNumber = _batchNumber.value,
                manufacturer = _manufacturer.value,
                packagingCondition = _packagingCondition.value,
                storageRequirement = _storageRequirement.value,
                donorName = _donorName.value,
                donorPhone = _donorPhone.value,
                donorAddress = _donorAddress.value,
                notes = _notes.value,
                status = if (selectedOrg != null) "MATCHED" else "AI_VERIFIED",
                aiVerificationScore = scan?.verificationScore ?: 95,
                aiVerificationNotes = scan?.verificationNotes ?: "Verified packaging and validity.",
                matchedOrganizationId = selectedOrg?.id,
                matchedOrganizationName = selectedOrg?.name,
                estimatedValueUsd = scan?.estimatedValueUsd ?: (qty * 3.5)
            )

            val id = repository.insertDonation(donation)
            if (selectedOrg != null) {
                repository.matchDonationWithOrganization(id, selectedOrg)
            }
            onSuccess(id)
        }
    }

    fun advanceDonationStatus(donation: MedicineDonation) {
        viewModelScope.launch {
            repository.advanceDonationStatus(
                donationId = donation.id,
                currentStatus = donation.status,
                orgName = donation.matchedOrganizationName
            )
        }
    }

    fun matchDonationToOrg(donationId: Long, org: RecipientOrganization) {
        viewModelScope.launch {
            repository.matchDonationWithOrganization(donationId, org)
        }
    }

    fun markNotificationRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    fun postNewMedicineRequest(
        orgId: Long,
        orgName: String,
        medicineName: String,
        category: String,
        quantity: Int,
        unit: String,
        urgency: String,
        purpose: String
    ) {
        viewModelScope.launch {
            repository.insertRequest(
                MedicineRequest(
                    organizationId = orgId,
                    organizationName = orgName,
                    medicineName = medicineName,
                    category = category,
                    quantityNeeded = quantity,
                    unit = unit,
                    urgencyLevel = urgency,
                    purposeNotes = purpose
                )
            )
        }
    }
}

class MediBridgeViewModelFactory(
    private val repository: MediBridgeRepository,
    private val aiService: GeminiAiService
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MediBridgeViewModel::class.java)) {
            return MediBridgeViewModel(repository, aiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
