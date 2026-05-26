package com.example.dacsiii_v2.ui.auth

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun RegisterProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel,
    onBackToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        viewModel.onAvatarSelected(it)
    }
    val frontPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        viewModel.onFrontSelected(it)
    }
    val backPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        viewModel.onBackSelected(it)
    }
    val portraitPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        viewModel.onPortraitSelected(it)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Thông tin hồ sơ", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.fullName,
            onValueChange = viewModel::onFullNameChange,
            label = { Text("Họ và tên") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Trường mật khẩu mới
        OutlinedTextField(
            value = uiState.password,
            onValueChange = viewModel::onPasswordChange,
            label = { Text("Mật khẩu (để đăng nhập lần sau)") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.email,
            onValueChange = {}, // Email đã được xác thực, không cho sửa
            label = { Text("Email (đã xác thực)") },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.cccdNumber,
            onValueChange = viewModel::onCccdChange,
            label = { Text("CCCD (12 số)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Hình ảnh định danh (eKYC)", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { avatarPicker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Chọn ảnh đại diện")
        }
        SelectedFileLabel(label = "Avatar", uri = uiState.avatarUri)

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { frontPicker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Chọn CCCD mặt trước")
        }
        SelectedFileLabel(label = "Mặt trước", uri = uiState.idCardFrontUri)

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { backPicker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Chọn CCCD mặt sau")
        }
        SelectedFileLabel(label = "Mặt sau", uri = uiState.idCardBackUri)

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { portraitPicker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Chọn ảnh chân dung")
        }
        SelectedFileLabel(label = "Chân dung", uri = uiState.portraitUri)

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = viewModel::updateProfile,
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Lưu thông tin & Đăng ký")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.uploadAvatar(context) },
            enabled = uiState.avatarUri != null && !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Upload Avatar")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.uploadIdentity(context) },
            enabled = uiState.idCardFrontUri != null && uiState.idCardBackUri != null && uiState.portraitUri != null && !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Upload eKYC")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onBackToLogin, modifier = Modifier.fillMaxWidth()) {
            Text("Về trang đăng nhập")
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        }

        uiState.message?.let {
            Text(text = it, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
private fun SelectedFileLabel(label: String, uri: Uri?) {
    val fileName = uri?.lastPathSegment ?: "Chưa chọn"
    Text(text = "$label: $fileName", style = MaterialTheme.typography.bodySmall)
}
