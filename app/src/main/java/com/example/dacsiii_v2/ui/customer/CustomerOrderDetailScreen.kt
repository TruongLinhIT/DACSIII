package com.example.dacsiii_v2.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    var showReportDialog by remember { mutableStateOf(false) }
    var selectedReason by remember { mutableStateOf("Thái độ tài xế không tốt") }
    var reportDescription by remember { mutableStateOf("") }
    val reasons = listOf(
        "Tài xế tự ý lấy hàng không giao cho khách",
        "Thái độ tài xế không tốt",
        "Không thể liên lạc được với tài xế",
        "Tài xế yêu cầu trả thêm tiền mặt ngoài app",
        "Khác"
    )

    LaunchedEffect(token, orderId) {
        if (token.isNotBlank() && orderId > 0) {
            viewModel.fetchOrderDetails(token, orderId)
        }
    }

    val order = uiState.orderDetail

    // Status color helpers
    fun statusColor(status: String?): Color = when (status?.lowercase()) {
        "accepted"           -> Color(0xFF1565C0)
        "picking_up"         -> Color(0xFFE65100)
        "delivering"         -> Color(0xFF6A1B9A)
        "arrived_delivery"   -> Color(0xFF2E7D32)
        "completed"          -> Color(0xFF1B5E20)
        "cancelled", "canceled" -> Color(0xFFB71C1C)
        else                 -> Color(0xFF37474F)
    }

    fun statusLabel(status: String?): String = when (status?.lowercase()) {
        "accepted"           -> "Đã tiếp nhận"
        "picking_up"         -> "Đang lấy hàng"
        "delivering"         -> "Đang giao hàng"
        "arrived_delivery"   -> "Đã đến điểm giao"
        "completed"          -> "Giao thành công"
        "cancelled", "canceled" -> "Đã hủy đơn"
        else                 -> status ?: "-"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết đơn hàng", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (order == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (uiState.isOrderDetailLoading) {
                        CircularProgressIndicator()
                    } else {
                        EmptyState(message = "Không có dữ liệu đơn hàng.")
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    // ── Order Header Banner ───────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF0F2027),
                                        Color(0xFF203A43),
                                        Color(0xFF2C5364)
                                    )
                                )
                            )
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Đơn #${order.order_id}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Cước phí & Thông tin hành trình",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                                StatusPill(
                                    text = statusLabel(order.status),
                                    containerColor = statusColor(order.status),
                                    contentColor = Color.White
                                )
                            }

                            // Price banner
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White.copy(alpha = 0.1f))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.AttachMoney,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD54F),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Tổng cước thanh toán",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "${order.total_price} ₫",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFD54F),
                                    fontSize = 20.sp
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // ── Journey Card ──────────────────────────────────────
                        SectionCard(title = "Tuyến đường vận chuyển") {
                            // Pickup point
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .background(Color(0xFF4CAF50), CircleShape)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(36.dp)
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(Color(0xFF4CAF50), Color(0xFFEF5350))
                                                )
                                            )
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "ĐIỂM LẤY",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF4CAF50),
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = order.pickup_address ?: "-",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Delivery point
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .background(Color(0xFFEF5350), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "ĐIỂM GIAO",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFEF5350),
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = order.delivery_address ?: "-",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // ── Order Timeline ────────────────────────────────────
                        SectionCard(title = "Trạng thái đơn") {
                            OrderStatusTimeline(status = order.status)
                        }

                        // ── Contact Detail Card ────────────────────────────────
                        SectionCard(title = "Thông tin liên hệ") {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    "Người gửi",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                InfoRow(label = "Tên", value = order.sender_name)
                                InfoRow(label = "Số điện thoại", value = order.sender_phone)
                                if (!order.pickup_note.isNullOrBlank()) {
                                    InfoRow(label = "Ghi chú lấy hàng", value = order.pickup_note ?: "")
                                }
                                
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    "Người nhận",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEF5350)
                                )
                                InfoRow(label = "Tên", value = order.recipient_name)
                                InfoRow(label = "Số điện thoại", value = order.recipient_phone)
                                if (!order.delivery_note.isNullOrBlank()) {
                                    InfoRow(label = "Ghi chú giao hàng", value = order.delivery_note ?: "")
                                }
                            }
                        }

                        // ── Package details Card ──────────────────────────────
                        SectionCard(title = "Chi tiết kiện hàng") {
                            InfoRow(
                                label = "Danh mục",
                                value = when (order.package_type) {
                                    "electronics" -> "Điện tử"
                                    "food" -> "Đồ ăn"
                                    "bulky" -> "Cồng kềnh"
                                    else -> "Khác"
                                }
                            )
                            InfoRow(label = "Kích cỡ", value = "Size ${order.package_size}")
                            InfoRow(label = "Khối lượng", value = "${order.weight_kg} kg")
                            if (!order.order_description.isNullOrBlank()) {
                                InfoRow(label = "Mô tả hàng hóa", value = order.order_description ?: "")
                            }
                            InfoRow(label = "Giá trị thu hộ (COD)", value = "${order.cod_amount ?: 0.0} ₫")
                        }

                        // ── Payment Card ──────────────────────────────────────
                        SectionCard(title = "Phương thức thanh toán") {
                            val paymentLabel = if (order.payment_method == "recipient_cash") {
                                "Người nhận trả tiền mặt"
                            } else {
                                "Người gửi trả tiền mặt"
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Receipt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = paymentLabel,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // ── Image Cargo Card ──────────────────────────────────
                        SectionCard(title = "Ảnh sản phẩm ký gửi") {
                            if (!order.photo_before_booking.isNullOrBlank()) {
                                AsyncImage(
                                    model = normalizePhotoUrl(order.photo_before_booking),
                                    contentDescription = "Order photo",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Không có hình ảnh",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // ── Driver Details Card ────────────────────────────────
                        SectionCard(title = "Tài xế giao nhận") {
                            val driver = uiState.driverDetail
                            if (driver == null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        "Đang tìm kiếm tài xế phù hợp...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Driver avatar mockup or verification icon
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.width(16.dp))
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = driver.full_name ?: "Tài xế Pro",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Star,
                                                contentDescription = null,
                                                tint = Color(0xFFFFD54F),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${driver.rating_avg ?: 5.0}",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(14.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                )
                                Spacer(modifier = Modifier.height(14.dp))

                                InfoRow(label = "Số điện thoại", value = driver.phone ?: "-")
                                InfoRow(label = "Loại xe", value = driver.vehicle_type ?: "-")
                                InfoRow(label = "Biển số xe", value = driver.license_plate ?: "-")

                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = { showReportDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        Icons.Default.Report,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Báo cáo tài xế này", color = Color.White)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

            if (uiState.isOrderDetailLoading) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.6f)
                ) {}
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // ── Dialog báo cáo tài xế ──
            if (showReportDialog && order != null && order.driver_id != null) {
                AlertDialog(
                    onDismissRequest = { showReportDialog = false },
                    title = {
                        Text(
                            text = "Báo cáo tài xế",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Vui lòng chọn lý do báo cáo và mô tả chi tiết sự việc.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                reasons.forEach { reason ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedReason = reason }
                                            .padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = (selectedReason == reason),
                                            onClick = { selectedReason = reason }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = reason, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = reportDescription,
                                onValueChange = { reportDescription = it },
                                label = { Text("Mô tả chi tiết") },
                                placeholder = { Text("Nhập lý do chi tiết sự việc...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                maxLines = 4
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (reportDescription.trim().length >= 5) {
                                    viewModel.submitReport(
                                        token = token,
                                        orderId = order.order_id,
                                        driverId = order.driver_id,
                                        reasonType = selectedReason,
                                        description = reportDescription,
                                        onSuccess = {
                                            showReportDialog = false
                                            reportDescription = ""
                                        }
                                    )
                                }
                            },
                            enabled = reportDescription.trim().length >= 5 && !uiState.isReportSubmitting,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            if (uiState.isReportSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Gửi báo cáo")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showReportDialog = false }) {
                            Text("Hủy")
                        }
                    }
                )
            }
        }
    }
}

private fun normalizePhotoUrl(photoUrl: String?): String {
    if (photoUrl.isNullOrBlank()) return ""
    return if (photoUrl.startsWith("http")) {
        photoUrl
    } else {
        "http://10.0.2.2:3000$photoUrl"
    }
}
