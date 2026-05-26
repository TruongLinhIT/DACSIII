package com.example.dacsiii_v2.ui.driver

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.dacsiii_v2.data.model.DriverProfileUpdateRequest
import com.example.dacsiii_v2.ui.common.IosPrimaryButton
import com.example.dacsiii_v2.ui.common.IosSecondaryButton
import com.example.dacsiii_v2.ui.common.IosTextField
import com.example.dacsiii_v2.ui.common.SectionCard
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hồ sơ tài xế") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val profile = uiState.profile
            SectionCard(title = "Thông tin tài xế") {
                Text(text = "Họ tên: ${profile?.full_name ?: "-"}")
                Text(text = "Email: ${profile?.email ?: "-"}")
                Text(text = "Số điện thoại: ${profile?.phone ?: "-"}")
                Text(text = "Trạng thái định danh: ${profile?.is_verified ?: "unverified"}")
            }

            SectionCard(title = "Thông tin phương tiện") {
                IosTextField(
                    value = licensePlate,
                    onValueChange = { licensePlate = it },
                    label = { Text("Biển số xe") },
                    modifier = Modifier.fillMaxWidth()
                )

                IosTextField(
                    value = vehicleType,
                    onValueChange = { vehicleType = it },
                    label = { Text("Loại xe") },
                    modifier = Modifier.fillMaxWidth()
                )

                IosPrimaryButton(
                    text = "Cập nhật",
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

            SectionCard(title = "Định danh eKYC") {
                IosTextField(
                    value = cccdNumber,
                    onValueChange = { cccdNumber = it },
                    label = { Text("CCCD") },
                    modifier = Modifier.fillMaxWidth()
                )

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
                        viewModel.uploadIdentity(token, context, cccd, idFrontUri!!, idBackUri!!, portraitUri!!)
                    },
                    enabled = !uiState.isIdentityUploading,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (!localError.isNullOrBlank()) {
                Text(text = localError ?: "", modifier = Modifier.fillMaxWidth())
            }
        }
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
