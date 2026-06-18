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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.dacsiii_v2.data.model.DriverProfileUpdateRequest
import com.example.dacsiii_v2.ui.common.IosPrimaryButton
import com.example.dacsiii_v2.ui.common.IosSecondaryButton
import com.example.dacsiii_v2.ui.common.IosTextField
import com.example.dacsiii_v2.ui.common.SectionCard
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverProfileScreen(
    token: String,
    viewModel: DriverViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var licensePlate by rememberSaveable { mutableStateOf("") }
    var vehicleType by rememberSaveable { mutableStateOf("") }
    var cccdNumber by rememberSaveable { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    var idFrontUri by remember { mutableStateOf<Uri?>(null) }
    var idBackUri by remember { mutableStateOf<Uri?>(null) }
    var portraitUri by remember { mutableStateOf<Uri?>(null) }
    var cameraIdentityUri by remember { mutableStateOf<Uri?>(null) }
    var identityTarget by remember { mutableStateOf<IdentityPhotoType?>(null) }

    val identityCameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            when (identityTarget) {
                IdentityPhotoType.FRONT -> idFrontUri = cameraIdentityUri
                IdentityPhotoType.BACK -> idBackUri = cameraIdentityUri
                IdentityPhotoType.PORTRAIT -> portraitUri = cameraIdentityUri
                null -> Unit
            }
        }
    }

    val identityPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = cameraIdentityUri
            if (uri != null) {
                identityCameraLauncher.launch(uri)
            }
        } else {
            localError = "Bạn cần cấp quyền Camera để chụp ảnh."
        }
    }

    LaunchedEffect(token) {
        if (token.isNotBlank()) {
            viewModel.fetchProfile(token)
        }
    }

    LaunchedEffect(uiState.profile) {
        val profile = uiState.profile
        if (profile != null) {
            licensePlate = profile.license_plate ?: licensePlate
            vehicleType = profile.vehicle_type ?: vehicleType
            cccdNumber = profile.cccd_number ?: cccdNumber
        }
    }

    LaunchedEffect(uiState.message) {
        val message = uiState.message
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    val profile = uiState.profile
    val isVerified = profile?.is_verified == "verified"
    val isOnline = (profile?.is_online ?: 0) == 1

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Hồ sơ tài xế",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Hero Profile Header ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1A237E),
                                Color(0xFF283593),
                                Color(0xFF3949AB)
                            )
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 28.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Avatar
                    Box(
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                                .border(3.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!profile?.avatar_url.isNullOrBlank()) {
                                AsyncImage(
                                    model = profile?.avatar_url,
                                    contentDescription = "Avatar",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(56.dp),
                                    tint = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                        // Online indicator
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .background(
                                    if (isOnline) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
                                    CircleShape
                                )
                                .border(3.dp, Color(0xFF1A237E), CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = profile?.full_name ?: "Tài xế",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isOnline) "🟢 Đang hoạt động" else "⚪ Ngoại tuyến",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.75f)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Quick info row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProfileStatItem(
                            icon = Icons.Default.Shield,
                            label = if (isVerified) "Đã xác thực" else "Chưa xác thực",
                            iconTint = if (isVerified) Color(0xFF4CAF50) else Color(0xFFFFC107)
                        )
                        ProfileStatItem(
                            icon = Icons.Default.DirectionsCar,
                            label = profile?.vehicle_type ?: "Chưa cập nhật",
                            iconTint = Color.White
                        )
                        ProfileStatItem(
                            icon = Icons.Default.Phone,
                            label = profile?.phone ?: "-",
                            iconTint = Color.White
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

                // ── Identity Verification Status Banner ──────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (isVerified)
                                Brush.linearGradient(listOf(Color(0xFF1B5E20), Color(0xFF2E7D32)))
                            else
                                Brush.linearGradient(listOf(Color(0xFF7B3F00), Color(0xFFBF6A02)))
                        )
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isVerified) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isVerified) "Tài khoản đã xác thực" else "Chưa xác thực eKYC",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (isVerified) "Bạn đã được xác minh danh tính thành công." else "Vui lòng hoàn thành xác minh để nhận đơn hàng.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // ── Contact Info ──────────────────────────────────────────────
                SectionCard(title = "Thông tin liên hệ") {
                    ProfileInfoRow(icon = Icons.Default.Person, label = "Họ tên", value = profile?.full_name ?: "-")
                    ProfileInfoRow(icon = Icons.Default.Email, label = "Email", value = profile?.email ?: "-")
                    ProfileInfoRow(icon = Icons.Default.Phone, label = "Điện thoại", value = profile?.phone ?: "-")
                }

                // ── Online Status Toggle ──────────────────────────────────────
                SectionCard(title = "Trạng thái hoạt động") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isOnline)
                                    Color(0xFF4CAF50).copy(alpha = 0.12f)
                                else
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    if (isOnline) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isOnline) "Đang trực tuyến – nhận đơn hàng mới" else "Đang ngoại tuyến – không nhận đơn",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isOnline) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IosPrimaryButton(
                        text = if (isOnline) "Tắt hoạt động" else "Bật hoạt động",
                        onClick = {
                            viewModel.setDriverOnlineStatus(context, token, if (isOnline) 0 else 1)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // ── Vehicle Info ──────────────────────────────────────────────
                SectionCard(title = "Thông tin phương tiện") {
                    IosTextField(
                        value = licensePlate,
                        onValueChange = { licensePlate = it },
                        label = { Text("Biển số xe") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    IosTextField(
                        value = vehicleType,
                        onValueChange = { vehicleType = it },
                        label = { Text("Loại xe") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    IosPrimaryButton(
                        text = "Cập nhật phương tiện",
                        onClick = {
                            if (licensePlate.isNotBlank()) {
                                viewModel.updateProfile(
                                    token,
                                    DriverProfileUpdateRequest(
                                        license_plate = licensePlate,
                                        vehicle_type = vehicleType.ifBlank { null }
                                    )
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // ── eKYC Section ──────────────────────────────────────────────
                SectionCard(title = "Xác minh danh tính (eKYC)") {
                    IosTextField(
                        value = cccdNumber,
                        onValueChange = { cccdNumber = it },
                        label = { Text("Số CCCD") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // CCCD Front
                    IdentityPhotoCard(
                        label = "CCCD mặt trước",
                        imageUri = idFrontUri,
                        existingUrl = profile?.id_card_front_url,
                        onCapture = {
                            identityTarget = IdentityPhotoType.FRONT
                            cameraIdentityUri = createCameraImageUri(context)
                            launchCamera(
                                context = context,
                                uri = cameraIdentityUri,
                                cameraLauncher = identityCameraLauncher,
                                permissionLauncher = identityPermissionLauncher
                            )
                        }
                    )

                    // CCCD Back
                    IdentityPhotoCard(
                        label = "CCCD mặt sau",
                        imageUri = idBackUri,
                        existingUrl = profile?.id_card_back_url,
                        onCapture = {
                            identityTarget = IdentityPhotoType.BACK
                            cameraIdentityUri = createCameraImageUri(context)
                            launchCamera(
                                context = context,
                                uri = cameraIdentityUri,
                                cameraLauncher = identityCameraLauncher,
                                permissionLauncher = identityPermissionLauncher
                            )
                        }
                    )

                    // Portrait
                    IdentityPhotoCard(
                        label = "Ảnh chân dung",
                        imageUri = portraitUri,
                        existingUrl = profile?.portrait_url,
                        onCapture = {
                            identityTarget = IdentityPhotoType.PORTRAIT
                            cameraIdentityUri = createCameraImageUri(context)
                            launchCamera(
                                context = context,
                                uri = cameraIdentityUri,
                                cameraLauncher = identityCameraLauncher,
                                permissionLauncher = identityPermissionLauncher
                            )
                        }
                    )

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

                    IosPrimaryButton(
                        text = if (uiState.isIdentityUploading) "Đang gửi..." else "Gửi xác minh danh tính",
                        onClick = {
                            val cccd = cccdNumber.trim()
                            if (cccd.isBlank()) {
                                localError = "Vui lòng nhập CCCD trước khi gửi định danh."
                                return@IosPrimaryButton
                            }
                            if (idFrontUri == null || idBackUri == null || portraitUri == null) {
                                localError = "Vui lòng chụp đủ 3 ảnh CCCD và chân dung."
                                return@IosPrimaryButton
                            }
                            localError = null
                            viewModel.uploadIdentity(token, context, cccd, idFrontUri!!, idBackUri!!, portraitUri!!)
                        },
                        enabled = !uiState.isIdentityUploading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ProfileStatItem(
    icon: ImageVector,
    label: String,
    iconTint: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.85f),
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun IdentityPhotoCard(
    label: String,
    imageUri: Uri?,
    existingUrl: String?,
    onCapture: () -> Unit
) {
    val hasPhoto = imageUri != null || !existingUrl.isNullOrBlank()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.5.dp,
                color = if (hasPhoto)
                    Color(0xFF4CAF50).copy(alpha = 0.5f)
                else
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                if (hasPhoto)
                    Color(0xFF4CAF50).copy(alpha = 0.05f)
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (hasPhoto) Icons.Default.CheckCircle else Icons.Default.Camera,
                contentDescription = null,
                tint = if (hasPhoto) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            if (hasPhoto) {
                Text(
                    text = "✓ Đã chụp",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        val displayModel = imageUri ?: existingUrl
        if (displayModel != null) {
            AsyncImage(
                model = displayModel,
                contentDescription = label,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }

        IosSecondaryButton(
            text = if (hasPhoto) "Chụp lại" else "Chụp ảnh",
            onClick = onCapture,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun launchCamera(
    context: Context,
    uri: Uri?,
    cameraLauncher: androidx.activity.result.ActivityResultLauncher<Uri>,
    permissionLauncher: androidx.activity.result.ActivityResultLauncher<String>
) {
    if (uri == null) return
    val hasPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
    if (hasPermission) {
        cameraLauncher.launch(uri)
    } else {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }
}

private fun createCameraImageUri(context: Context): Uri {
    val file = File(context.cacheDir, "identity_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

private enum class IdentityPhotoType {
    FRONT,
    BACK,
    PORTRAIT
}
