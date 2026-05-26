package com.example.dacsiii_v2.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.dacsiii_v2.ui.common.EmptyState
import com.example.dacsiii_v2.ui.common.InfoRow
import com.example.dacsiii_v2.ui.common.OrderStatusTimeline
import com.example.dacsiii_v2.ui.common.SectionCard
import com.example.dacsiii_v2.ui.common.StatusPill

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerOrderDetailScreen(
    token: String,
    orderId: Int,
    viewModel: CustomerViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(token, orderId) {
        if (token.isNotBlank() && orderId > 0) {
            viewModel.fetchOrderDetails(token, orderId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết đơn hàng") },
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
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val order = uiState.orderDetail
            if (uiState.isOrderDetailLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                return@Column
            }

            if (order == null) {
                EmptyState(message = "Không có dữ liệu đơn hàng.")
                return@Column
            }

            SectionCard(title = "Tổng quan đơn hàng") {
                Text(text = "Đơn #${order.order_id}")
                StatusPill(
                    text = order.status ?: "-",
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
                InfoRow(label = "Tổng tiền", value = "${order.total_price}")
                InfoRow(label = "Khoảng cách", value = "${order.distance_km ?: "-"} km")
            }

            SectionCard(title = "Trạng thái đơn") {
                OrderStatusTimeline(status = order.status)
            }

            SectionCard(title = "Địa chỉ") {
                Text(text = "Lấy: ${order.pickup_address}")
                Text(text = "Giao: ${order.delivery_address}")
            }

            SectionCard(title = "Người gửi / Người nhận") {
                InfoRow(label = "Người gửi", value = order.sender_name)
                InfoRow(label = "SĐT người gửi", value = order.sender_phone)
                if (!order.pickup_note.isNullOrBlank()) {
                    InfoRow(label = "Ghi chú lấy", value = order.pickup_note ?: "")
                }
                InfoRow(label = "Người nhận", value = order.recipient_name)
                InfoRow(label = "SĐT người nhận", value = order.recipient_phone)
                if (!order.delivery_note.isNullOrBlank()) {
                    InfoRow(label = "Ghi chú giao", value = order.delivery_note ?: "")
                }
            }

            SectionCard(title = "Kiện hàng") {
                InfoRow(label = "Loại hàng", value = order.package_type)
                InfoRow(label = "Kích cỡ", value = order.package_size)
                InfoRow(label = "Khối lượng", value = "${order.weight_kg} kg")
                if (!order.order_description.isNullOrBlank()) {
                    InfoRow(label = "Mô tả", value = order.order_description ?: "")
                }
                InfoRow(label = "Thu hộ", value = "${order.cod_amount ?: 0.0}")
            }

            SectionCard(title = "Thanh toán") {
                val paymentLabel = if (order.payment_method == "recipient_cash") {
                    "Người nhận trả tiền mặt"
                } else {
                    "Người gửi trả tiền mặt"
                }
                Text(text = paymentLabel)
            }

            SectionCard(title = "Ảnh đơn hàng") {
                AsyncImage(
                    model = normalizePhotoUrl(order.photo_before_booking),
                    contentDescription = "Order photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
            }

            SectionCard(title = "Tài xế") {
                val driver = uiState.driverDetail
                if (driver == null) {
                    Text("Chưa có tài xế nhận đơn.")
                } else {
                    InfoRow(label = "Tên", value = driver.full_name ?: "-")
                    InfoRow(label = "SĐT", value = driver.phone ?: "-")
                    InfoRow(label = "Xe", value = driver.vehicle_type ?: "-")
                    InfoRow(label = "Biển số", value = driver.license_plate ?: "-")
                    InfoRow(label = "Đánh giá", value = driver.rating_avg?.toString() ?: "-")
                }
            }
        }
    }
}

private fun normalizePhotoUrl(photoUrl: String): String {
    return if (photoUrl.startsWith("http")) {
        photoUrl
    } else {
        "http://10.0.2.2:3000$photoUrl"
    }
}
