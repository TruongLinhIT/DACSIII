package com.example.dacsiii_v2.ui.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
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
    actionLabel: String,
    onAction: () -> Unit
) {
    val distance = order.distance_from_driver
    val isNearPickup = distance != null && distance <= 3.0
    val distanceLabel = distance?.let { distanceKm ->
        String.format(Locale.US, "Gần bạn - %.2f km", distanceKm)
    }

    val highlightColor = if (isNearPickup) {
        Color(0xFFE6F4EA)
    } else {
        MaterialTheme.colorScheme.surface
    }

    IosCard(modifier = Modifier.fillMaxWidth(), containerColor = highlightColor) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Đơn #${order.order_id}")
            Spacer(modifier = Modifier.width(8.dp))
            StatusPill(
                text = order.status ?: "-",
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
                contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.weight(1f))
            if (isNearPickup && !distanceLabel.isNullOrBlank()) {
                DistanceBadge(distanceLabel)
            }
        }
        Text(text = "Khách: ${order.customer_name ?: "-"} • ${order.customer_phone ?: "-"}")
        Text(text = "Lấy: ${order.pickup_address ?: "-"}")
        Text(text = "Giao: ${order.delivery_address ?: "-"}")
        Text(text = "Thu nhập: ${order.driver_earning ?: 0.0}")
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

