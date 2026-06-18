package com.example.dacsiii_v2.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dacsiii_v2.data.model.UserSummary
import com.example.dacsiii_v2.ui.common.EmptyState
import com.example.dacsiii_v2.ui.common.IosCard
import com.example.dacsiii_v2.ui.common.IosTextField
import com.example.dacsiii_v2.ui.common.StatusPill

import androidx.compose.ui.graphics.graphicsLayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    token: String,
    initialFilter: String? = null,
    viewModel: AdminViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onNavigateDetail: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedStatus by remember { mutableStateOf(initialFilter) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(selectedStatus) {
        viewModel.fetchUsers(token, isVerified = selectedStatus)
    }

    // Lọc client-side theo search
    val filteredUsers = remember(uiState.users, searchQuery, selectedStatus) {
        uiState.users.filter { user ->
            val matchSearch = if (searchQuery.isBlank()) true
            else {
                (user.full_name?.contains(searchQuery, ignoreCase = true) == true) ||
                (user.email?.contains(searchQuery, ignoreCase = true) == true)
            }
            val matchLocked = if (selectedStatus == "locked") user.is_locked else true
            matchSearch && matchLocked
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (selectedStatus) {
                            "pending" -> "Duyệt hồ sơ"
                            "locked" -> "Tài khoản bị khóa"
                            else -> "Quản lý người dùng"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                // ── Search bar ──
                IosTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Tìm kiếm theo tên hoặc email...") },
                    singleLine = true,
                    trailingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Tìm kiếm",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ── Filter chips ──
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedStatus == null,
                            onClick = { selectedStatus = null },
                            label = { Text("Tất cả") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedStatus == "pending",
                            onClick = { selectedStatus = "pending" },
                            label = { Text("Chờ duyệt") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFF59E0B),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedStatus == "verified",
                            onClick = { selectedStatus = "verified" },
                            label = { Text("Đã duyệt") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF10B981),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedStatus == "locked",
                            onClick = {
                                selectedStatus = if (selectedStatus == "locked") null else "locked"
                                if (selectedStatus == "locked") viewModel.fetchUsers(token)
                            },
                            label = { Text("🔒 Bị khóa") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFEF4444),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                when {
                    uiState.isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    filteredUsers.isEmpty() -> {
                        EmptyState(message = "Không tìm thấy người dùng nào")
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filteredUsers) { user ->
                                UserItem(user = user, onClick = { onNavigateDetail(user.user_id) })
                            }
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        }
                    }
                }
            }

            uiState.message?.let {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(it)
                }
            }
        }
    }
}

@Composable
fun UserItem(user: UserSummary, onClick: () -> Unit) {
    val roleLabel = when (user.role) {
        "admin" -> "ADMIN"
        "driver" -> "TÀI XẾ"
        else -> "KHÁCH HÀNG"
    }
    val roleColor = when (user.role) {
        "admin" -> Color(0xFFEF4444)
        "driver" -> Color(0xFF3B82F6)
        else -> Color(0xFF10B981)
    }
    val statusLabel = when (user.is_verified) {
        "verified" -> "Đã duyệt"
        "pending" -> "Chờ duyệt"
        "rejected" -> "Từ chối"
        else -> "Chưa định danh"
    }
    val statusColors = when (user.is_verified) {
        "verified" -> Color(0xFF10B981) to Color(0xFF065F46)
        "pending" -> Color(0xFFF59E0B) to Color(0xFF92400E)
        "rejected" -> Color(0xFFEF4444) to Color(0xFF991B1B)
        else -> MaterialTheme.colorScheme.outline to MaterialTheme.colorScheme.onSurfaceVariant
    }

    val avatarBg = when (user.role) {
        "admin" -> Color(0xFFEF4444).copy(alpha = 0.1f)
        "driver" -> Color(0xFF3B82F6).copy(alpha = 0.1f)
        else -> Color(0xFF10B981).copy(alpha = 0.1f)
    }

    IosCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(avatarBg),
                contentAlignment = Alignment.Center
            ) {
                if (user.is_locked) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Bị khóa",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "User",
                        tint = roleColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.full_name ?: "Chưa cập nhật tên",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (user.is_locked) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFEF4444).copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "BỊ KHÓA",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = user.email ?: "Không có email",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusPill(
                        text = roleLabel,
                        containerColor = roleColor,
                        contentColor = roleColor
                    )
                    StatusPill(
                        text = statusLabel,
                        containerColor = statusColors.first,
                        contentColor = statusColors.second
                    )
                }
            }

            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                modifier = Modifier
                    .size(16.dp)
                    .graphicsLayer { rotationZ = 180f },
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }
    }
}
