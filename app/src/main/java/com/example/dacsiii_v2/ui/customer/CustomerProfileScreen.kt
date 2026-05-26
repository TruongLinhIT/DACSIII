package com.example.dacsiii_v2.ui.customer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
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
                title = { Text("Cập nhật hồ sơ") },
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
            SectionCard(
                title = "Thông tin cá nhân",
                subtitle = "Cập nhật thông tin liên hệ"
            ) {
                val avatarModel = selectedAvatarUri ?: profile?.avatar_url
                AsyncImage(
                    model = avatarModel,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
                IosSecondaryButton(
                    text = "Chụp ảnh chân dung",
                    onClick = {
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
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                if (uiState.isAvatarUploading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                IosTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Họ và tên") },
                    modifier = Modifier.fillMaxWidth()
                )
                IosTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                IosTextField(
                    value = cccdNumber,
                    onValueChange = { cccdNumber = it },
                    label = { Text("CCCD") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            SectionCard(title = "Bảo mật", subtitle = "Đổi mật khẩu bằng OTP") {
                Text("Mã OTP sẽ gửi về email: ${profile?.email ?: "Chưa có email"}")
                IosPrimaryButton(
                    text = "Đổi mật khẩu",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showPasswordSheet = true }
                )
            }

            SectionCard(title = "Định danh eKYC") {
                Text("Trạng thái: ${profile?.is_verified ?: "unverified"}")
                val rejectReason = profile?.identity_reject_reason
                if (!rejectReason.isNullOrBlank()) {
                    Text("Lý do từ chối: $rejectReason")
                }

                val frontModel = idFrontUri ?: profile?.id_card_front_url
                AsyncImage(
                    model = frontModel,
                    contentDescription = "CCCD mặt trước",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )
                IosSecondaryButton(
                    text = "Chụp CCCD mặt trước",
                    onClick = {
                        identityTarget = IdentityPhotoType.FRONT
                        cameraIdentityUri = createCameraImageUri(context)
                        val uri = cameraIdentityUri
                        if (uri != null) {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPermission) {
                                identityCameraLauncher.launch(uri)
                            } else {
                                identityPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                val backModel = idBackUri ?: profile?.id_card_back_url
                AsyncImage(
                    model = backModel,
                    contentDescription = "CCCD mặt sau",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )
                IosSecondaryButton(
                    text = "Chụp CCCD mặt sau",
                    onClick = {
                        identityTarget = IdentityPhotoType.BACK
                        cameraIdentityUri = createCameraImageUri(context)
                        val uri = cameraIdentityUri
                        if (uri != null) {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPermission) {
                                identityCameraLauncher.launch(uri)
                            } else {
                                identityPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                val portraitModel = portraitUri ?: profile?.portrait_url
                AsyncImage(
                    model = portraitModel,
                    contentDescription = "Ảnh chân dung",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )
                IosSecondaryButton(
                    text = "Chụp ảnh chân dung",
                    onClick = {
                        identityTarget = IdentityPhotoType.PORTRAIT
                        cameraIdentityUri = createCameraImageUri(context)
                        val uri = cameraIdentityUri
                        if (uri != null) {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPermission) {
                                identityCameraLauncher.launch(uri)
                            } else {
                                identityPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                IosPrimaryButton(
                    text = if (uiState.isIdentityUploading) "Đang gửi..." else "Gửi định danh",
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

            if (!localError.isNullOrBlank()) {
                Text(text = localError ?: "", modifier = Modifier.fillMaxWidth())
            }

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
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 12.dp)
            )

            if (uiState.isLoading) {
                Spacer(modifier = Modifier.height(8.dp))
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }

        if (showPasswordSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { showPasswordSheet = false },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Đổi mật khẩu")
                    Text("Email: ${profile?.email ?: "Chưa có email"}")

                    IosTextField(
                        value = otpCode,
                        onValueChange = { otpCode = it.trim() },
                        label = { Text("Mã OTP (6 số)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    IosTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Mật khẩu mới") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    IosSecondaryButton(
                        text = if (uiState.isPasswordOtpSending) "Đang gửi..." else "Gửi OTP",
                        enabled = !profile?.email.isNullOrBlank() && !uiState.isPasswordOtpSending,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { viewModel.sendChangePasswordOtp(token) }
                    )

                    IosPrimaryButton(
                        text = if (uiState.isPasswordChanging) "Đang đổi..." else "Xác nhận đổi mật khẩu",
                        enabled = otpCode.length == 6 && newPassword.length >= 6 && !uiState.isPasswordChanging,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { viewModel.changePasswordWithOtp(token, otpCode, newPassword) }
                    )

                    TextButton(
                        onClick = { showPasswordSheet = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Đóng")
                    }
                }
            }
        }
    }
}

private fun createCameraImageUri(context: Context): Uri {
    val file = File(context.cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

private enum class IdentityPhotoType {
    FRONT,
    BACK,
    PORTRAIT
}
