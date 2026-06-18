package com.example.dacsiii_v2.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacsiii_v2.data.model.CommissionBucket
import java.text.SimpleDateFormat
import java.util.*

private val GradientStart = Color(0xFF667EEA)
private val GradientEnd = Color(0xFF764BA2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommissionManagementScreen(
    token: String,
    viewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // State quản lý thời gian
    var selectedRange by remember { mutableStateOf("month") }
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

    val displayDate = remember(selectedRange, selectedYear, selectedMonth, selectedDateMillis) {
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
        topBar = {
            TopAppBar(
                title = { Text("Quản lý thu nhập", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.background(
                    brush = Brush.linearGradient(
                        colors = listOf(GradientStart, GradientEnd)
                    )
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Thống kê chiết khấu",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // --- BỘ LỌC THỜI GIAN (Premium Select Box) ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FilterAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Lọc theo thời gian", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Range Selection
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        val options = listOf("day" to "Ngày", "month" to "Tháng", "year" to "Năm")
                        options.forEachIndexed { index, pair ->
                            SegmentedButton(
                                selected = selectedRange == pair.first,
                                onClick = { selectedRange = pair.first },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
                            ) {
                                Text(pair.second, fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Select Boxes
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (selectedRange == "day") {
                            OutlinedCard(
                                onClick = { showDatePicker = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(displayDate, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                    }
                                    Icon(Icons.Default.ArrowDropDown, null)
                                }
                            }
                        } else {
                            if (selectedRange == "month") {
                                MonthYearDropdown(
                                    label = "Tháng",
                                    items = (1..12).toList(),
                                    selectedItem = selectedMonth,
                                    onItemSelected = { month: Int -> selectedMonth = month },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                            MonthYearDropdown(
                                label = "Năm",
                                items = (currentYear downTo currentYear - 5).toList(),
                                selectedItem = selectedYear,
                                onItemSelected = { year: Int -> selectedYear = year },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (uiState.isCommissionLoading) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Đang tải dữ liệu...")
                    } else {
                        val totals = uiState.commissionSummary?.totals
                        Text(
                            text = "Tổng thu nhập $displayDate",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = formatCurrency(totals?.total_commission ?: 0.0),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Số lượng đơn hàng", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "${totals?.order_count ?: 0}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            if (!uiState.isCommissionLoading && (uiState.commissionSummary?.breakdown?.isNotEmpty() == true)) {
                Text(
                    text = "Chi tiết doanh thu",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.commissionSummary?.breakdown ?: emptyList()) { item ->
                        BreakdownItem(item, selectedRange)
                    }
                }
            } else if (!uiState.isCommissionLoading && uiState.commissionSummary?.breakdown?.isEmpty() == true && selectedRange != "day") {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Không có dữ liệu chi tiết cho thời gian này", color = Color.Gray)
                }
            }
        }
    }

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
fun BreakdownItem(item: CommissionBucket, range: String) {
    val displayBucket = remember(item.bucket, range) {
        try {
            when (range) {
                "year" -> {
                    val date = SimpleDateFormat("yyyy-MM", Locale.US).parse(item.bucket)
                    SimpleDateFormat("MM/yyyy", Locale.US).format(date!!)
                }
                "month" -> {
                    val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(item.bucket)
                    SimpleDateFormat("dd/MM", Locale.US).format(date!!)
                }
                else -> item.bucket
            }
        } catch (e: Exception) { item.bucket }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = displayBucket,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${item.order_count} đơn hàng",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = formatCurrency(item.total_commission),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
