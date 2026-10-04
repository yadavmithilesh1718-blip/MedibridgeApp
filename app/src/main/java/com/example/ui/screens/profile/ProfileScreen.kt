package com.example.ui.screens.profile

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.MedicineDonation
import com.example.data.model.DeliveryOutcomeReport
import com.example.ui.screens.home.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.MediBridgeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MediBridgeViewModel,
    onNavigateToDonate: () -> Unit,
    onNavigateToTracker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val allDonations by viewModel.allDonations.collectAsStateWithLifecycle()
    val impact by viewModel.impactSummary.collectAsStateWithLifecycle()

    var historyFilter by remember { mutableStateOf("ALL") }
    var selectedReportDonation by remember { mutableStateOf<MedicineDonation?>(null) }

    val filteredHistory = remember(allDonations, historyFilter) {
        when (historyFilter) {
            "DELIVERED" -> allDonations.filter { it.status == "DELIVERED" || it.status == "REDISTRIBUTED" }
            "TRANSIT" -> allDonations.filter { it.status == "IN_TRANSIT" || it.status == "PENDING_PICKUP" || it.status == "PICKUP_SCHEDULED" }
            else -> allDonations
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen_column"),
        contentPadding = PaddingValues(16.dp, bottom = 84.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    Brush.linearGradient(
                                        listOf(MediTealPrimary, MediSkySecondary)
                                    ),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "EW",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = profile.fullName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified Donor",
                                    tint = StatusVerified,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = profile.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${profile.badgeIcon} ${profile.donorTier}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Profile Stat Metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ProfileMetric(
                            value = "${allDonations.size}",
                            label = "Contributions",
                            icon = Icons.Default.Medication
                        )
                        ProfileMetric(
                            value = "${impact.totalUnitsRedistributed}",
                            label = "Units Donated",
                            icon = Icons.Default.VolunteerActivism
                        )
                        ProfileMetric(
                            value = "$${String.format("%.0f", impact.totalEstimatedValueUsd)}",
                            label = "Value Saved",
                            icon = Icons.Default.Savings
                        )
                        ProfileMetric(
                            value = "${allDonations.count { it.status == "DELIVERED" || it.status == "REDISTRIBUTED" }}",
                            label = "Delivered",
                            icon = Icons.Default.DoneAll
                        )
                    }
                }
            }
        }

        // Section Title: Donation History
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Donation History & Delivery Outcomes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Past contributions, chain-of-custody tracking, and final delivery status reports",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // History Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    "ALL" to "All Contributions (${allDonations.size})",
                    "DELIVERED" to "Delivered to Clinics (${allDonations.count { it.status == "DELIVERED" || it.status == "REDISTRIBUTED" }})",
                    "TRANSIT" to "In Transit / Active"
                )
                items(filters) { (key, label) ->
                    FilterChip(
                        selected = historyFilter == key,
                        onClick = { historyFilter = key },
                        label = { Text(label) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        // Donation History Items List
        if (filteredHistory.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Past Contributions in this Filter",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Your completed contributions will display official delivery outcome certificates here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredHistory) { donation ->
                DonationHistoryCard(
                    donation = donation,
                    onViewReport = { selectedReportDonation = donation }
                )
            }
        }
    }

    // Official Delivery Outcome Report Dialog
    selectedReportDonation?.let { donation ->
        val report = viewModel.getDeliveryReport(donation)
        DeliveryOutcomeReportDialog(
            donation = donation,
            report = report,
            onDismiss = { selectedReportDonation = null }
        )
    }
}

@Composable
fun ProfileMetric(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DonationHistoryCard(
    donation: MedicineDonation,
    onViewReport: () -> Unit
) {
    val isDelivered = donation.status == "DELIVERED" || donation.status == "REDISTRIBUTED"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("donation_history_card_${donation.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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

            // Final Delivery Outcome Block
            Surface(
                color = if (isDelivered) StatusVerified.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp),
                border = if (isDelivered) androidx.compose.foundation.BorderStroke(1.dp, StatusVerified.copy(alpha = 0.3f)) else null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isDelivered) Icons.Default.CheckCircle else Icons.Default.Schedule,
                            contentDescription = null,
                            tint = if (isDelivered) StatusVerified else StatusPending,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isDelivered) "Final Delivery Outcome:" else "Current Status:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isDelivered) StatusVerified else StatusPending
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isDelivered)
                            "✓ Successfully received at ${donation.matchedOrganizationName ?: "Healthcare Partner"} dispensary. Cataloged into stock and dispensed to patients in need."
                        else
                            "Package active in redistribution pipeline. Current stage: ${donation.status.replace("_", " ")}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Batch, Expiry & Handover Metadata
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Batch: ${donation.batchNumber} • Expiry: ${donation.expiryDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Receipt: DELIV-REC-${donation.id * 1000 + 492}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = onViewReport,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delivery Report", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun DeliveryOutcomeReportDialog(
    donation: MedicineDonation,
    report: DeliveryOutcomeReport,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

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
                    modifier = Modifier.size(28.dp)
                )
                Text("Delivery Outcome Report")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Receipt Card
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "OFFICIAL REPORT",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = report.receiptId,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = report.medicineName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${report.quantity} ${report.unit} • Batch #${report.batchNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Delivery Sign-off
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Chain of Custody Handover:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "• Recipient: ${report.recipientOrgName}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Receiving Officer: ${report.receivingPharmacist} (${report.pharmacistLicense})",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Timestamp: ${report.deliveryTimestamp}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Quality Checks
                Surface(
                    color = StatusVerified.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Safety & Quality Verification:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = StatusVerified
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "✓ Sealed packaging verified intact\n✓ Storage compliance certified (${donation.storageRequirement})\n✓ AI Inspection Score: ${report.verifiedSafetyScore}%",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // Patient Impact
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Patient & Community Impact:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = report.patientImpactSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "MediBridge Delivery Outcome Report\nReceipt: ${report.receiptId}\nMedicine: ${report.medicineName}\nRecipient: ${report.recipientOrgName}\nStatus: Delivered & Dispensed\nReceiving Pharmacist: ${report.receivingPharmacist}\nImpact: ${report.patientImpactSummary}"
                        )
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Delivery Report"))
                }
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share Report")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
