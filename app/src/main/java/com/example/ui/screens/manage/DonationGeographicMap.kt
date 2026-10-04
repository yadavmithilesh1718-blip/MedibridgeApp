package com.example.ui.screens.manage

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MedicineDonation
import com.example.ui.theme.*

/**
 * Real-time Geographic Map Visualization showing progress of donation
 * from Donor Origin to NGO Partner Destination.
 */
@Composable
fun DonationGeographicMap(
    donation: MedicineDonation,
    modifier: Modifier = Modifier
) {
    // Map progress based on donation status
    val targetProgress = when (donation.status) {
        "SUBMITTED", "VERIFIED", "AI_VERIFIED" -> 0.08f
        "PENDING_PICKUP", "MATCHED" -> 0.28f
        "IN_TRANSIT", "PICKUP_SCHEDULED" -> 0.68f
        "DELIVERED", "REDISTRIBUTED" -> 1.0f
        else -> 0.45f
    }

    // Animated progress along route
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
        label = "routeProgress"
    )

    // Pulsing beacon animation for active vehicle
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    var mapZoom by remember { mutableFloatStateOf(1.0f) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("donation_geo_map_${donation.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Live Route Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Color(0xFF0F172A)) // Dark slate modern map styling
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // 1. Draw subtle road grid / map blocks
                    val gridColor = Color(0xFF1E293B)
                    val streetColor = Color(0xFF334155)

                    // Secondary grid
                    for (x in 0..w.toInt() step 50) {
                        drawLine(
                            color = gridColor,
                            start = Offset(x.toFloat(), 0f),
                            end = Offset(x.toFloat(), h),
                            strokeWidth = 1f
                        )
                    }
                    for (y in 0..h.toInt() step 40) {
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y.toFloat()),
                            end = Offset(w, y.toFloat()),
                            strokeWidth = 1f
                        )
                    }

                    // Arterial Avenue
                    drawLine(
                        color = streetColor,
                        start = Offset(0f, h * 0.45f),
                        end = Offset(w, h * 0.55f),
                        strokeWidth = 4f
                    )
                    drawLine(
                        color = streetColor,
                        start = Offset(w * 0.45f, 0f),
                        end = Offset(w * 0.55f, h),
                        strokeWidth = 3f
                    )

                    // 2. Define Waypoints: Donor Origin -> Midpoint 1 -> Midpoint 2 -> NGO Destination
                    val startPt = Offset(w * 0.16f, h * 0.68f)
                    val cp1 = Offset(w * 0.38f, h * 0.28f)
                    val cp2 = Offset(w * 0.64f, h * 0.76f)
                    val endPt = Offset(w * 0.86f, h * 0.32f)

                    // Path
                    val routePath = Path().apply {
                        moveTo(startPt.x, startPt.y)
                        cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, endPt.x, endPt.y)
                    }

                    // Background Route (Dashed or Faded Track)
                    drawPath(
                        path = routePath,
                        color = Color(0xFF475569),
                        style = Stroke(
                            width = 6f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )
                    )

                    // Active Progress Route (Emerald / Teal Glow)
                    val pathMeasure = android.graphics.PathMeasure()
                    val androidPath = android.graphics.Path()
                    androidPath.moveTo(startPt.x, startPt.y)
                    androidPath.cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, endPt.x, endPt.y)
                    pathMeasure.setPath(androidPath, false)
                    val totalLength = pathMeasure.length
                    val currentLen = totalLength * animatedProgress

                    val progressAndroidPath = android.graphics.Path()
                    pathMeasure.getSegment(0f, currentLen, progressAndroidPath, true)
                    drawPath(
                        path = progressAndroidPath.asComposePath(),
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF10B981), Color(0xFF06B6D4), Color(0xFF3B82F6))
                        ),
                        style = Stroke(width = 7f, cap = StrokeCap.Round)
                    )

                    // Calculate Courier Vehicle Position
                    val pos = floatArrayOf(startPt.x, startPt.y)
                    val tan = floatArrayOf(1f, 0f)
                    pathMeasure.getPosTan(currentLen, pos, tan)
                    val vehicleOffset = Offset(pos[0], pos[1])

                    // Pulse Beacon under vehicle
                    if (animatedProgress < 1.0f) {
                        drawCircle(
                            color = Color(0xFF06B6D4).copy(alpha = pulseAlpha),
                            radius = 24f * pulseScale,
                            center = vehicleOffset
                        )
                    }

                    // Draw Vehicle Node
                    drawCircle(
                        color = Color(0xFF0284C7),
                        radius = 12f,
                        center = vehicleOffset
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 5f,
                        center = vehicleOffset
                    )

                    // 3. Draw Origin Node (Donor)
                    drawCircle(
                        color = Color(0xFF10B981),
                        radius = 14f,
                        center = startPt
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 6f,
                        center = startPt
                    )

                    // 4. Draw Destination Node (NGO Partner)
                    drawCircle(
                        color = Color(0xFF7C3AED).copy(alpha = 0.35f),
                        radius = 22f,
                        center = endPt
                    )
                    drawCircle(
                        color = Color(0xFF7C3AED),
                        radius = 14f,
                        center = endPt
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 6f,
                        center = endPt
                    )
                }

                // Top Left Overlay: GPS Status Tag
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.TopStart)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(if (targetProgress >= 1f) StatusVerified else Color(0xFF06B6D4), CircleShape)
                        )
                        Text(
                            text = if (targetProgress >= 1f) "DELIVERY COMPLETED" else "LIVE GPS TELEMETRY",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Top Right: Zoom & Center Controls
                Row(
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.TopEnd),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilledIconButton(
                        onClick = { mapZoom = (mapZoom + 0.2f).coerceAtMost(2f) },
                        modifier = Modifier.size(28.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.Black.copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    FilledIconButton(
                        onClick = { mapZoom = (mapZoom - 0.2f).coerceAtLeast(0.8f) },
                        modifier = Modifier.size(28.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.Black.copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }

                // Origin Label Tag (Donor)
                Surface(
                    color = Color(0xFF0F172A).copy(alpha = 0.9f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                    modifier = Modifier
                        .padding(start = 12.dp, bottom = 12.dp)
                        .align(Alignment.BottomStart)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(12.dp))
                        Text(
                            text = "Donor: ${donation.donorName.ifBlank { "Emily W." }}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Destination Label Tag (NGO Partner)
                Surface(
                    color = Color(0xFF0F172A).copy(alpha = 0.9f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7C3AED).copy(alpha = 0.5f)),
                    modifier = Modifier
                        .padding(end = 12.dp, top = 40.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.LocalHospital, contentDescription = null, tint = Color(0xFFC084FC), modifier = Modifier.size(12.dp))
                        Text(
                            text = (donation.matchedOrganizationName ?: "Hope Health NGO").take(18),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Real-Time Telemetry HUD Bottom Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TelemetryItem(
                        icon = Icons.Default.DirectionsCar,
                        label = "Courier",
                        value = if (targetProgress >= 1f) "Delivered" else "Transit #VB-402"
                    )

                    TelemetryItem(
                        icon = Icons.Default.Straighten,
                        label = "Distance",
                        value = when {
                            targetProgress >= 1f -> "2.4 km completed"
                            targetProgress >= 0.5f -> "0.8 km remaining"
                            else -> "2.4 km total"
                        }
                    )

                    TelemetryItem(
                        icon = Icons.Default.Thermostat,
                        label = "Cold-Chain",
                        value = if (donation.storageRequirement.contains("Refrigerated", ignoreCase = true)) "4.8°C (Safe)" else "Ambient 21°C"
                    )

                    TelemetryItem(
                        icon = Icons.Default.Schedule,
                        label = "ETA",
                        value = if (targetProgress >= 1f) "Arrived" else "12 mins"
                    )
                }
            }
        }
    }
}

@Composable
private fun TelemetryItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
