package com.example.dacsiii_v2.ui.driver

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.dacsiii_v2.ui.common.IosPrimaryButton
import com.example.dacsiii_v2.ui.common.SectionCard
import com.example.dacsiii_v2.ui.common.StatusPill
import java.io.File
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverOrderDetailScreen(
    token: String,
    orderId: Int,
    viewModel: DriverViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pickupPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var deliveryPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var showPhotoPicker by remember { mutableStateOf(false) }
    var photoTarget by remember { mutableStateOf("pickup") }

    val pickupCameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pickupPhotoUri
        if (success && uri != null) {
            viewModel.uploadPickupPhoto(token, context, orderId, uri)
        }
    }

    val deliveryCameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = deliveryPhotoUri
        if (success && uri != null) {
            viewModel.uploadDeliveryPhoto(token, context, orderId, uri)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            if (photoTarget == "pickup") {
                viewModel.uploadPickupPhoto(token, context, orderId, uri)
            } else {
                viewModel.uploadDeliveryPhoto(token, context, orderId, uri)
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createCameraImageUri(context, photoTarget)
            if (photoTarget == "pickup") {
                pickupPhotoUri = uri
                pickupCameraLauncher.launch(uri)
            } else {
                deliveryPhotoUri = uri
                deliveryCameraLauncher.launch(uri)
            }
        } else {
            scope.launch {
                val message = if (photoTarget == "pickup") {
                    "Cần quyền camera để chụp ảnh nhận hàng"
                } else {
                    "Cần quyền camera để chụp ảnh giao hàng"
                }
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    LaunchedEffect(token, orderId) {
        if (token.isNotBlank() && orderId > 0) {
            viewModel.fetchOrderDetails(token, orderId)
        }
    }

    LaunchedEffect(uiState.message) {
        val message = uiState.message
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    val order = uiState.orderDetail

    val earningLabel = order?.driver_earning?.let { formatCurrency(it) } ?: "0đ"
    val advanceLabel = order?.cod_amount?.let { formatCurrency(it) }
    val distanceLabel = order?.distance_km?.let { formatDistance(it) }

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
        "accepted"           -> "Đã nhận đơn"
        "picking_up"         -> "Đang lấy hàng"
        "delivering"         -> "Đang giao"
        "arrived_delivery"   -> "Tới nơi giao"
        "completed"          -> "Hoàn thành"
        "cancelled", "canceled" -> "Đã hủy"
        else                 -> status ?: "-"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Chi tiết đơn hàng",
                        fontWeight = FontWeight.Bold
                    )
                },
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (order == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (uiState.isOrderDetailLoading) {
                        CircularProgressIndicator()
                    } else {
                        Text(
                            "Không có dữ liệu đơn hàng.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // ── Order Header Banner ────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF0D1B4B), Color(0xFF1A237E), Color(0xFF283593))
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
                                        text = "Chi tiết hành trình giao hàng",
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

                            // Earnings row
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
                                    text = "Thu nhập đơn này",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = earningLabel,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFD54F),
                                    fontSize = 20.sp
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Quãng đường",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = distanceLabel ?: "-",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Tạm ứng",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = advanceLabel ?: "0đ",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        // ── Route Card ─────────────────────────────────────────
                        SectionCard(title = "Tuyến đường") {
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

                        // ── Order Photos ───────────────────────────────────────
                        SectionCard(title = "Ảnh đơn hàng") {
                            // Photo before booking
                            if (!order.photo_before_booking.isNullOrBlank()) {
                                OrderPhotoItem(
                                    label = "Ảnh hàng hóa",
                                    icon = Icons.Default.Photo,
                                    iconTint = Color(0xFF1565C0),
                                    url = normalizePhotoUrl(order.photo_before_booking)
                                )
                            }

                            // Photo at pickup
                            if (!order.photo_at_pickup.isNullOrBlank()) {
                                if (!order.photo_before_booking.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                                OrderPhotoItem(
                                    label = "Ảnh nhận hàng",
                                    icon = Icons.Default.LocalShipping,
                                    iconTint = Color(0xFFE65100),
                                    url = normalizePhotoUrl(order.photo_at_pickup)
                                )
                            }

                            // Photo at delivery
                            if (!order.photo_at_delivery.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OrderPhotoItem(
                                    label = "Ảnh giao hàng",
                                    icon = Icons.Default.CheckCircle,
                                    iconTint = Color(0xFF2E7D32),
                                    url = normalizePhotoUrl(order.photo_at_delivery)
                                )
                            }

                            // Empty state
                            if (order.photo_before_booking.isNullOrBlank()
                                && order.photo_at_pickup.isNullOrBlank()
                                && order.photo_at_delivery.isNullOrBlank()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .padding(20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Default.Camera,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "Chưa có ảnh đơn hàng",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }

                        // ── Action Button ──────────────────────────────────────
                        when (order.status) {
                            "accepted" -> {
                                ActionStepCard(
                                    stepLabel = "Bước 1",
                                    title = "Di chuyển đến điểm lấy hàng",
                                    description = "Đến địa điểm lấy hàng và nhấn xác nhận khi đến nơi.",
                                    accentColor = Color(0xFF1565C0),
                                    buttonText = "Xác nhận đến điểm lấy",
                                    onAction = { viewModel.arrivePickup(token, order.order_id) }
                                )
                            }
                            "picking_up" -> {
                                ActionStepCard(
                                    stepLabel = "Bước 2",
                                    title = "Nhận hàng từ người gửi",
                                    description = "Chụp ảnh hàng hóa sau khi nhận để xác nhận.",
                                    accentColor = Color(0xFFE65100),
                                    buttonText = "Chụp ảnh & Nhận hàng",
                                    onAction = {
                                        photoTarget = "pickup"
                                        showPhotoPicker = true
                                    }
                                )
                            }
                            "delivering" -> {
                                ActionStepCard(
                                    stepLabel = "Bước 3",
                                    title = "Giao hàng đến người nhận",
                                    description = "Di chuyển đến địa điểm giao hàng và xác nhận đã tới.",
                                    accentColor = Color(0xFF6A1B9A),
                                    buttonText = "Xác nhận đến điểm giao",
                                    onAction = { viewModel.arriveDelivery(token, order.order_id) }
                                )
                            }
                            "arrived_delivery" -> {
                                ActionStepCard(
                                    stepLabel = "Bước 4",
                                    title = "Hoàn thành giao hàng",
                                    description = "Chụp ảnh bàn giao hàng để hoàn tất đơn.",
                                    accentColor = Color(0xFF2E7D32),
                                    buttonText = "Chụp ảnh & Hoàn thành",
                                    onAction = {
                                        photoTarget = "delivery"
                                        showPhotoPicker = true
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

            // Loading overlay
            if (uiState.isPhotoUploading || uiState.isOrderDetailLoading) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.6f)
                ) {}
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(48.dp)
                )
            }
        }
    }

    // Photo picker dialog
    if (showPhotoPicker) {
        AlertDialog(
            onDismissRequest = { showPhotoPicker = false },
            title = {
                Text("Chụp ảnh xác nhận", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Bạn muốn chụp ảnh mới hay chọn từ thư viện?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showPhotoPicker = false
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasPermission) {
                        val uri = createCameraImageUri(context, photoTarget)
                        if (photoTarget == "pickup") {
                            pickupPhotoUri = uri
                            pickupCameraLauncher.launch(uri)
                        } else {
                            deliveryPhotoUri = uri
                            deliveryCameraLauncher.launch(uri)
                        }
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }) {
                    Text(" Chụp ảnh", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPhotoPicker = false
                    galleryLauncher.launch("image/*")
                }) {
                    Text("️ Thư viện")
                }
            }
        )
    }
}

@Composable
private fun OrderPhotoItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    url: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        AsyncImage(
            model = url,
            contentDescription = label,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, iconTint.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun ActionStepCard(
    stepLabel: String,
    title: String,
    description: String,
    accentColor: Color,
    buttonText: String,
    onAction: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp), ambientColor = accentColor.copy(alpha = 0.3f))
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(accentColor.copy(alpha = 0.08f), accentColor.copy(alpha = 0.03f))
                )
            )
            .border(1.5.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = stepLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            IosPrimaryButton(
                text = buttonText,
                onClick = onAction,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun formatCurrency(value: Double): String {
    return String.format(java.util.Locale.US, "%,.0fđ", value).replace(',', '.')
}

private fun formatDistance(value: Double): String {
    return String.format(java.util.Locale.US, "%.2f km", value)
}

private fun normalizePhotoUrl(photoUrl: String?): String {
    if (photoUrl.isNullOrBlank()) return ""
    return if (photoUrl.startsWith("http")) {
        photoUrl
    } else {
        "http://10.0.2.2:3000$photoUrl"
    }
}

private fun createCameraImageUri(context: Context, prefix: String): Uri {
    val file = File(context.cacheDir, "${prefix}_photo_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
