package com.example.dacsiii_v2.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacsiii_v2.data.model.DriverReport
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportsScreen(
    token: String,
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Pending, 1: Resolved
    var searchQuery by remember { mutableStateOf("") }
    var selectedReport by remember { mutableStateOf<DriverReport?>(null) }
    
    val currentStatusFilter = if (selectedTab == 0) "pending" else "resolved"

    LaunchedEffect(token, selectedTab, searchQuery) {
        if (token.isNotBlank()) {
            viewModel.fetchReports(token, currentStatusFilter, searchQuery.ifBlank { null })
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý báo cáo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // ── Search & Filter Bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Tìm kiếm theo tên tài xế, khách hàng...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            // ── Tabs ──
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Chờ xử lý (${uiState.reports.filter { it.status == "pending" }.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Đã giải quyết") }
                )
            }

            // ── Reports List ──
            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.reports.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (uiState.isReportsLoading) {
                            CircularProgressIndicator()
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    "Không có báo cáo nào.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.reports) { report ->
                            ReportCard(report = report, onClick = { selectedReport = report })
                        }
                    }
                }

                if (uiState.isReportsLoading && uiState.reports.isNotEmpty()) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }

    // ── Detailed View Dialog ──
    selectedReport?.let { report ->
        ReportDetailDialog(
            token = token,
            report = report,
            viewModel = viewModel,
            onDismiss = { selectedReport = null }
        )
    }
}

@Composable
fun ReportCard(
    report: DriverReport,
    onClick: () -> Unit
) {
    val statusColor = if (report.status == "pending") Color(0xFFEF4444) else Color(0xFF10B981)
    val statusLabel = if (report.status == "pending") "Chờ xử lý" else "Đã giải quyết"
    val dateStr = formatDateTime(report.created_at)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mã báo cáo #${report.report_id}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = report.reason_type,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = report.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Khách hàng: ${report.reporter_name ?: "N/A"}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Tài xế: ${report.driver_name ?: "N/A"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailDialog(
    token: String,
    report: DriverReport,
    viewModel: AdminViewModel,
    onDismiss: () -> Unit
) {
    var showLockDialog by remember { mutableStateOf(false) }
    var lockReason by remember { mutableStateOf("") }
    val dateStr = formatDateTime(report.created_at)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Chi tiết báo cáo #${report.report_id}",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Trạng thái báo cáo
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val statusColor = if (report.status == "pending") Color(0xFFEF4444) else Color(0xFF10B981)
                    val statusLabel = if (report.status == "pending") "Chờ xử lý" else "Đã giải quyết"
                    
                    Text("Trạng thái: ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = statusColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = statusLabel,
                            color = statusColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Thời gian báo cáo
                Text(
                    text = "Ngày gửi: $dateStr",
                    style = MaterialTheme.typography.bodyMedium
                )

                if (report.order_id != null) {
                    Text(
                        text = "Đơn hàng liên quan: #${report.order_id}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                HorizontalDivider()

                // Lý do & nội dung
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Lý do: ${report.reason_type}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Chi tiết nội dung:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = report.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                HorizontalDivider()

                // Người báo cáo (Khách hàng)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Khách hàng (Người báo cáo)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("Họ tên: ${report.reporter_name ?: "Chưa cập nhật"}", style = MaterialTheme.typography.bodyMedium)
                    Text("Số điện thoại: ${report.reporter_phone ?: "-"}", style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Tài xế bị báo cáo
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Tài xế bị báo cáo",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                    Text("Họ tên: ${report.driver_name ?: "Chưa cập nhật"}", style = MaterialTheme.typography.bodyMedium)
                    Text("Số điện thoại: ${report.driver_phone ?: "-"}", style = MaterialTheme.typography.bodyMedium)
                    Text("Biển số xe: ${report.license_plate ?: "-"}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (report.status == "pending") {
                    // Nút giải quyết báo cáo
                    Button(
                        onClick = {
                            viewModel.resolveReport(token, report.report_id, onSuccess = onDismiss)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981)
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Giải quyết")
                    }

                    // Nút khóa tài xế nhanh
                    Button(
                        onClick = { showLockDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Khóa tài xế")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )

    // ── Dialog xác nhận khóa tài xế ──
    if (showLockDialog) {
        AlertDialog(
            onDismissRequest = { showLockDialog = false },
            title = { Text("Khóa tài khoản tài xế", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Bạn có chắc chắn muốn khóa tài khoản tài xế này do báo cáo vi phạm?")
                    OutlinedTextField(
                        value = lockReason,
                        onValueChange = { lockReason = it },
                        label = { Text("Lý do khóa tài khoản") },
                        placeholder = { Text("Nhập lý do chi tiết...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (lockReason.trim().length >= 3) {
                            viewModel.lockUser(token, report.driver_id, lockReason)
                            viewModel.resolveReport(token, report.report_id, onSuccess = {
                                showLockDialog = false
                                onDismiss()
                            })
                        }
                    },
                    enabled = lockReason.trim().length >= 3,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Khóa tài khoản")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLockDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

private fun formatDateTime(isoString: String): String {
    return try {
        // Hỗ trợ parse định dạng ISO và trả về định dạng Việt Nam
        val formatIn = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        formatIn.timeZone = TimeZone.getTimeZone("UTC")
        val date = formatIn.parse(isoString) ?: return isoString
        val formatOut = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
        formatOut.timeZone = TimeZone.getDefault()
        formatOut.format(date)
    } catch (e: Exception) {
        try {
            // Hỗ trợ trường hợp định dạng SQL timestamp thông thường
            val formatIn = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val date = formatIn.parse(isoString) ?: return isoString
            val formatOut = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
            formatOut.format(date)
        } catch (ex: Exception) {
            isoString
        }
    }
}
