package com.example.dacsiii_v2.ui.customer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.dacsiii_v2.data.model.ProfileUpdateRequest
import com.example.dacsiii_v2.ui.common.IosPrimaryButton
import com.example.dacsiii_v2.ui.common.IosSecondaryButton
import com.example.dacsiii_v2.ui.common.IosTextField
import com.example.dacsiii_v2.ui.common.SectionCard
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerProfileScreen(
    token: String,
    viewModel: CustomerViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var isInitialized by remember { mutableStateOf(false) }
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var cccdNumber by rememberSaveable { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    var selectedAvatarUri by remember { mutableStateOf<Uri?>(null) }
    var cameraAvatarUri by remember { mutableStateOf<Uri?>(null) }
    var lastUploadedAvatarUri by remember { mutableStateOf<Uri?>(null) }

    var idFrontUri by remember { mutableStateOf<Uri?>(null) }
    var idBackUri by remember { mutableStateOf<Uri?>(null) }
    var portraitUri by remember { mutableStateOf<Uri?>(null) }
    var cameraIdentityUri by remember { mutableStateOf<Uri?>(null) }
    var identityTarget by remember { mutableStateOf<IdentityPhotoType?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedAvatarUri = cameraAvatarUri
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = cameraAvatarUri
            if (uri != null) {
                cameraLauncher.launch(uri)
            }
        } else {
            localError = "Bạn cần cấp quyền Camera để chụp ảnh."
        }
    }

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

    var showPasswordSheet by remember { mutableStateOf(false) }
    var otpCode by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }

    val profile = uiState.userProfile?.user
    val isVerified = profile?.is_verified == "verified"
    val isPending = profile?.is_verified == "pending"

    LaunchedEffect(token) {
        if (token.isNotBlank()) {
            viewModel.fetchProfile(token)
        }
    }

    LaunchedEffect(profile?.user_id) {
        if (profile != null && !isInitialized) {
            fullName = profile.full_name.orEmpty()
            email = profile.email.orEmpty()
            cccdNumber = profile.cccd_number.orEmpty()
            isInitialized = true
        }
    }

    LaunchedEffect(selectedAvatarUri) {
        val uri = selectedAvatarUri
        if (uri != null && uri != lastUploadedAvatarUri && !uiState.isAvatarUploading) {
            lastUploadedAvatarUri = uri
            viewModel.uploadAvatar(token, context, uri)
        }
    }

    LaunchedEffect(uiState.passwordChanged) {
        if (uiState.passwordChanged) {
            showPasswordSheet = false
            otpCode = ""
            newPassword = ""
            viewModel.clearPasswordChanged()
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
                title = { Text("Hồ sơ cá nhân", fontWeight = FontWeight.Bold) },
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
                                Color(0xFF0F2027),
                                Color(0xFF203A43),
                                Color(0xFF2C5364)
                            )
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 28.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Avatar & Upload button
                    Box(
                        contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier.clickable {
                            cameraAvatarUri = createCameraImageUri(context)
                            val uri = cameraAvatarUri
                            if (uri != null) {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasPermission) {
                                    cameraLauncher.launch(uri)
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                                .border(3.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val avatarModel = selectedAvatarUri ?: if (!profile?.avatar_url.isNullOrBlank()) normalizePhotoUrl(profile?.avatar_url) else null
                            if (avatarModel != null) {
                                AsyncImage(
                                    model = avatarModel,
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
                        
                        // Edit camera overlay icon
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Camera,
                                contentDescription = "Edit Avatar",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = profile?.full_name ?: "Khách hàng",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = profile?.phone ?: "-",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.75f)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Verified badge inside hero
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isVerified) Icons.Default.VerifiedUser else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isVerified) Color(0xFF4CAF50) else Color(0xFFFFC107),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                isVerified -> "Đã xác thực eKYC"
                                isPending -> "Đang chờ duyệt"
                                else -> "Chưa xác thực danh tính"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
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
                // Identity reject reason if available
                val rejectReason = profile?.identity_reject_reason
                if (!rejectReason.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f))
                            .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    "Yêu cầu eKYC bị từ chối",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    "Lý do: $rejectReason",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // ── Personal details Card ──────────────────────────────────────
                SectionCard(
                    title = "Thông tin tài khoản",
                    subtitle = "Quản lý thông tin liên hệ chính"
                ) {
                    if (uiState.isAvatarUploading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Đang tải ảnh đại diện lên...", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    IosTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Họ và tên") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    IosTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Địa chỉ Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    IosTextField(
                        value = cccdNumber,
                        onValueChange = { cccdNumber = it },
                        label = { Text("Số CCCD") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    IosPrimaryButton(
                        text = "Lưu thay đổi",
                        onClick = {
                            val request = ProfileUpdateRequest(
                                full_name = fullName.takeIf { it.isNotBlank() },
                                email = email.takeIf { it.isNotBlank() },
                                cccd_number = cccdNumber.takeIf { it.isNotBlank() }
                            )

                            val hasAnyField = listOf(
                                request.full_name,
                                request.email,
                                request.cccd_number
                            ).any { !it.isNullOrBlank() }

                            if (!hasAnyField) {
                                localError = "Vui lòng nhập ít nhất một trường để cập nhật."
                            } else {
                                localError = null
                                viewModel.updateProfile(token, request)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // ── Security Card ─────────────────────────────────────────────
                SectionCard(title = "Bảo mật tài khoản") {
                    ProfileInfoRow(icon = Icons.Default.Email, label = "Email nhận mã OTP", value = profile?.email ?: "Chưa có email")
                    IosPrimaryButton(
                        text = "Đổi mật khẩu bảo mật",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { showPasswordSheet = true }
                    )
                }

                // ── eKYC Verification Card ────────────────────────────────────
                SectionCard(title = "Xác minh định danh (eKYC)") {
                    // Front CCCD photo
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

                    // Back CCCD photo
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

                    // Portrait photo
                    IdentityPhotoCard(
                        label = "Ảnh chân dung cá nhân",
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
                        text = if (uiState.isIdentityUploading) "Đang tải ảnh lên..." else "Gửi yêu cầu định danh",
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
                            viewModel.uploadIdentity(token, context, idFrontUri!!, idBackUri!!, portraitUri!!)
                        },
                        enabled = !uiState.isIdentityUploading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Change password bottom sheet
        if (showPasswordSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { showPasswordSheet = false },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Đổi mật khẩu mới", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "Chúng tôi sẽ gửi mã xác thực OTP về email của bạn: ${profile?.email ?: "Chưa cấu hình"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    IosTextField(
                        value = otpCode,
                        onValueChange = { otpCode = it.trim() },
                        label = { Text("Mã OTP (6 số)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    IosTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Mật khẩu mới (Tối thiểu 6 kí tự)") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IosSecondaryButton(
                            text = if (uiState.isPasswordOtpSending) "Đang gửi..." else "Nhận mã OTP",
                            enabled = !profile?.email.isNullOrBlank() && !uiState.isPasswordOtpSending,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.sendChangePasswordOtp(token) }
                        )
                        IosPrimaryButton(
                            text = if (uiState.isPasswordChanging) "Đang xử lý..." else "Đổi mật khẩu",
                            enabled = otpCode.length == 6 && newPassword.length >= 6 && !uiState.isPasswordChanging,
                            modifier = Modifier.weight(1.5f),
                            onClick = { viewModel.changePasswordWithOtp(token, otpCode, newPassword) }
                        )
                    }

                    TextButton(
                        onClick = { showPasswordSheet = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Hủy bỏ")
                    }
                }
            }
        }
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
                    text = "✓ Đã có ảnh",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        val displayModel = imageUri ?: if (!existingUrl.isNullOrBlank()) normalizePhotoUrl(existingUrl) else null
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
            text = if (hasPhoto) "Chụp lại ảnh" else "Chụp ảnh",
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

private fun normalizePhotoUrl(photoUrl: String?): String {
    if (photoUrl.isNullOrBlank()) return ""
    return if (photoUrl.startsWith("http")) {
        photoUrl
    } else {
        "http://10.0.2.2:3000$photoUrl"
    }
}
