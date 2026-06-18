package com.example.dacsiii_v2.ui.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Motorcycle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.example.dacsiii_v2.data.model.DriverOrderSummary
import com.example.dacsiii_v2.ui.common.EmptyState
import com.example.dacsiii_v2.ui.common.IosCard
import com.example.dacsiii_v2.ui.common.IosPrimaryButton
import com.example.dacsiii_v2.ui.common.StatusPill
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverAvailableOrdersScreen(
    token: String,
    viewModel: DriverViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(token) {
        if (token.isNotBlank()) {
            viewModel.startRealtimeOrders(context, token)
            viewModel.refreshAvailableOrders(token, showLoading = true)
            while (isActive) {
                delay(15000)
                viewModel.refreshAvailableOrders(token, showLoading = false)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopRealtimeOrders()
        }
    }

    LaunchedEffect(uiState.message) {
        val message = uiState.message
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đơn mới") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            if (uiState.availableOrders.isEmpty()) {
                item { EmptyState(message = "Không có đơn mới.") }
            }
            items(uiState.availableOrders) { order ->
                DriverOrderCard(
                    order = order,
                    driverLat = uiState.currentLat,
                    driverLng = uiState.currentLng,
                    actionLabel = "Nhận đơn",
                    onAction = {
                        viewModel.acceptOrder(token, order.order_id)
                    }
                )
            }
        }
    }
}

@Composable
private fun DriverOrderCard(
    order: DriverOrderSummary,
    driverLat: Double?,
    driverLng: Double?,
    actionLabel: String,
    onAction: () -> Unit
) {
    val localDistance = if (
        isValidLatitude(driverLat) &&
        isValidLongitude(driverLng) &&
        isValidLatitude(order.pickup_lat) &&
        isValidLongitude(order.pickup_lng)
    ) {
        calculateDistanceKm(driverLat!!, driverLng!!, order.pickup_lat!!, order.pickup_lng!!)
    } else {
        null
    }

    val distanceToPickup = localDistance ?: order.distance_from_driver
    val isNearPickup = distanceToPickup != null && distanceToPickup <= 3.0
    val distanceLabel = distanceToPickup?.let { distanceKm ->
        String.format(Locale.US, "%.2f km", distanceKm)
    }
    val tripDistanceLabel = order.distance_km?.let { tripKm ->
        String.format(Locale.US, "%.2f km", tripKm)
    }
    val advanceLabel = order.cod_amount?.let { amount ->
        String.format(Locale.US, "%,.0fđ", amount).replace(',', '.')
    }
    val driverEarningLabel = order.driver_earning?.let { earning ->
        String.format(Locale.US, "%,.0fđ", earning).replace(',', '.')
    } ?: "0đ"

    val highlightColor = if (isNearPickup) {
        Color(0xFFE8F5E9)
    } else {
        MaterialTheme.colorScheme.surface
    }

    IosCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = highlightColor,
        contentPadding = PaddingValues(16.dp)
    ) {
        // Row 1: Header (ID & Status Pill & Distance to Pickup Badge)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Motorcycle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Đơn #${order.order_id}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            StatusPill(
                text = order.status ?: "-",
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.weight(1f))
            if (distanceLabel != null) {
                DistanceBadge("Cách: $distanceLabel")
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        // Row 2: Earnings & Trip Info Dashboard
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Thu nhập của bạn",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "+$driverEarningLabel",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF2E7D32)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Quãng đường / Tạm ứng",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = listOfNotNull(
                        tripDistanceLabel,
                        advanceLabel?.let { "Ứng: $it" }
                    ).joinToString(" • "),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Package and weight info
        if (!order.package_type.isNullOrBlank() || order.weight_kg != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!order.package_type.isNullOrBlank()) {
                    Text(
                        text = "Loại hàng: ${order.package_type}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Normal
                    )
                }
                if (order.weight_kg != null) {
                    Text(
                        text = "Trọng lượng: ${order.weight_kg} kg",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }

        // Row 3: Route Address Connector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left column: path graphic
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(16.dp)
                    .padding(top = 4.dp)
            ) {
                // Green dot for pickup
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color(0xFF2E7D32), CircleShape)
                )
                // Connector line
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(36.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                // Red dot for delivery
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color(0xFFC62828), CircleShape)
                )
            }

            // Right column: address texts
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column {
                    Text(
                        text = "Điểm lấy hàng",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = order.pickup_address ?: "-",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Column {
                    Text(
                        text = "Điểm giao hàng",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = order.delivery_address ?: "-",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        // Row 4: Customer Details
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${order.customer_name ?: "-"} • ${order.customer_phone ?: "-"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Row 5: Action Button
        IosPrimaryButton(
            text = actionLabel,
            modifier = Modifier.fillMaxWidth(),
            onClick = onAction
        )
    }
}

@Composable
private fun DistanceBadge(text: String) {
    Surface(
        shape = CircleShape,
        color = Color(0xFFDDF5E3)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = Color(0xFF1B5E20),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun calculateDistanceKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val earthRadiusKm = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
        Math.sin(dLng / 2) * Math.sin(dLng / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return (earthRadiusKm * c * 100).toInt() / 100.0
}

private fun isValidLatitude(value: Double?): Boolean {
    return value != null && value >= -90.0 && value <= 90.0
}

private fun isValidLongitude(value: Double?): Boolean {
    return value != null && value >= -180.0 && value <= 180.0
}