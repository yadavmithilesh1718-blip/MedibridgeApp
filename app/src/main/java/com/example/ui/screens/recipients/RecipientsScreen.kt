package com.example.ui.screens.recipients

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.MedicineDonation
import com.example.data.local.MedicineRequest
import com.example.data.local.RecipientOrganization
import com.example.data.model.NgoMatchAlert
import com.example.ui.theme.*
import com.example.ui.viewmodel.MediBridgeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipientsScreen(
    viewModel: MediBridgeViewModel,
    onDonateForRequest: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val allOrgs by viewModel.allOrganizations.collectAsStateWithLifecycle()
    val allRequests by viewModel.allRequests.collectAsStateWithLifecycle()
    val selectedPartnerId by viewModel.selectedPartnerViewId.collectAsStateWithLifecycle()
    val activeMatchAlert by viewModel.activeNgoMatchAlert.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Verified Partners, 1: Urgent Requests, 2: NGO Partner View
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("ALL") }
    var showNewRequestDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Trigger Snackbar toast when a new urgent match alert appears
    LaunchedEffect(activeMatchAlert) {
        activeMatchAlert?.let { alert ->
            coroutineScope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = "⚡ Urgent Match: ${alert.medicineName} in ${alert.locationName} (${String.format("%.1f", alert.distanceKm)} km) matches your requirements!",
                    actionLabel = "View Alert",
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    selectedTab = 2
                }
            }
        }
    }

    val filteredOrgs = remember(allOrgs, searchQuery, selectedTypeFilter) {
        allOrgs.filter { org ->
            val matchesQuery = org.name.contains(searchQuery, ignoreCase = true) ||
                    org.city.contains(searchQuery, ignoreCase = true) ||
                    org.acceptedCategories.contains(searchQuery, ignoreCase = true) ||
                    org.urgentNeeds.contains(searchQuery, ignoreCase = true)

            val matchesType = when (selectedTypeFilter) {
                "NGO" -> org.type.contains("NGO", ignoreCase = true)
                "CLINIC" -> org.type.contains("Clinic", ignoreCase = true) || org.type.contains("Hospital", ignoreCase = true)
                "PHARMACY" -> org.type.contains("Pharmacy", ignoreCase = true)
                else -> true
            }
            matchesQuery && matchesType
        }
    }

    val selectedPartner = remember(allOrgs, selectedPartnerId) {
        allOrgs.firstOrNull { it.id == selectedPartnerId } ?: allOrgs.firstOrNull()
    }

    val currentNgoMatchCount = remember(selectedPartner, viewModel.allDonations) {
        selectedPartner?.let { viewModel.getUrgentMatchCountForOrg(it) } ?: 0
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            if (selectedTab == 1) {
                ExtendedFloatingActionButton(
                    onClick = { showNewRequestDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Post Need") },
                    modifier = Modifier.testTag("post_need_fab")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("recipients_screen_column")
        ) {
            // Header Tabs with Real-time Notification Badge on NGO Partner View
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Partners (${filteredOrgs.size})") },
                    icon = { Icon(Icons.Default.Verified, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Requests (${allRequests.size})") },
                    icon = { Icon(Icons.Default.Emergency, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("NGO Portal") },
                    icon = {
                        if (currentNgoMatchCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(containerColor = MaterialTheme.colorScheme.error) {
                                        Text("$currentNgoMatchCount")
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Storefront, contentDescription = null)
                            }
                        } else {
                            Icon(Icons.Default.Storefront, contentDescription = null)
                        }
                    }
                )
            }

            if (selectedTab == 0) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("search_recipients_input"),
                    placeholder = { Text("Search by name, city, or medicine needed...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                // Filter chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf(
                        "ALL" to "All Partners",
                        "NGO" to "Verified NGOs",
                        "CLINIC" to "Community Clinics & Hospitals",
                        "PHARMACY" to "Legal Pharmacies"
                    )
                    items(filters) { (key, label) ->
                        FilterChip(
                            selected = selectedTypeFilter == key,
                            onClick = { selectedTypeFilter = key },
                            label = { Text(label) },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredOrgs) { org ->
                        val urgentCount = viewModel.getUrgentMatchCountForOrg(org)
                        OrganizationCard(
                            org = org,
                            urgentMatchCount = urgentCount,
                            onOpenPortalView = {
                                viewModel.selectPartnerView(org.id)
                                selectedTab = 2
                            },
                            onCall = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${org.contactPhone}"))
                                context.startActivity(intent)
                            },
                            onEmail = {
                                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${org.contactEmail}"))
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            } else if (selectedTab == 1) {
                // Urgent Requests View
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(allRequests) { request ->
                        UrgentRequestDetailCard(
                            request = request,
                            onPledge = {
                                onDonateForRequest(request.medicineName)
                            }
                        )
                    }
                }
            } else {
                // TAB 2: NGO PARTNER PORTAL VIEW
                selectedPartner?.let { org ->
                    NgoPartnerPortalView(
                        org = org,
                        allOrgs = allOrgs,
                        activeAlert = activeMatchAlert,
                        matchedDonations = viewModel.getUrgentMatchesForOrg(org),
                        onSelectOrg = { viewModel.selectPartnerView(it.id) },
                        onDismissAlert = { viewModel.dismissNgoMatchAlert() },
                        onClaimDonation = { donationId ->
                            viewModel.acceptAndClaimDonationForNgo(donationId, org)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("✓ Donation claimed! Pickup scheduled for ${org.name}.")
                            }
                        },
                        onSimulateAlert = {
                            viewModel.triggerMatchAlertForPartner(org.id)
                        }
                    )
                }
            }
        }
    }

    if (showNewRequestDialog) {
        PostRequestDialog(
            organizations = allOrgs,
            onDismiss = { showNewRequestDialog = false },
            onSubmit = { orgId, orgName, medName, category, qty, unit, urgency, purpose ->
                viewModel.postNewMedicineRequest(orgId, orgName, medName, category, qty, unit, urgency, purpose)
                showNewRequestDialog = false
            }
        )
    }
}

/**
 * Dedicated NGO Partner Portal View
 * Displays real-time notification toast/card when an urgent donation matches
 * their specific geographic radius and medicine-type requirements.
 */
@Composable
fun NgoPartnerPortalView(
    org: RecipientOrganization,
    allOrgs: List<RecipientOrganization>,
    activeAlert: NgoMatchAlert?,
    matchedDonations: List<MedicineDonation>,
    onSelectOrg: (RecipientOrganization) -> Unit,
    onDismissAlert: () -> Unit,
    onClaimDonation: (Long) -> Unit,
    onSimulateAlert: () -> Unit
) {
    var expandedPartnerSelector by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ngo_partner_portal_view"),
        contentPadding = PaddingValues(16.dp, bottom = 84.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Partner Switcher Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Viewing as NGO Partner:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = org.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${org.type} • ${org.city} (${String.format("%.1f", org.distanceKm)} km) • Lic: ${org.licenseNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        OutlinedButton(
                            onClick = { expandedPartnerSelector = !expandedPartnerSelector },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("Switch Partner", style = MaterialTheme.typography.labelSmall)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }

                    // Dropdown selector
                    if (expandedPartnerSelector) {
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        allOrgs.forEach { otherOrg ->
                            Surface(
                                color = if (otherOrg.id == org.id) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectOrg(otherOrg)
                                        expandedPartnerSelector = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${otherOrg.name} (${otherOrg.type})",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (otherOrg.id == org.id) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (otherOrg.id == org.id) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Real-Time Notification Toast / Banner (When an urgent match exists)
        item {
            AnimatedVisibility(
                visible = activeAlert != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                activeAlert?.let { alert ->
                    RealTimeNgoMatchCard(
                        alert = alert,
                        onClaim = { onClaimDonation(alert.donationId) },
                        onDismiss = onDismissAlert
                    )
                }
            }
        }

        // Simulation Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Incoming Matched Donations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Real-time donations filtered to your geographic radius & medicine categories",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledTonalButton(
                    onClick = onSimulateAlert,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFD97706))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Trigger Alert", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // List of Matched Donations for this NGO
        if (matchedDonations.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusVerified, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No Pending Matches", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(
                            text = "When new donations match ${org.name}'s geographic radius and categories (${org.acceptedCategories}), real-time alerts will appear here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(matchedDonations) { donation ->
                NgoMatchedDonationItemCard(
                    donation = donation,
                    org = org,
                    onClaim = { onClaimDonation(donation.id) }
                )
            }
        }
    }
}

/**
 * Real-Time Animated Notification Toast / Banner for NGO Partners
 */
@Composable
fun RealTimeNgoMatchCard(
    alert: NgoMatchAlert,
    onClaim: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("real_time_ngo_match_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)), // Warm urgent tint
        border = BorderStroke(1.5.dp, Color(0xFFF59E0B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Urgency Pill & Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFFF59E0B),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Text(
                            text = "REAL-TIME URGENT MATCH ALERT",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(16.dp), tint = Color(0xFF92400E))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Medicine & Quantity
            Text(
                text = alert.medicineName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF78350F)
            )
            Text(
                text = "${alert.quantity} ${alert.unit} • Category: ${alert.category}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF92400E),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Geographic & Category Match Breakdown
            Surface(
                color = Color.White.copy(alpha = 0.85f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = StatusMatched, modifier = Modifier.size(14.dp))
                        Text(
                            text = "Geographic Match: Within ${String.format("%.1f", alert.distanceKm)} km (${alert.locationName} pickup radius)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = StatusVerified, modifier = Modifier.size(14.dp))
                        Text(
                            text = "Requirement Match: ${alert.matchReason}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF334155)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Decline", color = Color(0xFF92400E))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onClaim,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Accept & Schedule Pickup", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun NgoMatchedDonationItemCard(
    donation: MedicineDonation,
    org: RecipientOrganization,
    onClaim: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = donation.medicineName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${donation.dosage} • ${donation.quantity} ${donation.unit} • Batch: ${donation.batchNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${donation.aiVerificationScore}% Safety",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Expiry: ${donation.expiryDate} • Storage: ${donation.storageRequirement}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = onClaim,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Claim Donation", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun OrganizationCard(
    org: RecipientOrganization,
    urgentMatchCount: Int = 0,
    onOpenPortalView: () -> Unit = {},
    onCall: () -> Unit,
    onEmail: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("org_card_${org.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                when {
                                    org.type.contains("NGO") -> MediTealContainer
                                    org.type.contains("Hospital") || org.type.contains("Clinic") -> MediSkyContainer
                                    else -> Color(0xFFF3E8FF)
                                },
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                org.type.contains("NGO") -> Icons.Default.VolunteerActivism
                                org.type.contains("Hospital") || org.type.contains("Clinic") -> Icons.Default.LocalHospital
                                else -> Icons.Default.LocalPharmacy
                            },
                            contentDescription = null,
                            tint = when {
                                org.type.contains("NGO") -> MediTealPrimary
                                org.type.contains("Hospital") || org.type.contains("Clinic") -> MediSkySecondary
                                else -> Color(0xFF7E22CE)
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = org.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified Legal",
                                tint = StatusVerified,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "${org.type} • Lic: ${org.licenseNumber}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${org.rating}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Real-Time Urgent Match Badge (If matches exist for this NGO)
            if (urgentMatchCount > 0) {
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenPortalView() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                            Text(
                                text = "$urgentMatchCount Urgent Donations Matched Nearby!",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                        }
                        Text(
                            text = "Open Portal →",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD97706)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${org.address}, ${org.city} (${String.format("%.1f", org.distanceKm)} km away)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "Urgent Needs:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = org.urgentNeeds,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${org.donationsReceivedCount} donations received",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onCall,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Call", style = MaterialTheme.typography.labelSmall)
                    }

                    Button(
                        onClick = onEmail,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Contact", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun UrgentRequestDetailCard(
    request: MedicineRequest,
    onPledge: () -> Unit
) {
    val progress = (request.fulfilledQuantity.toFloat() / request.quantityNeeded.toFloat()).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("request_card_${request.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.medicineName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Requested by: ${request.organizationName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Surface(
                    color = when (request.urgencyLevel) {
                        "CRITICAL" -> StatusAlertExpiry.copy(alpha = 0.15f)
                        "HIGH" -> StatusMatched.copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = request.urgencyLevel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (request.urgencyLevel) {
                            "CRITICAL" -> StatusAlertExpiry
                            "HIGH" -> StatusMatched
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Fulfilled: ${request.fulfilledQuantity} of ${request.quantityNeeded} ${request.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = when (request.urgencyLevel) {
                        "CRITICAL" -> StatusAlertExpiry
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            }

            if (request.purposeNotes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Clinical Purpose: ${request.purposeNotes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onPledge,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Donate to Fulfill this Request")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostRequestDialog(
    organizations: List<RecipientOrganization>,
    onDismiss: () -> Unit,
    onSubmit: (Long, String, String, String, Int, String, String, String) -> Unit
) {
    var selectedOrg by remember { mutableStateOf(organizations.firstOrNull()) }
    var medicineName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Antibiotics") }
    var quantity by remember { mutableStateOf("50") }
    var unit by remember { mutableStateOf("Strips") }
    var urgency by remember { mutableStateOf("HIGH") }
    var purpose by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Post Clinic Medical Need") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Request unexpired medications needed by certified community clinics.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = medicineName,
                    onValueChange = { medicineName = it },
                    label = { Text("Medicine Required *") },
                    placeholder = { Text("e.g. Amoxicillin 500mg") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    label = { Text("Clinical Purpose / Target Ward") },
                    placeholder = { Text("e.g. Free outpatient dispensary for low-income seniors") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val org = selectedOrg ?: organizations.first()
                    onSubmit(
                        org.id,
                        org.name,
                        medicineName,
                        category,
                        quantity.toIntOrNull() ?: 10,
                        unit,
                        urgency,
                        purpose
                    )
                },
                enabled = medicineName.isNotBlank()
            ) {
                Text("Publish Request")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
