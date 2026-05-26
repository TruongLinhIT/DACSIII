package com.example.dacsiii_v2.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dacsiii_v2.data.model.UserSummary
import com.example.dacsiii_v2.ui.common.EmptyState
import com.example.dacsiii_v2.ui.common.IosCard
import com.example.dacsiii_v2.ui.common.StatusPill

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

    LaunchedEffect(selectedStatus) {
        viewModel.fetchUsers(token, isVerified = selectedStatus)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(if (selectedStatus == "pending") "Duyệt hồ sơ" else "Quản lý người dùng") 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {}
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
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                LazyRow(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedStatus == null,
                            onClick = { selectedStatus = null },
                            label = { Text("Tất cả") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedStatus == "pending",
                            onClick = { selectedStatus = "pending" },
                            label = { Text("Chờ duyệt") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedStatus == "verified",
                            onClick = { selectedStatus = "verified" },
                            label = { Text("Đã duyệt") }
                        )
                    }
                }

                when {
                    uiState.isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    uiState.users.isEmpty() -> {
                        EmptyState(message = "Không tìm thấy người dùng nào")
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.users) { user ->
                                UserItem(user = user, onClick = { onNavigateDetail(user.user_id) })
                            }
                        }
                    }
                }
            }

            uiState.message?.let {
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                ) {
                    Text(it)
                }
            }
        }
    }
}

@Composable
fun UserItem(user: UserSummary, onClick: () -> Unit) {
    val roleLabel = user.role.uppercase()
    val roleColors = when (user.role) {
        "admin" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        "driver" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    }
    val statusLabel = when (user.is_verified) {
        "verified" -> "Đã duyệt"
        "pending" -> "Chờ duyệt"
        "rejected" -> "Từ chối"
        else -> "Chưa định danh"
    }
    val statusColors = when (user.is_verified) {
        "verified" -> Color(0xFFE6F4EA) to Color(0xFF1E4620)
        "pending" -> Color(0xFFFFF4E5) to Color(0xFF7A4A00)
        "rejected" -> Color(0xFFFCE8E6) to Color(0xFFB3261E)
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    IosCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(12.dp)) {
        ListItem(
            headlineContent = {
                Text(
                    text = user.full_name ?: "Chưa cập nhật tên",
                    fontWeight = FontWeight.SemiBold
                )
            },
            supportingContent = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = user.email ?: "Không có email")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusPill(
                            text = roleLabel,
                            containerColor = roleColors.first,
                            contentColor = roleColors.second
                        )
                    }
                }
            },
            trailingContent = {
                StatusPill(
                    text = statusLabel,
                    containerColor = statusColors.first,
                    contentColor = statusColors.second
                )
            }
        )
    }
}
