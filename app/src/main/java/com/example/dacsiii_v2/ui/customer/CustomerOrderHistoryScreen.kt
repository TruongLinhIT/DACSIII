package com.example.dacsiii_v2.ui.customer

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dacsiii_v2.ui.common.EmptyState
import com.example.dacsiii_v2.ui.common.IosCard
import com.example.dacsiii_v2.ui.common.StatusPill

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerOrderHistoryScreen(
    token: String,
    viewModel: CustomerViewModel,
    onNavigateBack: () -> Unit,
    onNavigateDetail: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(token) {
        if (token.isNotBlank()) {
            viewModel.fetchOrders(token)
        }
    }

    val activeOrders = uiState.orders.filter { it.status != "completed" }
    val historyOrders = uiState.orders.filter { it.status == "completed" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đơn hàng") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Đang đến") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Lịch sử") }
                )
            }

            val ordersToShow = if (selectedTab == 0) activeOrders else historyOrders
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                if (ordersToShow.isEmpty()) {
                    item {
                        val message = if (selectedTab == 0) {
                            "Chưa có đơn đang đến."
                        } else {
                            "Chưa có đơn hàng hoàn tất."
                        }
                        EmptyState(message = message)
                    }
                }
                items(ordersToShow) { order ->
                    IosCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateDetail(order.order_id) }
                    ) {
                        Text(text = "Đơn #${order.order_id}")
                        StatusPill(
                            text = order.status ?: "-",
                            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(text = "Tổng tiền: ${order.total_price}")
                        Text(text = "Lấy: ${order.pickup_address}")
                        Text(text = "Giao: ${order.delivery_address}")
                    }
                }
            }
        }
    }
}