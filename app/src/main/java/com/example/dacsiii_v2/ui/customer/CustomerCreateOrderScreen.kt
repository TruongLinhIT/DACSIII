package com.example.dacsiii_v2.ui.customer

import android.Manifest
import android.content.Context
import android.location.Geocoder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.dacsiii_v2.data.model.CreateOrderRequest
import com.example.dacsiii_v2.ui.common.IosPrimaryButton
import com.example.dacsiii_v2.ui.common.IosSecondaryButton
import com.example.dacsiii_v2.ui.common.IosTextField
import com.example.dacsiii_v2.ui.common.SectionCard
import java.io.File
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerCreateOrderScreen(
    token: String,
    viewModel: CustomerViewModel,
    onNavigateBack: () -> Unit,
    onNavigatePickPickup: () -> Unit,
    onNavigatePickDelivery: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val packageOptions = listOf("electronics", "food", "bulky", "others")

    var packageType by rememberSaveable { mutableStateOf(packageOptions.first()) }
    var packageExpanded by remember { mutableStateOf(false) }
    var weightKg by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var pickupAddress by rememberSaveable { mutableStateOf("") }
    var deliveryAddress by rememberSaveable { mutableStateOf("") }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var cameraPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var lastUploadedUri by remember { mutableStateOf<Uri?>(null) }
    var localError by remember { mutableStateOf<String?>(null) }
    var pickupFavoriteExpanded by remember { mutableStateOf(false) }
    var deliveryFavoriteExpanded by remember { mutableStateOf(false) }
    var showPickupSaveDialog by remember { mutableStateOf(false) }
    var showDeliverySaveDialog by remember { mutableStateOf(false) }
    var pickupFavoriteLabel by rememberSaveable { mutableStateOf("Nhà") }
    var deliveryFavoriteLabel by rememberSaveable { mutableStateOf("Cơ quan") }
    var pickupLookupMessage by remember { mutableStateOf<String?>(null) }
    var deliveryLookupMessage by remember { mutableStateOf<String?>(null) }
    var lastResolvedPickupAddress by rememberSaveable { mutableStateOf("") }
    var lastResolvedDeliveryAddress by rememberSaveable { mutableStateOf("") }
    var lastResolvedPickupCoords by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var lastResolvedDeliveryCoords by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    var senderName by rememberSaveable { mutableStateOf("") }
    var senderPhone by rememberSaveable { mutableStateOf("") }
    var recipientName by rememberSaveable { mutableStateOf("") }
    var recipientPhone by rememberSaveable { mutableStateOf("") }
    var pickupNote by rememberSaveable { mutableStateOf("") }
    var deliveryNote by rememberSaveable { mutableStateOf("") }
    var packageSize by rememberSaveable { mutableStateOf("S") }
    var codAmountText by rememberSaveable { mutableStateOf("") }
    var paymentMethod by rememberSaveable { mutableStateOf("sender_cash") }
    var showSenderSheet by remember { mutableStateOf(false) }
    var showRecipientSheet by remember { mutableStateOf(false) }
    var showPackageSheet by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedPhotoUri = uri
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedPhotoUri = cameraPhotoUri
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = cameraPhotoUri
            if (uri != null) {
                cameraLauncher.launch(uri)
            }
        } else {
            localError = "Bạn cần cấp quyền Camera để chụp ảnh."
        }
    }

    LaunchedEffect(uiState.message) {
        val message = uiState.message
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(uiState.pickupLat, uiState.pickupLng, uiState.deliveryLat, uiState.deliveryLng, weightKg) {
        val weightValue = weightKg.toDoubleOrNull()
        viewModel.updateEstimate(weightValue)
    }

    LaunchedEffect(selectedPhotoUri) {
        val uri = selectedPhotoUri
        if (uri != null && uri != lastUploadedUri && !uiState.isPhotoUploading) {
            lastUploadedUri = uri
            viewModel.uploadOrderPhotoBefore(token, context, uri)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadFavorites(context)
    }

    LaunchedEffect(pickupAddress) {
        val query = pickupAddress.trim()
        if (query == lastResolvedPickupAddress.trim()) {
            return@LaunchedEffect
        }
        if (query.length < 6) {
            pickupLookupMessage = null
            return@LaunchedEffect
        }
        delay(700)
        val point = geocodeAddress(context, query)
        if (point != null) {
            viewModel.setPickupLocation(point.first, point.second)
            lastResolvedPickupAddress = query
            lastResolvedPickupCoords = point
            pickupLookupMessage = "Đã định vị địa chỉ trên bản đồ."
        } else {
            pickupLookupMessage = "Không tìm thấy địa chỉ này."
        }
    }

    LaunchedEffect(deliveryAddress) {
        val query = deliveryAddress.trim()
        if (query == lastResolvedDeliveryAddress.trim()) {
            return@LaunchedEffect
        }
        if (query.length < 6) {
            deliveryLookupMessage = null
            return@LaunchedEffect
        }
        delay(700)
        val point = geocodeAddress(context, query)
        if (point != null) {
            viewModel.setDeliveryLocation(point.first, point.second)
            lastResolvedDeliveryAddress = query
            lastResolvedDeliveryCoords = point
            deliveryLookupMessage = "Đã định vị địa chỉ trên bản đồ."
        } else {
            deliveryLookupMessage = "Không tìm thấy địa chỉ này."
        }
    }

    LaunchedEffect(uiState.pickupLat, uiState.pickupLng) {
        val lat = uiState.pickupLat
        val lng = uiState.pickupLng
        if (lat != null && lng != null) {
            val lastCoords = lastResolvedPickupCoords
            if (lastCoords == null || lastCoords.first != lat || lastCoords.second != lng) {
                val addr = reverseGeocodeAddress(context, lat, lng)
                if (addr != null) {
                    pickupAddress = addr
                    lastResolvedPickupAddress = addr
                } else {
                    pickupAddress = "$lat, $lng"
                    lastResolvedPickupAddress = "$lat, $lng"
                }
                lastResolvedPickupCoords = lat to lng
            }
        }
    }

    LaunchedEffect(uiState.deliveryLat, uiState.deliveryLng) {
        val lat = uiState.deliveryLat
        val lng = uiState.deliveryLng
        if (lat != null && lng != null) {
            val lastCoords = lastResolvedDeliveryCoords
            if (lastCoords == null || lastCoords.first != lat || lastCoords.second != lng) {
                val addr = reverseGeocodeAddress(context, lat, lng)
                if (addr != null) {
                    deliveryAddress = addr
                    lastResolvedDeliveryAddress = addr
                } else {
                    deliveryAddress = "$lat, $lng"
                    lastResolvedDeliveryAddress = "$lat, $lng"
                }
                lastResolvedDeliveryCoords = lat to lng
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tạo đơn hàng mới", fontWeight = FontWeight.Bold) },
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
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val senderSummary = if (senderName.isBlank() && senderPhone.isBlank()) {
                    "Chạm để cấu hình người gửi"
                } else {
                    listOf(senderName, senderPhone).filter { it.isNotBlank() }.joinToString(" · ")
                }
                val recipientSummary = if (recipientName.isBlank() && recipientPhone.isBlank()) {
                    "Chạm để cấu hình người nhận"
                } else {
                    listOf(recipientName, recipientPhone).filter { it.isNotBlank() }.joinToString(" · ")
                }
                val packageSummary = listOf(
                    "Kích thước $packageSize",
                    if (weightKg.isBlank()) "Chọn khối lượng" else "$weightKg kg",
                    when (packageType) {
                        "electronics" -> "Điện tử"
                        "food" -> "Đồ ăn"
                        "bulky" -> "Cồng kềnh"
                        else -> "Loại khác"
                    }
                ).joinToString(" · ")

                // ── Route & Contact Card ─────────────────────────────────────
                SectionCard(title = "Hành trình & Liên hệ") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Pickup block
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .clickable {
                                    viewModel.loadFavorites(context)
                                    showSenderSheet = true
                                }
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(Color(0xFF4CAF50), CircleShape)
                                )
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(30.dp)
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color(0xFF4CAF50), Color(0xFFEF5350))
                                            )
                                        )
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ĐIỂM LẤY HÀNG",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF4CAF50),
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = pickupAddress.ifBlank { "Nhập địa chỉ lấy hàng" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = senderSummary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        // Delivery block
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .clickable {
                                    viewModel.loadFavorites(context)
                                    showRecipientSheet = true
                                }
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(Color(0xFFEF5350), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ĐIỂM GIAO HÀNG",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFEF5350),
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = deliveryAddress.ifBlank { "Nhập địa chỉ giao hàng" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = recipientSummary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // ── Package Card ──────────────────────────────────────────────
                SectionCard(title = "Kiện hàng & COD") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .clickable { showPackageSheet = true }
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocalShipping, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "THÔNG TIN HÀNG HÓA",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = packageSummary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (description.isNotBlank()) {
                                Text(
                                    text = "Ghi chú: $description",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (codAmountText.isNotBlank() && (codAmountText.toDoubleOrNull() ?: 0.0) > 0.0) {
                                Text(
                                    text = "Thu hộ COD: $codAmountText ₫",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFEF5350),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // ── Payment Mode ──────────────────────────────────────────────
                SectionCard(title = "Hình thức thanh toán") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PaymentOptionRow(
                            selected = paymentMethod == "sender_cash",
                            title = "Người gửi trả tiền mặt",
                            onClick = { paymentMethod = "sender_cash" }
                        )
                        PaymentOptionRow(
                            selected = paymentMethod == "recipient_cash",
                            title = "Người nhận trả tiền mặt",
                            onClick = { paymentMethod = "recipient_cash" }
                        )
                    }
                }

                // ── Estimates Card ────────────────────────────────────────────
                val distanceText = uiState.distanceKm?.let { String.format(Locale.US, "%.2f", it) } ?: "-"
                val totalPriceText = uiState.totalPrice?.let { String.format(Locale.US, "%.0f", it) } ?: "-"
                
                SectionCard(title = "Tính toán chi phí") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Khoảng cách",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$distanceText km",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Ước tính cước phí",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$totalPriceText ₫",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // ── Photos upload Card ─────────────────────────────────────────
                SectionCard(title = "Hình ảnh hàng hóa") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IosSecondaryButton(
                            onClick = {
                                val uri = createCameraImageUri(context)
                                cameraPhotoUri = uri
                                val permission = Manifest.permission.CAMERA
                                val granted = ContextCompat.checkSelfPermission(context, permission) ==
                                    android.content.pm.PackageManager.PERMISSION_GRANTED
                                if (granted) {
                                    cameraLauncher.launch(uri)
                                } else {
                                    cameraPermissionLauncher.launch(permission)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            text = "📷 Chụp ảnh"
                        )
                        IosSecondaryButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            text = "🖼️ Chọn ảnh"
                        )
                    }

                    selectedPhotoUri?.let { uri ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        ) {
                            AsyncImage(
                                model = uri,
                                contentDescription = "Order photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    val photoUploaded = !uiState.orderPhotoUrl.isNullOrBlank()
                    val uploadStatus = when {
                        uiState.isPhotoUploading -> "⏳ Đang tải ảnh lên máy chủ..."
                        photoUploaded -> "✅ Ảnh hàng hóa đã được tải lên thành công."
                        else -> "⚠️ Vui lòng tải lên ảnh hàng hóa trước khi tạo đơn."
                    }
                    Text(
                        text = uploadStatus,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (photoUploaded) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (!localError.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = localError ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // ── Submit Button ─────────────────────────────────────────────
                IosPrimaryButton(
                    onClick = {
                        val errors = mutableListOf<String>()

                        val weightValue = weightKg.toDoubleOrNull().also {
                            if (it == null) errors.add("Khối lượng không hợp lệ.")
                        }

                        if (uiState.pickupLat == null || uiState.pickupLng == null) {
                            errors.add("Vui lòng chọn vị trí lấy hàng trên bản đồ.")
                        }
                        if (uiState.deliveryLat == null || uiState.deliveryLng == null) {
                            errors.add("Vui lòng chọn vị trí giao hàng trên bản đồ.")
                        }

                        if (pickupAddress.isBlank()) errors.add("Vui lòng nhập địa chỉ lấy hàng.")
                        if (deliveryAddress.isBlank()) errors.add("Vui lòng nhập địa chỉ giao hàng.")
                        if (uiState.orderPhotoUrl.isNullOrBlank()) errors.add("Vui lòng upload ảnh trước khi đặt.")
                        if (uiState.distanceKm == null || uiState.totalPrice == null) errors.add("Chưa thể tính khoảng cách và tổng tiền.")
                        if (senderName.isBlank()) errors.add("Vui lòng nhập tên người gửi.")
                        if (senderPhone.isBlank()) errors.add("Vui lòng nhập SĐT người gửi.")
                        if (recipientName.isBlank()) errors.add("Vui lòng nhập tên người nhận.")
                        if (recipientPhone.isBlank()) errors.add("Vui lòng nhập SĐT người nhận.")
                        if (packageSize.isBlank()) errors.add("Vui lòng chọn kích cỡ kiện hàng.")
                        if (paymentMethod.isBlank()) errors.add("Vui lòng chọn phương thức thanh toán.")

                        if (errors.isNotEmpty()) {
                            localError = errors.joinToString("\n")
                            return@IosPrimaryButton
                        }

                        localError = null
                        val request = CreateOrderRequest(
                            package_type = packageType,
                            weight_kg = weightValue ?: 0.0,
                            order_description = description.takeIf { it.isNotBlank() },
                            pickup_address = pickupAddress,
                            delivery_address = deliveryAddress,
                            pickup_lat = uiState.pickupLat ?: 0.0,
                            pickup_lng = uiState.pickupLng ?: 0.0,
                            delivery_lat = uiState.deliveryLat ?: 0.0,
                            delivery_lng = uiState.deliveryLng ?: 0.0,
                            distance_km = uiState.distanceKm,
                            total_price = uiState.totalPrice ?: 0.0,
                            photo_before_booking = uiState.orderPhotoUrl ?: "",
                            sender_name = senderName.trim(),
                            sender_phone = senderPhone.trim(),
                            recipient_name = recipientName.trim(),
                            recipient_phone = recipientPhone.trim(),
                            pickup_note = pickupNote.takeIf { it.isNotBlank() },
                            delivery_note = deliveryNote.takeIf { it.isNotBlank() },
                            package_size = packageSize,
                            cod_amount = codAmountText.toDoubleOrNull() ?: 0.0,
                            payment_method = paymentMethod
                        )

                        viewModel.createOrder(token, request) {
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    text = "Tạo đơn hàng mới"
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            if (uiState.isLoading) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.6f)
                ) {}
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }

    // ── Dialogs ──────────────────────────────────────────────────────────────
    if (showPickupSaveDialog) {
        AlertDialog(
            onDismissRequest = { showPickupSaveDialog = false },
            title = { Text("Lưu địa chỉ lấy hàng", fontWeight = FontWeight.Bold) },
            text = {
                IosTextField(
                    value = pickupFavoriteLabel,
                    onValueChange = { pickupFavoriteLabel = it },
                    label = { Text("Gợi nhớ (VD: Nhà, Cơ quan...)") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val lat = uiState.pickupLat
                        val lng = uiState.pickupLng
                        if (lat != null && lng != null) {
                            viewModel.addFavorite(context, pickupFavoriteLabel, pickupAddress, lat, lng)
                        }
                        showPickupSaveDialog = false
                    }
                ) {
                    Text("Lưu lại", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPickupSaveDialog = false }) {
                    Text("Hủy bỏ")
                }
            }
        )
    }

    if (showDeliverySaveDialog) {
        AlertDialog(
            onDismissRequest = { showDeliverySaveDialog = false },
            title = { Text("Lưu địa chỉ giao hàng", fontWeight = FontWeight.Bold) },
            text = {
                IosTextField(
                    value = deliveryFavoriteLabel,
                    onValueChange = { deliveryFavoriteLabel = it },
                    label = { Text("Gợi nhớ (VD: Cơ quan, Bạn bè...)") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val lat = uiState.deliveryLat
                        val lng = uiState.deliveryLng
                        if (lat != null && lng != null) {
                            viewModel.addFavorite(context, deliveryFavoriteLabel, deliveryAddress, lat, lng)
                        }
                        showDeliverySaveDialog = false
                    }
                ) {
                    Text("Lưu lại", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeliverySaveDialog = false }) {
                    Text("Hủy bỏ")
                }
            }
        )
    }

    // ── Bottom Sheets ────────────────────────────────────────────────────────
    if (showSenderSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showSenderSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Cấu hình Người gửi", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                
                val favorites = uiState.favoriteAddresses
                if (favorites.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        IosTextField(
                            value = "Chọn địa chỉ yêu thích",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { pickupFavoriteExpanded = true }
                        )
                        DropdownMenu(
                            expanded = pickupFavoriteExpanded,
                            onDismissRequest = { pickupFavoriteExpanded = false }
                        ) {
                            favorites.forEach { favorite ->
                                DropdownMenuItem(
                                    text = { Text("${favorite.label}: ${favorite.address}") },
                                    onClick = {
                                        pickupAddress = favorite.address
                                        lastResolvedPickupAddress = favorite.address
                                        lastResolvedPickupCoords = favorite.lat to favorite.lng
                                        viewModel.setPickupLocation(favorite.lat, favorite.lng)
                                        pickupFavoriteExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                IosTextField(
                    value = pickupAddress,
                    onValueChange = { pickupAddress = it },
                    label = { Text("Địa chỉ lấy hàng") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (!pickupLookupMessage.isNullOrBlank()) {
                    Text(
                        text = pickupLookupMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                IosTextField(
                    value = pickupNote,
                    onValueChange = { pickupNote = it },
                    label = { Text("Ghi chú lấy (VD: Số nhà, Tầng...)") },
                    modifier = Modifier.fillMaxWidth()
                )
                IosTextField(
                    value = senderName,
                    onValueChange = { senderName = it },
                    label = { Text("Tên người gửi") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                IosTextField(
                    value = senderPhone,
                    onValueChange = { senderPhone = it },
                    label = { Text("Số điện thoại người gửi") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IosSecondaryButton(
                        onClick = {
                            showSenderSheet = false
                            onNavigatePickPickup()
                        },
                        modifier = Modifier.weight(1f),
                        text = "🗺️ Bản đồ"
                    )
                    IosSecondaryButton(
                        onClick = {
                            if (pickupAddress.isBlank()) {
                                localError = "Vui lòng nhập địa chỉ lấy hàng trước khi lưu."
                                return@IosSecondaryButton
                            }
                            if (uiState.pickupLat == null || uiState.pickupLng == null) {
                                localError = "Vui lòng định vị địa chỉ lấy hàng trước khi lưu."
                                return@IosSecondaryButton
                            }
                            localError = null
                            showPickupSaveDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        text = "⭐ Lưu địa chỉ"
                    )
                }

                IosPrimaryButton(
                    onClick = { showSenderSheet = false },
                    modifier = Modifier.fillMaxWidth(),
                    text = "Xác nhận thông tin người gửi"
                )
            }
        }
    }

    if (showRecipientSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showRecipientSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Cấu hình Người nhận", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                
                val favorites = uiState.favoriteAddresses
                if (favorites.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        IosTextField(
                            value = "Chọn địa chỉ yêu thích",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { deliveryFavoriteExpanded = true }
                        )
                        DropdownMenu(
                            expanded = deliveryFavoriteExpanded,
                            onDismissRequest = { deliveryFavoriteExpanded = false }
                        ) {
                            favorites.forEach { favorite ->
                                DropdownMenuItem(
                                    text = { Text("${favorite.label}: ${favorite.address}") },
                                    onClick = {
                                        deliveryAddress = favorite.address
                                        lastResolvedDeliveryAddress = favorite.address
                                        lastResolvedDeliveryCoords = favorite.lat to favorite.lng
                                        viewModel.setDeliveryLocation(favorite.lat, favorite.lng)
                                        deliveryFavoriteExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                IosTextField(
                    value = deliveryAddress,
                    onValueChange = { deliveryAddress = it },
                    label = { Text("Địa chỉ giao hàng") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (!deliveryLookupMessage.isNullOrBlank()) {
                    Text(
                        text = deliveryLookupMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                IosTextField(
                    value = deliveryNote,
                    onValueChange = { deliveryNote = it },
                    label = { Text("Ghi chú giao (VD: Số nhà, Cửa hàng...)") },
                    modifier = Modifier.fillMaxWidth()
                )
                IosTextField(
                    value = recipientName,
                    onValueChange = { recipientName = it },
                    label = { Text("Tên người nhận") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                IosTextField(
                    value = recipientPhone,
                    onValueChange = { recipientPhone = it },
                    label = { Text("Số điện thoại người nhận") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IosSecondaryButton(
                        onClick = {
                            showRecipientSheet = false
                            onNavigatePickDelivery()
                        },
                        modifier = Modifier.weight(1f),
                        text = "🗺️ Bản đồ"
                    )
                    IosSecondaryButton(
                        onClick = {
                            if (deliveryAddress.isBlank()) {
                                localError = "Vui lòng nhập địa chỉ giao hàng trước khi lưu."
                                return@IosSecondaryButton
                            }
                            if (uiState.deliveryLat == null || uiState.deliveryLng == null) {
                                localError = "Vui lòng định vị địa chỉ giao hàng trước khi lưu."
                                return@IosSecondaryButton
                            }
                            localError = null
                            showDeliverySaveDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        text = "⭐ Lưu địa chỉ"
                    )
                }

                IosPrimaryButton(
                    onClick = { showRecipientSheet = false },
                    modifier = Modifier.fillMaxWidth(),
                    text = "Xác nhận thông tin người nhận"
                )
            }
        }
    }

    if (showPackageSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showPackageSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Thông tin Kiện hàng", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                
                Text("Kích cỡ hàng hóa", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("S", "M", "L").forEach { size ->
                        val isSelected = packageSize == size
                        Button(
                            onClick = { packageSize = size },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(
                                text = when (size) {
                                    "S" -> "Nhỏ (S)"
                                    "M" -> "Vừa (M)"
                                    else -> "Lớn (L)"
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Text("Gợi ý khối lượng nhanh", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val weights = listOf("≤ 5kg" to 5.0, "≤ 10kg" to 10.0, "≤ 30kg" to 30.0)
                    weights.forEach { (label, value) ->
                        val isSelected = weightKg == value.toString()
                        Button(
                            onClick = { weightKg = value.toString() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(label, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                IosTextField(
                    value = weightKg,
                    onValueChange = { weightKg = it },
                    label = { Text("Khối lượng chính xác (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    IosTextField(
                        value = when (packageType) {
                            "electronics" -> "Điện tử"
                            "food" -> "Đồ ăn"
                            "bulky" -> "Cồng kềnh"
                            else -> "Danh mục khác"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Danh mục hàng hóa") },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { packageExpanded = true }
                    )
                    DropdownMenu(
                        expanded = packageExpanded,
                        onDismissRequest = { packageExpanded = false }
                    ) {
                        packageOptions.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        when (option) {
                                            "electronics" -> "Điện tử"
                                            "food" -> "Đồ ăn"
                                            "bulky" -> "Cồng kềnh"
                                            else -> "Danh mục khác"
                                        }
                                    )
                                },
                                onClick = {
                                    packageType = option
                                    packageExpanded = false
                                }
                            )
                        }
                    }
                }

                IosTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Mô tả hàng hóa (VD: Hàng dễ vỡ...)") },
                    modifier = Modifier.fillMaxWidth()
                )

                IosTextField(
                    value = codAmountText,
                    onValueChange = { codAmountText = it },
                    label = { Text("Số tiền cần thu hộ (COD) - ₫") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                IosPrimaryButton(
                    onClick = { showPackageSheet = false },
                    modifier = Modifier.fillMaxWidth(),
                    text = "Xác nhận thông tin kiện hàng"
                )
            }
        }
    }
}

@Composable
private fun PaymentOptionRow(
    selected: Boolean,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
            .clickable { onClick() }
            .border(
                1.dp,
                if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun createCameraImageUri(context: Context): Uri {
    val file = File(context.cacheDir, "order_photo_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

private suspend fun geocodeAddress(context: Context, address: String): Pair<Double, Double>? {
    return withContext(Dispatchers.IO) {
        if (!Geocoder.isPresent()) {
            return@withContext null
        }
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val results = geocoder.getFromLocationName(address, 1)
            val location = results?.firstOrNull() ?: return@withContext null
            location.latitude to location.longitude
        } catch (e: Exception) {
            null
        }
    }
}

private suspend fun reverseGeocodeAddress(context: Context, lat: Double, lng: Double): String? {
    return withContext(Dispatchers.IO) {
        if (!Geocoder.isPresent()) {
            return@withContext null
        }
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val results = geocoder.getFromLocation(lat, lng, 1)
            val addressObj = results?.firstOrNull() ?: return@withContext null
            addressObj.getAddressLine(0)
        } catch (e: Exception) {
            null
        }
    }
}
