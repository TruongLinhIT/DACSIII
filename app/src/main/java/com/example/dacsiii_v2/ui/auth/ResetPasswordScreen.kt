package com.example.dacsiii_v2.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dacsiii_v2.ui.common.IosPrimaryButton
import com.example.dacsiii_v2.ui.common.IosSecondaryButton
import com.example.dacsiii_v2.ui.common.IosTextField
import com.example.dacsiii_v2.ui.common.SectionCard

@Composable
fun ResetPasswordScreen(
    modifier: Modifier = Modifier,
    viewModel: ResetPasswordViewModel = viewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onBack()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Quên mật khẩu",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.step == 1) {
            // Bước 1: Nhập Email
            Text(
                text = "Nhập email của bạn để nhận mã OTP khôi phục mật khẩu.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            SectionCard(title = "Email khôi phục") {
                IosTextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChange,
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.email.isNotEmpty() && !uiState.isEmailValid
                )

                IosPrimaryButton(
                    text = if (uiState.isLoading) "Đang gửi..." else "Gửi mã OTP",
                    enabled = uiState.isEmailValid && !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = viewModel::sendOtp
                )
            }
        } else {
            // Bước 2: Nhập OTP và Mật khẩu mới
            Text(
                text = "Mã OTP đã được gửi về email ${uiState.email}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            SectionCard(title = "Xác nhận OTP") {
                IosTextField(
                    value = uiState.otp,
                    onValueChange = viewModel::onOtpChange,
                    label = { Text("Mã OTP (6 số)") },
                    modifier = Modifier.fillMaxWidth()
                )

                IosTextField(
                    value = uiState.newPassword,
                    onValueChange = viewModel::onNewPasswordChange,
                    label = { Text("Mật khẩu mới") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                IosPrimaryButton(
                    text = if (uiState.isLoading) "Đang đổi..." else "Đổi mật khẩu",
                    enabled = uiState.otp.length == 6 && uiState.newPassword.length >= 6 && !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = viewModel::resetPassword
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        IosSecondaryButton(
            text = "Quay lại",
            modifier = Modifier.fillMaxWidth(),
            onClick = onBack
        )

        uiState.message?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = it,
                color = if (uiState.isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}
