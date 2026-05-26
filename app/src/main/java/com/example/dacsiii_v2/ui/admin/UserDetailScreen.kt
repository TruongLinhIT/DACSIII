package com.example.dacsiii_v2.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.dacsiii_v2.ui.common.InfoRow
import com.example.dacsiii_v2.ui.common.IosCard
import com.example.dacsiii_v2.ui.common.IosPrimaryButton
import com.example.dacsiii_v2.ui.common.IosSecondaryButton
import com.example.dacsiii_v2.ui.common.IosTextField
import com.example.dacsiii_v2.ui.common.SectionCard
import com.example.dacsiii_v2.ui.common.StatusPill

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    token: String,
    userId: Int,
    viewModel: AdminViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val baseUrl = "http://10.0.2.2:3000" // Cập nhật đúng IP server của bạn
    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectReason by remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        viewModel.fetchUserDetail(token, userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết hồ sơ") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            uiState.selectedUser?.let { user ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val statusColors = when (user.is_verified) {
                        "verified" -> Color(0xFFE6F4EA) to Color(0xFF1E4620)
                        "rejected" -> Color(0xFFFCE8E6) to Color(0xFFB3261E)
                        "pending" -> Color(0xFFFFF4E5) to Color(0xFF7A4A00)
                        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    SectionCard(
                        title = "Thông tin tài khoản",
                        subtitle = "Chi tiết hồ sơ người dùng"
                    ) {
                        InfoRow(label = "Họ tên", value = user.full_name ?: "N/A")
                        InfoRow(label = "Email", value = user.email ?: "N/A")
                        InfoRow(label = "Số điện thoại", value = user.phone ?: "N/A")
                        InfoRow(label = "Vai trò", value = user.role.uppercase())
                        InfoRow(label = "CCCD", value = user.cccd_number ?: "Chưa có")
                        if (!user.identity_reject_reason.isNullOrBlank()) {
                            InfoRow(label = "Lý do từ chối", value = user.identity_reject_reason)
                        }
                        StatusPill(
                            text = user.is_verified.uppercase(),
                            containerColor = statusColors.first,
                            contentColor = statusColors.second
                        )
                    }

                    SectionCard(
                        title = "Hình ảnh định danh (eKYC)",
                        subtitle = "Kiểm tra ảnh CCCD và chân dung"
                    ) {
                        IdentityImage(label = "Mặt trước CCCD", url = user.id_card_front_url?.let { "$baseUrl$it" })
                        IdentityImage(label = "Mặt sau CCCD", url = user.id_card_back_url?.let { "$baseUrl$it" })
                        IdentityImage(label = "Ảnh chân dung", url = user.portrait_url?.let { "$baseUrl$it" })
                    }

                    if (user.is_verified == "pending") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IosPrimaryButton(
                                text = "Duyệt",
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.verifyUser(token, userId, "verified") }
                            )
                            IosSecondaryButton(
                                text = "Từ chối",
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    rejectReason = ""
                                    showRejectDialog = true
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            uiState.message?.let {
                Snackbar(modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)) {
                    Text(it)
                }
            }
        }
    }

    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Lý do từ chối") },
            text = {
                IosTextField(
                    value = rejectReason,
                    onValueChange = { rejectReason = it },
                    label = { Text("Nhập lý do") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                IosPrimaryButton(
                    text = "Xác nhận",
                    enabled = rejectReason.trim().length >= 3,
                    onClick = {
                        viewModel.verifyUser(token, userId, "rejected", rejectReason.trim())
                        showRejectDialog = false
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}


@Composable
fun IdentityImage(label: String, url: String?) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(4.dp))
        IosCard(modifier = Modifier.fillMaxWidth().height(200.dp), contentPadding = PaddingValues(0.dp)) {
            if (url != null) {
                AsyncImage(
                    model = url,
                    contentDescription = label,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Không có ảnh", color = Color.Gray)
                }
            }
        }
    }
}