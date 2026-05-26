package com.example.dacsiii_v2.ui.driver

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File
import kotlinx.coroutines.launch
import com.example.dacsiii_v2.ui.common.IosPrimaryButton
import com.example.dacsiii_v2.ui.common.SectionCard
import com.example.dacsiii_v2.ui.common.StatusPill

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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val order = uiState.orderDetail
                if (order == null) {
                    Text("Không có dữ liệu đơn hàng.")
                    return@Column
                }

                SectionCard(title = "Tổng quan") {
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

                SectionCard(title = "Ảnh đơn hàng") {
                    AsyncImage(
                        model = normalizePhotoUrl(order.photo_before_booking),
                        contentDescription = "Order photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )

                    if (!order.photo_at_pickup.isNullOrBlank()) {
                        Text(text = "Ảnh nhận hàng")
                        AsyncImage(
                            model = normalizePhotoUrl(order.photo_at_pickup),
                            contentDescription = "Pickup photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }

                    if (!order.photo_at_delivery.isNullOrBlank()) {
                        Text(text = "Ảnh giao hàng")
                        AsyncImage(
                            model = normalizePhotoUrl(order.photo_at_delivery),
                            contentDescription = "Delivery photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
                }

                when (order.status) {
                    "accepted" -> {
                        IosPrimaryButton(
                            text = "Đến điểm lấy",
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { viewModel.arrivePickup(token, order.order_id) }
                        )
                    }
                    "picking_up" -> {
                        IosPrimaryButton(
                            text = "Nhận hàng",
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                photoTarget = "pickup"
                                showPhotoPicker = true
                            }
                        )
                    }
                    "delivering" -> {
                        IosPrimaryButton(
                            text = "Đến điểm giao",
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { viewModel.arriveDelivery(token, order.order_id) }
                        )
                    }
                    "arrived_delivery" -> {
                        IosPrimaryButton(
                            text = "Hoàn thành",
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                photoTarget = "delivery"
                                showPhotoPicker = true
                            }
                        )
                    }
                }
            }

            if (uiState.isPhotoUploading || uiState.isOrderDetailLoading) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background.copy(alpha = 0.6f)
                ) {}
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(androidx.compose.ui.Alignment.Center)
                        .size(48.dp)
                )
            }
        }
    }

    if (showPhotoPicker) {
        AlertDialog(
            onDismissRequest = { showPhotoPicker = false },
            title = { Text("Chọn ảnh") },
            text = { Text("Bạn muốn chụp ảnh mới hay chọn từ thư viện?") },
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
                    Text("Chụp ảnh")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPhotoPicker = false
                    galleryLauncher.launch("image/*")
                }) {
                    Text("Thư viện")
                }
            }
        )
    }
}

private fun normalizePhotoUrl(photoUrl: String): String {
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
