package com.example.ui.screens.manage

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.MedicineDonation
import com.example.ui.screens.home.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.MediBridgeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageDonationsScreen(
    viewModel: MediBridgeViewModel,
    onNavigateToDonate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allDonations by viewModel.allDonations.collectAsStateWithLifecycle()
    val allOrgs by viewModel.allOrganizations.collectAsStateWithLifecycle()

    var statusFilter by remember { mutableStateOf("ALL") }
    var selectedDonationForDetail by remember { mutableStateOf<MedicineDonation?>(null) }
    var selectedDonationForMatch by remember { mutableStateOf<MedicineDonation?>(null) }

    val filteredDonations = remember(allDonations, statusFilter) {
        when (statusFilter) {
            "ACTIVE" -> allDonations.filter { it.status != "DELIVERED" && it.status != "REDISTRIBUTED" }
            "PICKUP" -> allDonations.filter { it.status == "PENDING_PICKUP" || it.status == "PICKUP_SCHEDULED" }
            "TRANSIT" -> allDonations.filter { it.status == "IN_TRANSIT" }
            "DELIVERED" -> allDonations.filter { it.status == "DELIVERED" || it.status == "REDISTRIBUTED" }
            else -> allDonations
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToDonate,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Donate Medicine") },
                modifier = Modifier.testTag("manage_add_donation_fab")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("manage_donations_screen_column")
        ) {
            // Header
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Text(
                    text = "Live Donation Tracker",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Detailed visual timeline tracking each milestone from verification to clinic delivery",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Status Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    "ALL" to "All (${allDonations.size})",
                    "ACTIVE" to "Active",
                    "PICKUP" to "Pending Pickup",
                    "TRANSIT" to "In Transit",
                    "DELIVERED" to "Delivered"
                )
                items(filters) { (key, label) ->
                    FilterChip(
                        selected = statusFilter == key,
                        onClick = { statusFilter = key },
                        label = { Text(label) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            if (filteredDonations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Donations in this Filter",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Add your unused unexpired medicine to help someone today.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, bottom = 84.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredDonations) { donation ->
                        DonationTrackerCard(
                            donation = donation,
                            onAdvance = { viewModel.advanceDonationStatus(donation) },
                            onMatchClick = { selectedDonationForMatch = donation },
                            onDetailsClick = { selectedDonationForDetail = donation }
                        )
                    }
                }
            }
        }
    }

    // Detail Certificate Dialog
    selectedDonationForDetail?.let { donation ->
        DonationCertificateDialog(
            donation = donation,
            onDismiss = { selectedDonationForDetail = null }
        )
    }

    // Match Dialog
    selectedDonationForMatch?.let { donation ->
        MatchOrganizationDialog(
            donation = donation,
            organizations = allOrgs,
            onDismiss = { selectedDonationForMatch = null },
            onSelectOrg = { org ->
                viewModel.matchDonationToOrg(donation.id, org)
                selectedDonationForMatch = null
            }
        )
    }
}

@Composable
fun DonationTrackerCard(
    donation: MedicineDonation,
    onAdvance: () -> Unit,
    onMatchClick: () -> Unit,
    onDetailsClick: () -> Unit
) {
    var isTimelineExpanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tracker_card_${donation.id}"),
        shape = RoundedCornerShape(20.dp),
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = donation.medicineName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${donation.category} • ${donation.dosage} • ${donation.quantity} ${donation.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(status = donation.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Matched Recipient Info Box
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Apartment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (!donation.matchedOrganizationName.isNullOrBlank())
                                "Matched Recipient: ${donation.matchedOrganizationName}"
                            else
                                "Awaiting Partner Match",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Batch: ${donation.batchNumber} • Expiry: ${donation.expiryDate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            var viewMode by remember { mutableStateOf("MAP") } // "MAP" or "TIMELINE"

            // View Switcher: Live Route Map vs Milestone Timeline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.height(34.dp)) {
                    SegmentedButton(
                        selected = viewMode == "MAP",
                        onClick = { viewMode = "MAP" },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        icon = {}
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text("Live Route Map", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    SegmentedButton(
                        selected = viewMode == "TIMELINE",
                        onClick = { viewMode = "TIMELINE" },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        icon = {}
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Timeline, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text("Milestones", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (donation.status == "DELIVERED" || donation.status == "REDISTRIBUTED") "✓ Delivered" else "● Live GPS",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Geographic Map or Timeline Content
            if (viewMode == "MAP") {
                DonationGeographicMap(donation = donation)
            } else {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    DonationDetailedTimeline(donation = donation)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDetailsClick,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Safety Certificate", style = MaterialTheme.typography.labelMedium)
                }

                when (donation.status) {
                    "SUBMITTED" -> {
                        Button(
                            onClick = onAdvance,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Run Verification", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    "VERIFIED", "AI_VERIFIED" -> {
                        Button(
                            onClick = onMatchClick,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Assign Pickup", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    "PENDING_PICKUP", "MATCHED" -> {
                        Button(
                            onClick = onAdvance,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Start Transit", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    "IN_TRANSIT", "PICKUP_SCHEDULED" -> {
                        Button(
                            onClick = onAdvance,
                            colors = ButtonDefaults.buttonColors(containerColor = StatusDelivered),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Confirm Delivered", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    "DELIVERED", "REDISTRIBUTED" -> {
                        Surface(
                            color = StatusVerified.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = StatusVerified, modifier = Modifier.size(14.dp))
                                Text(
                                    text = "Dispensed to Patients",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StatusVerified,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Detailed Visual Timeline for each medicine donation
 * Displays the 4 key stages: 'Verified', 'Pending Pickup', 'In Transit', 'Delivered'
 * with formatted timestamp markers and operational milestone notes.
 */
@Composable
fun DonationDetailedTimeline(donation: MedicineDonation) {
    val sdf = remember { SimpleDateFormat("MMM d, yyyy • hh:mm a", Locale.getDefault()) }
    val baseTime = donation.createdAt

    // Map status into progress indices (0: Submitted, 1: Verified, 2: Pending Pickup, 3: In Transit, 4: Delivered)
    val progressLevel = when (donation.status) {
        "SUBMITTED" -> 0
        "VERIFIED", "AI_VERIFIED" -> 1
        "PENDING_PICKUP", "MATCHED" -> 2
        "IN_TRANSIT", "PICKUP_SCHEDULED" -> 3
        "DELIVERED", "REDISTRIBUTED" -> 4
        else -> 1
    }

    val milestones = listOf(
        TimelineMilestone(
            title = "Verified",
            level = 1,
            timestamp = sdf.format(Date(baseTime + 2 * 60 * 1000L)),
            description = "AI OCR scan passed with ${donation.aiVerificationScore}% safety score. Packaging integrity and batch #${donation.batchNumber} validated.",
            icon = Icons.Default.VerifiedUser
        ),
        TimelineMilestone(
            title = "Pending Pickup",
            level = 2,
            timestamp = sdf.format(Date(baseTime + 45 * 60 * 1000L)),
            description = if (!donation.matchedOrganizationName.isNullOrBlank())
                "Matched with ${donation.matchedOrganizationName}. Courier collection dispatched to donor address."
            else
                "Matched with verified health partner. Courier dispatch pending confirmation.",
            icon = Icons.Default.Schedule
        ),
        TimelineMilestone(
            title = "In Transit",
            level = 3,
            timestamp = sdf.format(Date(baseTime + 180 * 60 * 1000L)),
            description = "Courier collected sealed package. Secure ${donation.storageRequirement} transport en route to clinic.",
            icon = Icons.Default.LocalShipping
        ),
        TimelineMilestone(
            title = "Delivered",
            level = 4,
            timestamp = sdf.format(Date(baseTime + 320 * 60 * 1000L)),
            description = "Safely received at clinic dispensary. Medication cataloged and dispensed to patients in need.",
            icon = Icons.Default.DoneAll
        )
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        milestones.forEachIndexed { index, milestone ->
            val isCompleted = progressLevel >= milestone.level
            val isCurrent = progressLevel == milestone.level
            val isLast = index == milestones.size - 1

            TimelineNodeRow(
                milestone = milestone,
                isCompleted = isCompleted,
                isCurrent = isCurrent,
                isLast = isLast
            )
        }
    }
}

data class TimelineMilestone(
    val title: String,
    val level: Int,
    val timestamp: String,
    val description: String,
    val icon: ImageVector
)

@Composable
fun TimelineNodeRow(
    milestone: TimelineMilestone,
    isCompleted: Boolean,
    isCurrent: Boolean,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Vertical Timeline Track & Indicator Node
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        color = when {
                            isCompleted -> StatusVerified
                            isCurrent -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCompleted) Icons.Default.Check else milestone.icon,
                    contentDescription = milestone.title,
                    tint = if (isCompleted || isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.5.dp)
                        .height(48.dp)
                        .background(
                            if (isCompleted) StatusVerified else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }

        // Timeline Event Details with Timestamp Marker
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 4.dp else 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = milestone.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (isCompleted || isCurrent) FontWeight.Bold else FontWeight.Medium,
                    color = when {
                        isCompleted -> StatusVerified
                        isCurrent -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                // Timestamp Marker Pill
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = milestone.timestamp,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = milestone.description,
                style = MaterialTheme.typography.bodySmall,
                color = if (isCompleted || isCurrent) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun DonationCertificateDialog(
    donation: MedicineDonation,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = StatusVerified,
                    modifier = Modifier.size(26.dp)
                )
                Text("Safety & Redistribution Certificate")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = donation.medicineName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Batch: ${donation.batchNumber} • Mfr: ${donation.manufacturer}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Expiry Date: ${donation.expiryDate} (Verified Safe)",
                            style = MaterialTheme.typography.bodySmall,
                            color = StatusVerified,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("AI Inspection Score:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${donation.aiVerificationScore}% Pass", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = StatusVerified)
                    }
                    Column {
                        Text("Packaging Seal:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(donation.packagingCondition, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                }

                Text(
                    text = "WHO Compliance Note:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = donation.aiVerificationNotes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close Certificate")
            }
        }
    )
}

@Composable
fun MatchOrganizationDialog(
    donation: MedicineDonation,
    organizations: List<com.example.data.local.RecipientOrganization>,
    onDismiss: () -> Unit,
    onSelectOrg: (com.example.data.local.RecipientOrganization) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Recipient NGO / Clinic") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Choose which verified medical partner will receive ${donation.medicineName}:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                organizations.forEach { org ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectOrg(org) }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = org.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(text = "${org.type} • ${org.city} (${String.format("%.1f", org.distanceKm)} km away)", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
