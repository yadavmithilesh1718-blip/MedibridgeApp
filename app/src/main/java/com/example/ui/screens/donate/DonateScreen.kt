package com.example.ui.screens.donate

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PresetSampleMedicine
import com.example.data.model.SmartMatchRecommendation
import com.example.ui.theme.*
import com.example.ui.viewmodel.MediBridgeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonateScreen(
    viewModel: MediBridgeViewModel,
    onDonationSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val medicineName by viewModel.medicineName.collectAsStateWithLifecycle()
    val category by viewModel.category.collectAsStateWithLifecycle()
    val dosage by viewModel.dosage.collectAsStateWithLifecycle()
    val quantity by viewModel.quantity.collectAsStateWithLifecycle()
    val unit by viewModel.unit.collectAsStateWithLifecycle()
    val expiryDate by viewModel.expiryDate.collectAsStateWithLifecycle()
    val batchNumber by viewModel.batchNumber.collectAsStateWithLifecycle()
    val manufacturer by viewModel.manufacturer.collectAsStateWithLifecycle()
    val packagingCondition by viewModel.packagingCondition.collectAsStateWithLifecycle()
    val storageRequirement by viewModel.storageRequirement.collectAsStateWithLifecycle()
    val donorName by viewModel.donorName.collectAsStateWithLifecycle()
    val donorPhone by viewModel.donorPhone.collectAsStateWithLifecycle()
    val donorAddress by viewModel.donorAddress.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()

    val isAnalyzing by viewModel.isAnalyzingWithAi.collectAsStateWithLifecycle()
    val aiScanResult by viewModel.aiScanResult.collectAsStateWithLifecycle()
    val suggestedMatches by viewModel.suggestedMatches.collectAsStateWithLifecycle()
    val selectedMatchOrg by viewModel.selectedMatchOrg.collectAsStateWithLifecycle()
    val allOrgs by viewModel.allOrganizations.collectAsStateWithLifecycle()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var submittedDonationId by remember { mutableStateOf<Long?>(null) }

    // Zero-permission Android Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            viewModel.runAiOcrAnalysis()
        }
    }

    LaunchedEffect(Unit) {
        if (aiScanResult == null) {
            viewModel.runAiOcrAnalysis()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("donate_screen_column"),
        contentPadding = PaddingValues(16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Step 1: Preset Medicine Cards (1-Tap Fast Fill)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "1-Tap Sample Presets for Quick Testing",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Select a preset or upload your own medicine package photo below",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(viewModel.samplePresets) { preset ->
                            PresetChip(
                                preset = preset,
                                isSelected = medicineName == preset.medicineName,
                                onSelect = { viewModel.loadPresetSample(preset) }
                            )
                        }
                    }
                }
            }
        }

        // Step 2: Upload or Capture Medicine Packaging Photo
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Medicine Packaging & Image OCR",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Upload front label showing batch number, expiry date, and seal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(
                                1.5.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                photoPickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            }
                            .testTag("upload_photo_box"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = if (selectedImageUri != null) Icons.Default.CheckCircle else Icons.Default.PhotoCamera,
                                contentDescription = "Camera upload",
                                tint = if (selectedImageUri != null) StatusVerified else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (selectedImageUri != null) "Photo Uploaded & Scanned" else "Tap to Select Medicine Package Photo",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Zero-permission Android Photo Picker",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.runAiOcrAnalysis() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("run_ai_ocr_button"),
                        enabled = !isAnalyzing,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Running AI Verification & OCR...")
                        } else {
                            Icon(Icons.Default.DocumentScanner, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Run AI Safety & OCR Verification")
                        }
                    }
                }
            }
        }

        // AI Verification Report Banner
        aiScanResult?.let { scan ->
            item {
                AiVerificationReportCard(scan = scan)
            }
        }

        // Step 3: Medicine Details Form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Medicine Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = medicineName,
                        onValueChange = { viewModel.updateMedicineName(it) },
                        label = { Text("Medicine Trade Name") },
                        leadingIcon = { Icon(Icons.Default.Medication, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_medicine_name"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = { viewModel.updateCategory(it) },
                            label = { Text("Category") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_category"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = dosage,
                            onValueChange = { viewModel.updateDosage(it) },
                            label = { Text("Dosage / Strength") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_dosage"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { viewModel.updateQuantity(it) },
                            label = { Text("Quantity") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_quantity"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { viewModel.updateUnit(it) },
                            label = { Text("Unit (Strips/Tabs/Bottles)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_unit"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = expiryDate,
                            onValueChange = { viewModel.updateExpiryDate(it) },
                            label = { Text("Expiry (YYYY-MM-DD)") },
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("input_expiry_date"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = batchNumber,
                            onValueChange = { viewModel.updateBatchNumber(it) },
                            label = { Text("Batch #") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_batch_number"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    OutlinedTextField(
                        value = manufacturer,
                        onValueChange = { viewModel.updateManufacturer(it) },
                        label = { Text("Manufacturer") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = packagingCondition,
                        onValueChange = { viewModel.updatePackagingCondition(it) },
                        label = { Text("Packaging Condition (e.g. Intact Blister Strip)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_packaging_condition"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = storageRequirement,
                        onValueChange = { viewModel.updateStorageRequirement(it) },
                        label = { Text("Storage (Room Temp / Refrigerated)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // Step 4: Donor Details
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Donor & Pickup Contact",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = donorName,
                        onValueChange = { viewModel.updateDonorName(it) },
                        label = { Text("Your Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = donorPhone,
                        onValueChange = { viewModel.updateDonorPhone(it) },
                        label = { Text("Phone Number (for pickup coordination)") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = donorAddress,
                        onValueChange = { viewModel.updateDonorAddress(it) },
                        label = { Text("Pickup Address / Neighborhood") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { viewModel.updateNotes(it) },
                        label = { Text("Additional Notes (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // Step 5: Smart Matching Recommendations
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Smart Matching Destination",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "AI OPTIMIZED",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Automatically matched with verified clinics based on urgency and distance",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    if (suggestedMatches.isEmpty()) {
                        Text(
                            text = "Evaluating matches...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        suggestedMatches.take(3).forEach { match ->
                            val org = allOrgs.firstOrNull { it.id == match.organizationId }
                            val isSelected = selectedMatchOrg?.id == match.organizationId
                            MatchRecommendationItem(
                                match = match,
                                isSelected = isSelected,
                                onSelect = { viewModel.selectMatchOrg(org) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        // Submit Button
        item {
            Button(
                onClick = {
                    viewModel.submitDonation { id ->
                        submittedDonationId = id
                        showSuccessDialog = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_donation_button"),
                shape = RoundedCornerShape(14.dp),
                enabled = medicineName.isNotBlank() && quantity.isNotBlank()
            ) {
                Icon(Icons.Default.VolunteerActivism, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Confirm & Submit Donation",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Success Confirmation Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onDonationSuccess()
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = StatusVerified,
                        modifier = Modifier.size(28.dp)
                    )
                    Text("Donation Submitted!")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Your donation of $medicineName ($quantity $unit) has been successfully verified and logged.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    selectedMatchOrg?.let { org ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Matched Recipient:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = org.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${org.type} • ${org.city}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Text(
                        text = "A volunteer or courier coordinator will reach out to confirm pickup.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onDonationSuccess()
                    },
                    modifier = Modifier.testTag("dialog_done_button")
                ) {
                    Text("View in Tracker")
                }
            }
        )
    }
}

@Composable
fun PresetChip(
    preset: PresetSampleMedicine,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .clickable { onSelect() }
            .testTag("preset_${preset.category}")
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(
                text = preset.medicineName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${preset.quantity} ${preset.unit} • ${preset.dosage}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AiVerificationReportCard(
    scan: com.example.data.model.OcrScanResult
) {
    val isSafe = scan.isSafeForDonation
    val containerBg = if (isSafe) StatusVerified.copy(alpha = 0.1f) else StatusAlertExpiry.copy(alpha = 0.1f)
    val borderColor = if (isSafe) StatusVerified.copy(alpha = 0.4f) else StatusAlertExpiry.copy(alpha = 0.4f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ai_verification_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isSafe) Icons.Default.VerifiedUser else Icons.Default.ReportProblem,
                        contentDescription = null,
                        tint = if (isSafe) StatusVerified else StatusAlertExpiry,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = if (isSafe) "AI Safety Verified" else "AI Safety Flag",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSafe) StatusVerified else StatusAlertExpiry
                    )
                }

                Surface(
                    color = if (isSafe) StatusVerified else StatusAlertExpiry,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${scan.verificationScore}% Score",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = scan.verificationNotes,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Shelf-Life: ${scan.daysUntilExpiry} days",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Batch: ${scan.batchNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MatchRecommendationItem(
    match: SmartMatchRecommendation,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("match_item_${match.organizationId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                modifier = Modifier.size(24.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = match.organizationName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        color = StatusVerified.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${match.matchScorePercent}% Match",
                            style = MaterialTheme.typography.labelSmall,
                            color = StatusVerified,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "${match.organizationType} • ${String.format("%.1f", match.distanceKm)} km away",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = match.matchingReason,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
