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
import androidx.compose.ui.unit.dp
import com.example.dacsiii_v2.ui.common.IosPrimaryButton
import com.example.dacsiii_v2.ui.common.IosSecondaryButton
import com.example.dacsiii_v2.ui.common.IosTextField
import com.example.dacsiii_v2.ui.common.SectionCard

@Composable
fun RegisterOtpScreen(
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel,
    onVerified: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.token) {
        if (!uiState.token.isNullOrBlank()) {
            onVerified()
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
        Text(text = "Xác thực OTP", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(16.dp))

        SectionCard(title = "Mã xác thực") {
            IosTextField(
                value = uiState.otp,
                onValueChange = viewModel::onOtpChange,
                label = { Text("Mã OTP") },
                enabled = uiState.canVerify,
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.otpCountdown > 0) {
                Text(
                    text = "Gửi lại sau ${uiState.otpCountdown}s",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IosPrimaryButton(
                text = "Xác thực",
                enabled = uiState.canVerify && !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
                onClick = viewModel::verifyOtp
            )

            IosSecondaryButton(
                text = "Quay lại",
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
                onClick = onBack
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator()
        }

        uiState.message?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it)
        }
    }
}

