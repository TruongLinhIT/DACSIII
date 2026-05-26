package com.example.dacsiii_v2.ui.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dacsiii_v2.data.model.DriverEarningsBucket
import com.example.dacsiii_v2.ui.common.IosPrimaryButton
import com.example.dacsiii_v2.ui.common.IosSecondaryButton
import com.example.dacsiii_v2.ui.common.IosTextField
import com.example.dacsiii_v2.ui.common.SectionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverEarningsScreen(
    token: String,
    viewModel: DriverViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var range by rememberSaveable { mutableStateOf("day") }
    var dateInput by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(token) {
        if (token.isNotBlank()) {
            viewModel.fetchEarnings(token, range, null)
        }
    }

    LaunchedEffect(uiState.message) {
        val message = uiState.message
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thu nhập") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionCard(title = "Bộ lọc") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IosSecondaryButton(
                        text = "Ngày",
                        modifier = Modifier.weight(1f),
                        onClick = { range = "day" }
                    )
                    IosSecondaryButton(
                        text = "Tháng",
                        modifier = Modifier.weight(1f),
                        onClick = { range = "month" }
                    )
                    IosSecondaryButton(
                        text = "Năm",
                        modifier = Modifier.weight(1f),
                        onClick = { range = "year" }
                    )
                }

                IosTextField(
                    value = dateInput,
                    onValueChange = { dateInput = it },
                    label = { Text("Ngày/Tháng/Năm (tùy chọn)") },
                    modifier = Modifier.fillMaxWidth()
                )

                IosPrimaryButton(
                    text = "Tải dữ liệu",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val dateValue = dateInput.trim().ifBlank { null }
                        viewModel.fetchEarnings(token, range, dateValue)
                    }
                )
            }

            val earnings = uiState.earnings
            if (earnings == null) {
                Text("Chưa có dữ liệu thu nhập.")
            } else {
                SectionCard(title = "Tổng quan") {
                    Text("Tổng thu nhập: ${earnings.totals.total_earning}")
                    Text("Số đơn: ${earnings.totals.order_count}")
                }

                if (earnings.breakdown.isNotEmpty()) {
                    SectionCard(title = "Chi tiết") {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(bottom = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(earnings.breakdown) { bucket ->
                                EarningsBucketRow(bucket)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EarningsBucketRow(bucket: DriverEarningsBucket) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = bucket.bucket)
        Text(text = "Thu nhập: ${bucket.total_earning} • Đơn: ${bucket.order_count}")
    }
}

