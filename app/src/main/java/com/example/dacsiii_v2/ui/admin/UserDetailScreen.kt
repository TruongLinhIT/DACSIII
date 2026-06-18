package com.example.dacsiii_v2.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.dacsiii_v2.ui.common.InfoRow
import com.example.dacsiii_v2.ui.common.IosCard
import com.example.dacsiii_v2.ui.common.IosPrimaryButton
import com.example.dacsiii_v2.ui.common.IosSecondaryButton
import com.example.dacsiii_v2.ui.common.IosTextField
import com.example.dacsiii_v2.ui.common.SectionCard
import com.example.dacsiii_v2.ui.common.StatusPill

private val GradientStart = Color(0xFF667EEA)
private val GradientEnd = Color(0xFF764BA2)

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

    // Dialog states
    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectReason by remember { mutableStateOf("") }
    var showLockDialog by remember { mutableStateOf(false) }
    var lockReason by remember { mutableStateOf("") }
    var showUnlockDialog by remember { mutableStateOf(false) }
    var showRevokeEkycDialog by remember { mutableStateOf(false) }
    var revokeReason by remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        viewModel.fetchUserDetail(token, userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết hồ sơ", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
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
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ── Banner cảnh báo nếu bị khóa ──
                    if (user.is_locked) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFEF4444).copy(alpha = 0.08f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color(0xFFEF4444).copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Tài khoản đang bị khóa",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFEF4444)
                                    )
                                    Text(
                                        text = "Người dùng không thể đăng nhập",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFEF4444).copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }

                    // ── Profile header card ──
                    IosCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(GradientStart, GradientEnd)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (user.avatar_url != null) {
                                    AsyncImage(
                                        model = "$baseUrl${user.avatar_url}",
                                        contentDescription = "Avatar",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = "Avatar",
                                        tint = Color.White,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = user.full_name ?: "Chưa cập nhật tên",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = user.email ?: "Không có email",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Status pills row
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val roleColor = when (user.role) {
                                    "admin" -> Color(0xFFEF4444)
                                    "driver" -> Color(0xFF3B82F6)
                                    else -> Color(0xFF10B981)
                                }
                                val roleLabel = when (user.role) {
                                    "admin" -> "ADMIN"
                                    "driver" -> "TÀI XẾ"
                                    else -> "KHÁCH HÀNG"
                                }
                                StatusPill(
                                    text = roleLabel,
                                    containerColor = roleColor,
                                    contentColor = roleColor
                                )

                                val verifyColor = when (user.is_verified) {
                                    "verified" -> Color(0xFF10B981) to Color(0xFF065F46)
                                    "pending" -> Color(0xFFF59E0B) to Color(0xFF92400E)
                                    "rejected" -> Color(0xFFEF4444) to Color(0xFF991B1B)
                                    else -> MaterialTheme.colorScheme.outline to MaterialTheme.colorScheme.onSurfaceVariant
                                }
                                val verifyLabel = when (user.is_verified) {
                                    "verified" -> "ĐÃ DUYỆT"
                                    "pending" -> "CHỜ DUYỆT"
                                    "rejected" -> "TỪ CHỐI"
                                    else -> "CHƯA ĐỊNH DANH"
                                }
                                StatusPill(
                                    text = verifyLabel,
                                    containerColor = verifyColor.first,
                                    contentColor = verifyColor.second
                                )

                                if (user.is_locked) {
                                    StatusPill(
                                        text = "BỊ KHÓA",
                                        containerColor = Color(0xFFEF4444),
                                        contentColor = Color(0xFF991B1B)
                                    )
                                }
                            }
                        }
                    }

                    // ── Thông tin chi tiết ──
                    SectionCard(
                        title = "Thông tin tài khoản",
                        subtitle = "Chi tiết hồ sơ người dùng"
                    ) {
                        InfoRow(label = "Họ tên", value = user.full_name ?: "N/A")
                        InfoRow(label = "Email", value = user.email ?: "N/A")
                        InfoRow(label = "Số điện thoại", value = user.phone ?: "N/A")
                        InfoRow(label = "Vai trò", value = when (user.role) {
                            "admin" -> "Quản trị viên"
                            "driver" -> "Tài xế"
                            else -> "Khách hàng"
                        })
                        InfoRow(label = "CCCD", value = user.cccd_number ?: "Chưa có")
                        if (!user.identity_reject_reason.isNullOrBlank()) {
                            InfoRow(
                                label = "Lý do từ chối",
                                value = user.identity_reject_reason,
                                valueColor = Color(0xFFEF4444)
                            )
                        }
                    }

                    // ── Hình ảnh eKYC ──
                    SectionCard(
                        title = "Hình ảnh định danh (eKYC)",
                        subtitle = "Kiểm tra ảnh CCCD và chân dung"
                    ) {
                        IdentityImage(label = "Mặt trước CCCD", url = user.id_card_front_url?.let { "$baseUrl$it" })
                        IdentityImage(label = "Mặt sau CCCD", url = user.id_card_back_url?.let { "$baseUrl$it" })
                        IdentityImage(label = "Ảnh chân dung", url = user.portrait_url?.let { "$baseUrl$it" })
                    }

                    // ── Nút duyệt/từ chối eKYC (chỉ hiện khi pending) ──
                    if (user.is_verified == "pending") {
                        SectionCard(
                            title = "Xét duyệt eKYC",
                            subtitle = "Duyệt hoặc từ chối hồ sơ định danh"
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                IosPrimaryButton(
                                    text = "✓ Duyệt",
                                    modifier = Modifier.weight(1f),
                                    onClick = { viewModel.verifyUser(token, userId, "verified") }
                                )
                                IosSecondaryButton(
                                    text = "✗ Từ chối",
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        rejectReason = ""
                                        showRejectDialog = true
                                    }
                                )
                            }
                        }
                    }

                    // ── Hành động quản trị ──
                    if (user.role != "admin") {
                        SectionCard(
                            title = "Hành động quản trị",
                            subtitle = "Quản lý tài khoản người dùng"
                        ) {
                            // Khóa / Mở khóa tài khoản
                            if (user.is_locked) {
                                AdminActionButton(
                                    text = "Mở khóa tài khoản",
                                    icon = Icons.Default.LockOpen,
                                    color = Color(0xFF10B981),
                                    onClick = { showUnlockDialog = true }
                                )
                            } else {
                                AdminActionButton(
                                    text = "Khóa tài khoản",
                                    icon = Icons.Default.Lock,
                                    color = Color(0xFFEF4444),
                                    onClick = {
                                        lockReason = ""
                                        showLockDialog = true
                                    }
                                )
                            }

                            // Hủy duyệt eKYC (chỉ hiện khi verified)
                            if (user.is_verified == "verified") {
                                AdminActionButton(
                                    text = "Hủy duyệt eKYC",
                                    icon = Icons.Default.RemoveCircle,
                                    color = Color(0xFFF59E0B),
                                    onClick = {
                                        revokeReason = ""
                                        showRevokeEkycDialog = true
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            uiState.message?.let {
                Snackbar(modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)) {
                    Text(it)
                }
            }
        }
    }

    // ── Dialogs ──

    // Dialog từ chối eKYC
    if (showRejectDialog) {
        AdminReasonDialog(
            title = "Lý do từ chối",
            label = "Nhập lý do từ chối",
            value = rejectReason,
            onValueChange = { rejectReason = it },
            onConfirm = {
                viewModel.verifyUser(token, userId, "rejected", rejectReason.trim())
                showRejectDialog = false
            },
            onDismiss = { showRejectDialog = false }
        )
    }

    // Dialog khóa tài khoản
    if (showLockDialog) {
        AdminReasonDialog(
            title = "Khóa tài khoản",
            label = "Nhập lý do khóa (vi phạm)",
            value = lockReason,
            onValueChange = { lockReason = it },
            onConfirm = {
                viewModel.lockUser(token, userId, lockReason.trim())
                showLockDialog = false
            },
            onDismiss = { showLockDialog = false }
        )
    }

    // Dialog mở khóa tài khoản
    if (showUnlockDialog) {
        AlertDialog(
            onDismissRequest = { showUnlockDialog = false },
            icon = {
                Icon(
                    Icons.Default.LockOpen,
                    contentDescription = null,
                    tint = Color(0xFF10B981)
                )
            },
            title = { Text("Mở khóa tài khoản") },
            text = { Text("Bạn có chắc muốn mở khóa tài khoản này? Người dùng sẽ có thể đăng nhập lại.") },
            confirmButton = {
                IosPrimaryButton(
                    text = "Mở khóa",
                    onClick = {
                        viewModel.unlockUser(token, userId)
                        showUnlockDialog = false
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showUnlockDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    // Dialog hủy duyệt eKYC
    if (showRevokeEkycDialog) {
        AdminReasonDialog(
            title = "Hủy duyệt eKYC",
            label = "Nhập lý do hủy duyệt",
            value = revokeReason,
            onValueChange = { revokeReason = it },
            onConfirm = {
                viewModel.revokeEkyc(token, userId, revokeReason.trim())
                showRevokeEkycDialog = false
            },
            onDismiss = { showRevokeEkycDialog = false }
        )
    }
}

@Composable
fun AdminActionButton(
    text: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = color,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                modifier = Modifier
                    .size(16.dp)
                    .graphicsLayer { rotationZ = 180f },
                tint = color.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun AdminReasonDialog(
    title: String,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            IosTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label) },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            IosPrimaryButton(
                text = "Xác nhận",
                enabled = value.trim().length >= 3,
                onClick = onConfirm
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@Composable
fun IdentityImage(label: String, url: String?) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(4.dp))
        IosCard(modifier = Modifier
            .fillMaxWidth()
            .height(200.dp), contentPadding = PaddingValues(0.dp)) {
            if (url != null) {
                AsyncImage(
                    model = url,
                    contentDescription = label,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Không có ảnh",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}