package com.example.dacsiii_v2.ui.customer

import android.Manifest
import android.content.Context
import android.location.Geocoder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.dacsiii_v2.data.model.CreateOrderRequest
import androidx.compose.runtime.collectAsState
import java.io.File
import java.util.Locale
import com.example.dacsiii_v2.ui.common.SectionCard
import com.example.dacsiii_v2.ui.common.IosPrimaryButton
import com.example.dacsiii_v2.ui.common.IosSecondaryButton
import com.example.dacsiii_v2.ui.common.IosTextField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState

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
                title = { Text("Tạo đơn hàng") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
                    titleContentColor = androidx.compose.material3.MaterialTheme.colorScheme.onBackground
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val senderSummary = if (senderName.isBlank() && senderPhone.isBlank()) {
                "Chạm để thêm thông tin người gửi"
            } else {
                listOf(senderName, senderPhone).filter { it.isNotBlank() }.joinToString(" · ")
            }
            val recipientSummary = if (recipientName.isBlank() && recipientPhone.isBlank()) {
                "Chạm để thêm thông tin người nhận"
            } else {
                listOf(recipientName, recipientPhone).filter { it.isNotBlank() }.joinToString(" · ")
            }
            val packageSummary = listOf(
                "Size $packageSize",
                if (weightKg.isBlank()) "Chọn khối lượng" else "$weightKg kg",
                packageType
            ).joinToString(" · ")

            SectionCard(title = "Thông tin giao nhận") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.loadFavorites(context)
                            showSenderSheet = true
                        },
                    colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Place, contentDescription = null)
                        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(text = pickupAddress.ifBlank { "Địa chỉ lấy hàng" })
                            Text(text = senderSummary, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    }
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.loadFavorites(context)
                            showRecipientSheet = true
                        },
                    colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(text = deliveryAddress.ifBlank { "Địa chỉ giao hàng" })
                            Text(text = recipientSummary, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    }
                }
            }

            SectionCard(title = "Thông tin kiện hàng") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPackageSheet = true },
                    colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Thêm thông tin kiện hàng*")
                            Text(text = packageSummary, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    }
                }
            }

            SectionCard(title = "Phương thức thanh toán") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = paymentMethod == "sender_cash",
                        onClick = { paymentMethod = "sender_cash" }
                    )
                    Text(text = "Người gửi trả tiền mặt")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = paymentMethod == "recipient_cash",
                        onClick = { paymentMethod = "recipient_cash" }
                    )
                    Text(text = "Người nhận trả tiền mặt")
                }
            }

            val distanceText = uiState.distanceKm?.let { String.format(Locale.US, "%.2f", it) } ?: "-"
            val totalPriceText = uiState.totalPrice?.let { String.format(Locale.US, "%.0f", it) } ?: "-"
            SectionCard(title = "Ước tính chi phí") {
                Text(text = "Khoảng cách ước tính: $distanceText km")
                Text(text = "Tổng tiền ước tính: $totalPriceText VND")
            }

            SectionCard(title = "Ảnh đơn hàng") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
                        text = "Chụp ảnh"
                    )
                    IosSecondaryButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f),
                        text = "Chọn ảnh"
                    )
                }

                selectedPhotoUri?.let { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = "Order photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }

                val photoUploaded = !uiState.orderPhotoUrl.isNullOrBlank()
                val uploadStatus = when {
                    uiState.isPhotoUploading -> "Đang upload ảnh..."
                    photoUploaded -> "Ảnh đã được upload"
                    else -> "Chưa upload ảnh"
                }
                Text(text = uploadStatus)
            }

            if (!localError.isNullOrBlank()) {
                Text(text = localError ?: "")
            }

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
                contentPadding = PaddingValues(vertical = 12.dp),
                text = "Tạo đơn"
            )

            if (uiState.isLoading) {
                Spacer(modifier = Modifier.height(8.dp))
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }

    if (showPickupSaveDialog) {
        AlertDialog(
            onDismissRequest = { showPickupSaveDialog = false },
            title = { Text("Lưu địa chỉ lấy hàng") },
            text = {
                IosTextField(
                    value = pickupFavoriteLabel,
                    onValueChange = { pickupFavoriteLabel = it },
                    label = { Text("Gợi nhớ") },
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
                    Text("Lưu")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPickupSaveDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    if (showDeliverySaveDialog) {
        AlertDialog(
            onDismissRequest = { showDeliverySaveDialog = false },
            title = { Text("Lưu địa chỉ giao hàng") },
            text = {
                IosTextField(
                    value = deliveryFavoriteLabel,
                    onValueChange = { deliveryFavoriteLabel = it },
                    label = { Text("Gợi nhớ") },
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
                    Text("Lưu")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeliverySaveDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    if (showSenderSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showSenderSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "Thông tin Người gửi")
                val favorites = uiState.favoriteAddresses
                if (favorites.isNotEmpty()) {
                    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxWidth()) {
                        IosTextField(
                            value = "Chọn địa chỉ yêu thích",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Chọn địa chỉ yêu thích")
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        // Transparent overlay to catch clicks
                        androidx.compose.foundation.layout.Box(
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
                    label = { Text("Địa chỉ") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (!pickupLookupMessage.isNullOrBlank()) {
                    Text(text = pickupLookupMessage ?: "")
                }
                IosTextField(
                    value = pickupNote,
                    onValueChange = { pickupNote = it },
                    label = { Text("Ghi chú (VD: Số tòa nhà)") },
                    modifier = Modifier.fillMaxWidth()
                )
                IosTextField(
                    value = senderName,
                    onValueChange = { senderName = it },
                    label = { Text("Tên") },
                    modifier = Modifier.fillMaxWidth()
                )
                IosTextField(
                    value = senderPhone,
                    onValueChange = { senderPhone = it },
                    label = { Text("Số điện thoại") },
                    modifier = Modifier.fillMaxWidth()
                )
                IosSecondaryButton(
                    onClick = {
                        showSenderSheet = false
                        onNavigatePickPickup()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    text = "Chọn vị trí trên bản đồ"
                )
                IosSecondaryButton(
                    onClick = {
                        if (pickupAddress.isBlank()) {
                            localError = "Vui lòng nhập địa chỉ lấy hàng trước khi lưu."
                            return@IosSecondaryButton
                        }
                        if (uiState.pickupLat == null || uiState.pickupLng == null) {
                            localError = "Vui lòng định vị địa chỉ lấy hàng trên bản đồ trước khi lưu."
                            return@IosSecondaryButton
                        }
                        localError = null
                        showPickupSaveDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    text = "Lưu địa chỉ lấy hàng"
                )
                IosPrimaryButton(
                    onClick = { showSenderSheet = false },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    text = "Xác nhận"
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "Thông tin Người nhận")
                val favorites = uiState.favoriteAddresses
                if (favorites.isNotEmpty()) {
                    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxWidth()) {
                        IosTextField(
                            value = "Chọn địa chỉ yêu thích",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Chọn địa chỉ yêu thích")
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        // Transparent overlay to catch clicks
                        androidx.compose.foundation.layout.Box(
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
                    label = { Text("Địa chỉ") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (!deliveryLookupMessage.isNullOrBlank()) {
                    Text(text = deliveryLookupMessage ?: "")
                }
                IosTextField(
                    value = deliveryNote,
                    onValueChange = { deliveryNote = it },
                    label = { Text("Ghi chú (VD: Số tòa nhà)") },
                    modifier = Modifier.fillMaxWidth()
                )
                IosTextField(
                    value = recipientName,
                    onValueChange = { recipientName = it },
                    label = { Text("Tên") },
                    modifier = Modifier.fillMaxWidth()
                )
                IosTextField(
                    value = recipientPhone,
                    onValueChange = { recipientPhone = it },
                    label = { Text("Số điện thoại") },
                    modifier = Modifier.fillMaxWidth()
                )
                IosSecondaryButton(
                    onClick = {
                        showRecipientSheet = false
                        onNavigatePickDelivery()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    text = "Chọn vị trí trên bản đồ"
                )
                IosSecondaryButton(
                    onClick = {
                        if (deliveryAddress.isBlank()) {
                            localError = "Vui lòng nhập địa chỉ giao hàng trước khi lưu."
                            return@IosSecondaryButton
                        }
                        if (uiState.deliveryLat == null || uiState.deliveryLng == null) {
                            localError = "Vui lòng định vị địa chỉ giao hàng trên bản đồ trước khi lưu."
                            return@IosSecondaryButton
                        }
                        localError = null
                        showDeliverySaveDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    text = "Lưu địa chỉ giao hàng"
                )
                IosPrimaryButton(
                    onClick = { showRecipientSheet = false },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    text = "Xác nhận"
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "Chi tiết kiện hàng")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("S", "M", "L").forEach { size ->
                        IosSecondaryButton(
                            onClick = { packageSize = size },
                            modifier = Modifier.weight(1f),
                            text = size
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val weights = listOf("S" to 5.0, "M" to 10.0, "L" to 30.0)
                    weights.forEach { (label, value) ->
                        IosSecondaryButton(
                            onClick = { weightKg = value.toString() },
                            modifier = Modifier.weight(1f),
                            text = label
                        )
                    }
                }
                IosTextField(
                    value = weightKg,
                    onValueChange = { weightKg = it },
                    label = { Text("Khối lượng (kg)") },
                    modifier = Modifier.fillMaxWidth()
                )
                androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxWidth()) {
                    IosTextField(
                        value = packageType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Danh mục hàng hóa") },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Chọn danh mục")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { packageExpanded = true }
                    )
                    DropdownMenu(
                        expanded = packageExpanded,
                        onDismissRequest = { packageExpanded = false }
                    ) {
                        packageOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
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
                    label = { Text("Thông tin thêm (không bắt buộc)") },
                    modifier = Modifier.fillMaxWidth()
                )
                IosTextField(
                    value = codAmountText,
                    onValueChange = { codAmountText = it },
                    label = { Text("Số tiền thu hộ") },
                    modifier = Modifier.fillMaxWidth()
                )
                IosPrimaryButton(
                    onClick = { showPackageSheet = false },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    text = "Xác nhận"
                )
            }
        }
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
        val results = geocoder.getFromLocationName(address, 1)
        val location = results?.firstOrNull() ?: return@withContext null
        location.latitude to location.longitude
    }
}

private suspend fun reverseGeocodeAddress(context: Context, lat: Double, lng: Double): String? {
    return withContext(Dispatchers.IO) {
        if (!Geocoder.isPresent()) {
            return@withContext null
        }
        val geocoder = Geocoder(context, Locale.getDefault())
        val results = geocoder.getFromLocation(lat, lng, 1)
        val addressObj = results?.firstOrNull() ?: return@withContext null
        addressObj.getAddressLine(0)
    }
}
