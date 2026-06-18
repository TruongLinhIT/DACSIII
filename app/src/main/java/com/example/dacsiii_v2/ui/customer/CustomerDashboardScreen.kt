package com.example.dacsiii_v2.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDashboardScreen(
    token: String,
    viewModel: CustomerViewModel,
    onNavigateProfile: () -> Unit,
    onNavigateHistory: () -> Unit,
    onNavigateOrders: () -> Unit,
    onNavigateNotifications: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(token) {
        if (token.isNotBlank()) {
            viewModel.fetchProfile(token)
        }
    }

    val user = uiState.userProfile?.user
    val fullName = user?.full_name ?: "Khách hàng"
    val isVerified = user?.is_verified == "verified"
    val isPending = user?.is_verified == "pending"

    // Time-based greeting helper
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 0..11 -> "Chào buổi sáng"
            in 12..17 -> "Chào buổi chiều"
            else -> "Chào buổi tối"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Delivery Pro", 
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleLarge,
                        letterSpacing = 0.5.sp
                    ) 
                },
                actions = {
                    IconButton(onClick = onNavigateNotifications) {
                        Icon(
                            imageVector = Icons.Default.Notifications, 
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onNavigateProfile) {
                        if (!user?.avatar_url.isNullOrBlank()) {
                            AsyncImage(
                                model = normalizePhotoUrl(user?.avatar_url),
                                contentDescription = "Profile",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person, 
                                contentDescription = "Profile",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp, 
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Trang chủ") },
                    label = { Text("Trang chủ") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { 
                        selectedTab = 0 // reset selected tab locally
                        onNavigateOrders() 
                    },
                    icon = { Icon(Icons.Default.AddCircle, contentDescription = "Đặt hàng") },
                    label = { Text("Đặt hàng") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { 
                        selectedTab = 0 // reset selected tab locally
                        onNavigateHistory() 
                    },
                    icon = { Icon(Icons.Default.History, contentDescription = "Lịch sử") },
                    label = { Text("Lịch sử") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // ── Hero Section ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF0F2027),
                                Color(0xFF203A43),
                                Color(0xFF2C5364)
                            )
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 28.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "$greeting,",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "$fullName 👋",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    // Verification badge inside hero
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isVerified) Icons.Default.VerifiedUser else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isVerified) Color(0xFF4CAF50) else Color(0xFFFFC107),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                isVerified -> "Tài khoản đã định danh eKYC"
                                isPending -> "Đang chờ duyệt định danh"
                                else -> "Tài khoản chưa định danh"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Hôm nay bạn muốn đặt gì nào?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Main Action Cards
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Wide Premium Create Order Card
                    CustomerDashboardCard(
                        title = "Đặt hàng giao ngay",
                        subtitle = "Tạo đơn hàng vận chuyển mới siêu tốc",
                        icon = Icons.Default.ShoppingBag,
                        onClick = onNavigateOrders,
                        containerColor = MaterialTheme.colorScheme.primary
                    )

                    // Wide Premium History Card
                    CustomerDashboardCard(
                        title = "Lịch sử & Đơn hàng",
                        subtitle = "Theo dõi đơn hàng đang giao hoặc đã hoàn tất",
                        icon = Icons.Default.History,
                        onClick = onNavigateHistory,
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Verification alert banner - redesigned to look premium and sleek
                if (!isVerified) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPending) 
                                Color(0xFFFFC107).copy(alpha = 0.12f)
                            else 
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.12f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = if (isPending)
                                Color(0xFFFFC107).copy(alpha = 0.3f)
                            else
                                MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth().clickable { onNavigateProfile() },
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Warning",
                                tint = if (isPending) Color(0xFFE65100) else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isPending) "Hồ sơ đang chờ phê duyệt" else "Yêu cầu định danh tài khoản",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPending) Color(0xFFE65100) else MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = if (isPending)
                                        "Thông tin định danh đang được xem xét bởi quản trị viên."
                                    else
                                        "Vui lòng hoàn thành eKYC tại mục hồ sơ cá nhân.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerDashboardCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    containerColor: androidx.compose.ui.graphics.Color
) {
    val isPrimary = containerColor == MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(24.dp)
    
    val cardBackground = if (isPrimary) {
        Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.primaryContainer
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surface
            )
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .clickable { onClick() }
            .shadow(
                elevation = 6.dp,
                shape = shape,
                ambientColor = if (isPrimary) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.05f),
                spotColor = if (isPrimary) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.05f)
            ),
        shape = shape,
        border = if (isPrimary) null else androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(cardBackground)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isPrimary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isPrimary) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isPrimary) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            modifier = Modifier.size(26.dp),
                            tint = if (isPrimary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

private fun normalizePhotoUrl(photoUrl: String?): String {
    if (photoUrl.isNullOrBlank()) return ""
    return if (photoUrl.startsWith("http")) {
        photoUrl
    } else {
        "http://10.0.2.2:3000$photoUrl"
    }
}
