package com.example.dacsiii_v2.ui.admin

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

// Màu sắc premium
private val GradientStart = Color(0xFF667EEA)
private val GradientEnd = Color(0xFF764BA2)
private val CardBlue = Color(0xFF3B82F6)
private val CardAmber = Color(0xFFF59E0B)
private val CardRed = Color(0xFFEF4444)
private val CardGreen = Color(0xFF10B981)
private val CardPurple = Color(0xFF8B5CF6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    token: String,
    viewModel: AdminViewModel,
    onNavigateUserManagement: () -> Unit,
    onNavigateVerification: () -> Unit,
    onNavigateLockedUsers: () -> Unit,
    onNavigateNotifications: () -> Unit,
    onNavigateReports: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // State quản lý thời gian thống kê
    var selectedRange by remember { mutableStateOf("day") }
    val calendar = remember { Calendar.getInstance() }
    var selectedYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH) + 1) }
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val formattedDateForApi = remember(selectedRange, selectedYear, selectedMonth, selectedDateMillis) {
        when (selectedRange) {
            "year" -> "$selectedYear"
            "month" -> String.format(Locale.US, "%d-%02d", selectedYear, selectedMonth)
            else -> SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(selectedDateMillis))
        }
    }

    val displayLabel = remember(selectedRange, selectedYear, selectedMonth, selectedDateMillis) {
        when (selectedRange) {
            "year" -> "Năm $selectedYear"
            "month" -> "Tháng $selectedMonth/$selectedYear"
            else -> SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(selectedDateMillis))
        }
    }

    LaunchedEffect(token, selectedRange, formattedDateForApi) {
        if (token.isNotBlank()) {
            viewModel.fetchCommissionSummary(token, selectedRange, formattedDateForApi)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // ── Header với gradient ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(GradientStart, GradientEnd)
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 28.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Xin chào ",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Quản Trị Viên",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = onNavigateNotifications) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "Thông báo",
                                    tint = Color.White
                                )
                            }
                            IconButton(onClick = onLogout) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = "Đăng xuất",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Quản lý người dùng và duyệt hồ sơ nhanh chóng",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Commission Summary ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "Chiết khấu đã thu",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedRange == "day",
                        onClick = { selectedRange = "day" },
                        label = { Text("Ngày") }
                    )
                    FilterChip(
                        selected = selectedRange == "month",
                        onClick = { selectedRange = "month" },
                        label = { Text("Tháng") }
                    )
                    FilterChip(
                        selected = selectedRange == "year",
                        onClick = { selectedRange = "year" },
                        label = { Text("Năm") }
                    )
                }
                
                // Select Box trigger (Dạng nút bấm mở chọn ngày/tháng/năm)
                OutlinedCard(
                    onClick = {
                        if (selectedRange == "day") showDatePicker = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    if (selectedRange == "day") {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(displayLabel, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                            Icon(Icons.Default.ArrowDropDown, null)
                        }
                    } else {
                        Row(
                            modifier = Modifier.padding(8.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (selectedRange == "month") {
                                MonthYearDropdown(
                                    label = "Tháng",
                                    items = (1..12).toList(),
                                    selectedItem = selectedMonth,
                                    onItemSelected = { month -> selectedMonth = month },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                            MonthYearDropdown(
                                label = "Năm",
                                items = (currentYear downTo currentYear - 5).toList(),
                                selectedItem = selectedYear,
                                onItemSelected = { year -> selectedYear = year },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (uiState.isCommissionLoading) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Đang tải dữ liệu...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            val totals = uiState.commissionSummary?.totals
                            Text(
                                text = "Tổng thu nhập $displayLabel",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatCurrency(totals?.total_commission ?: 0.0),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Số đơn: ${totals?.order_count ?: 0}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Menu chức năng ──
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "Chức năng",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        AdminMenuCard(
                            title = "Quản lý\nngười dùng",
                            subtitle = "Danh sách & phân quyền",
                            icon = Icons.Default.People,
                            accentColor = CardBlue,
                            onClick = onNavigateUserManagement
                        )
                    }
                    item {
                        AdminMenuCard(
                            title = "Duyệt\nhồ sơ eKYC",
                            subtitle = "Xét duyệt định danh",
                            icon = Icons.Default.AssignmentInd,
                            accentColor = CardAmber,
                            onClick = onNavigateVerification
                        )
                    }
                    item {
                        AdminMenuCard(
                            title = "Tài khoản\nbị khóa",
                            subtitle = "Quản lý vi phạm",
                            icon = Icons.Default.Lock,
                            accentColor = CardRed,
                            onClick = onNavigateLockedUsers
                        )
                    }
                    item {
                        AdminMenuCard(
                            title = "Thông báo",
                            subtitle = "Danh sách thông báo",
                            icon = Icons.Default.Notifications,
                            accentColor = CardGreen,
                            onClick = onNavigateNotifications
                        )
                    }
                    item {
                        AdminMenuCard(
                            title = "Báo cáo\ntài xế",
                            subtitle = "Xử lý khiếu nại",
                            icon = Icons.Default.Warning,
                            accentColor = CardPurple,
                            onClick = onNavigateReports
                        )
                    }
                }
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        selectedDateMillis = it
                    }
                    showDatePicker = false
                }) {
                    Text("Xác nhận")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Hủy")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun AdminMenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100),
        label = "scale"
    )
    val bgAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.12f else 0.06f,
        animationSpec = tween(100),
        label = "bgAlpha"
    )

    val shape = RoundedCornerShape(24.dp)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .scale(scale)
            .shadow(
                elevation = 6.dp,
                shape = shape,
                ambientColor = accentColor.copy(alpha = 0.15f),
                spotColor = accentColor.copy(alpha = 0.15f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Accent circle glow ở góc trên phải
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 20.dp, y = (-20).dp)
                    .background(
                        color = accentColor.copy(alpha = bgAlpha),
                        shape = CircleShape
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = accentColor.copy(alpha = 0.12f)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier
                            .padding(12.dp)
                            .size(28.dp),
                        tint = accentColor
                    )
                }

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
