package com.example.dacsiii_v2.ui.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dacsiii_v2.data.model.DriverOrderSummary
import com.example.dacsiii_v2.ui.common.EmptyState
import com.example.dacsiii_v2.ui.common.IosCard
import com.example.dacsiii_v2.ui.common.IosPrimaryButton
import com.example.dacsiii_v2.ui.common.StatusPill
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverActiveOrdersScreen(
    token: String,
    viewModel: DriverViewModel,
    onNavigateBack: () -> Unit,
    onNavigateDetail: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(token) {
        if (token.isNotBlank()) {
            viewModel.fetchActiveOrders(token)
            while (isActive) {
                delay(15000)
                viewModel.fetchActiveOrders(token, showLoading = false)
            }
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
                title = { Text("Đơn đang chạy") },
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
            if (uiState.activeOrders.isEmpty()) {
                item { EmptyState(message = "Không có đơn đang chạy.") }
            }
            items(uiState.activeOrders) { order ->
                DriverActiveOrderCard(order = order, onOpenDetail = { onNavigateDetail(order.order_id) })
            }
        }
    }
}

@Composable
private fun DriverActiveOrderCard(order: DriverOrderSummary, onOpenDetail: () -> Unit) {
    IosCard(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Đơn #${order.order_id}")
        StatusPill(
            text = order.status ?: "-",
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
            contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = "Khách: ${order.customer_name ?: "-"}")
        Text(text = "Lấy: ${order.pickup_address ?: "-"}")
        Text(text = "Giao: ${order.delivery_address ?: "-"}")
        IosPrimaryButton(
            text = "Chi tiết",
            modifier = Modifier.fillMaxWidth(),
            onClick = onOpenDetail
        )
    }
}
