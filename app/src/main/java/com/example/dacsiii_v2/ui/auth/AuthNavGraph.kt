package com.example.dacsiii_v2.ui.auth

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dacsiii_v2.ui.admin.AdminDashboardScreen
import com.example.dacsiii_v2.ui.admin.AdminReportsScreen
import com.example.dacsiii_v2.ui.admin.AdminViewModel
import com.example.dacsiii_v2.ui.admin.UserDetailScreen
import com.example.dacsiii_v2.ui.admin.UserManagementScreen
import com.example.dacsiii_v2.ui.customer.CustomerCreateOrderScreen
import com.example.dacsiii_v2.ui.customer.CustomerDashboardScreen
import com.example.dacsiii_v2.ui.customer.CustomerMapPickerScreen
import com.example.dacsiii_v2.ui.customer.CustomerOrderDetailScreen
import com.example.dacsiii_v2.ui.customer.CustomerOrderHistoryScreen
import com.example.dacsiii_v2.ui.customer.CustomerProfileScreen
import com.example.dacsiii_v2.ui.customer.CustomerViewModel
import com.example.dacsiii_v2.ui.driver.DriverActiveOrdersScreen
import com.example.dacsiii_v2.ui.driver.DriverAvailableOrdersScreen
import com.example.dacsiii_v2.ui.driver.DriverDashboardScreen
import com.example.dacsiii_v2.ui.driver.DriverEarningsScreen
import com.example.dacsiii_v2.ui.driver.DriverHistoryScreen
import com.example.dacsiii_v2.ui.driver.DriverOrderDetailScreen
import com.example.dacsiii_v2.ui.driver.DriverProfileScreen
import com.example.dacsiii_v2.ui.driver.DriverViewModel
import com.example.dacsiii_v2.ui.driver.DriverWalletScreen
import com.example.dacsiii_v2.data.remote.RetrofitClient
import com.example.dacsiii_v2.data.repository.UserRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import com.example.dacsiii_v2.ui.notifications.NotificationViewModel
import com.example.dacsiii_v2.ui.notifications.NotificationsScreen

@Composable
fun AuthNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    var userToken by remember { mutableStateOf("") }
    val context = LocalContext.current
    val userRepository = remember { UserRepository(RetrofitClient.api) }

    LaunchedEffect(userToken) {
        val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        if (userToken.isNotBlank()) {
            sharedPrefs.edit().putString("jwt_token", userToken).apply()
            val fcmToken = runCatching { FirebaseMessaging.getInstance().token.await() }.getOrNull()
            if (!fcmToken.isNullOrBlank()) {
                userRepository.registerDeviceToken(userToken, fcmToken)
            }
        } else {
            sharedPrefs.edit().remove("jwt_token").apply()
        }
    }

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                modifier = modifier,
                onNavigateRegister = { navController.navigate("register/phone") },
                onNavigateForgotPassword = { navController.navigate("forgot_password") },
                onLoginSuccess = { token, role ->
                    userToken = token
                    when (role) {
                        "admin" -> {
                            navController.navigate("admin_dashboard") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                        "customer" -> {
                            navController.navigate("customer_dashboard") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                        "driver" -> {
                            navController.navigate("driver_dashboard") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                        else -> {
                            // Xử lý các role khác nếu cần
                        }
                    }
                }
            )
        }

        composable("forgot_password") {
            ResetPasswordScreen(
                modifier = modifier,
                onBack = { navController.popBackStack() }
            )
        }

        composable("admin_dashboard") {
            val adminViewModel: AdminViewModel = viewModel()
            AdminDashboardScreen(
                token = userToken,
                viewModel = adminViewModel,
                onNavigateUserManagement = { navController.navigate("admin/users") },
                onNavigateVerification = { 
                    navController.navigate("admin/users?filter=pending") 
                },
                onNavigateLockedUsers = {
                    navController.navigate("admin/users?filter=locked")
                },
                onNavigateNotifications = { navController.navigate("notifications") },
                onNavigateReports = { navController.navigate("admin/reports") },
                onLogout = {
                    userToken = ""
                    navController.navigate("login") {
                        popUpTo("admin_dashboard") { inclusive = true }
                    }
                }
            )
        }

        composable("admin/reports") {
            val adminViewModel: AdminViewModel = viewModel()
            AdminReportsScreen(
                token = userToken,
                viewModel = adminViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "admin/users?filter={filter}",
            arguments = listOf(navArgument("filter") { 
                nullable = true
                defaultValue = null 
            })
        ) { backStackEntry ->
            val filter = backStackEntry.arguments?.getString("filter")
            val adminViewModel: AdminViewModel = viewModel()
            UserManagementScreen(
                token = userToken,
                initialFilter = filter,
                viewModel = adminViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateDetail = { userId -> 
                    navController.navigate("admin/users/$userId") 
                }
            )
        }

        composable(
            route = "admin/users/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            val adminViewModel: AdminViewModel = viewModel()
            UserDetailScreen(
                token = userToken,
                userId = userId,
                viewModel = adminViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("customer_dashboard") { backStackEntry ->
            val customerViewModel: CustomerViewModel = viewModel(backStackEntry)
            CustomerDashboardScreen(
                token = userToken,
                viewModel = customerViewModel,
                onNavigateProfile = { navController.navigate("customer/profile") },
                onNavigateHistory = { navController.navigate("customer/orders") },
                onNavigateOrders = { navController.navigate("customer/orders/create") },
                onNavigateNotifications = { navController.navigate("notifications") },
                onLogout = {
                    userToken = ""
                    navController.navigate("login") {
                        popUpTo("customer_dashboard") { inclusive = true }
                    }
                }
            )
        }

        composable("customer/profile") {
            val parentEntry = remember { navController.getBackStackEntry("customer_dashboard") }
            val customerViewModel: CustomerViewModel = viewModel(parentEntry)
            CustomerProfileScreen(
                token = userToken,
                viewModel = customerViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("customer/orders/create") {
            val parentEntry = remember { navController.getBackStackEntry("customer_dashboard") }
            val customerViewModel: CustomerViewModel = viewModel(parentEntry)
            CustomerCreateOrderScreen(
                token = userToken,
                viewModel = customerViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigatePickPickup = { navController.navigate("customer/map/pickup") },
                onNavigatePickDelivery = { navController.navigate("customer/map/delivery") }
            )
        }

        composable("customer/orders") {
            val parentEntry = remember { navController.getBackStackEntry("customer_dashboard") }
            val customerViewModel: CustomerViewModel = viewModel(parentEntry)
            CustomerOrderHistoryScreen(
                token = userToken,
                viewModel = customerViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateDetail = { orderId -> navController.navigate("customer/orders/$orderId") }
            )
        }

        composable(
            route = "customer/orders/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.IntType })
        ) { backStackEntry ->
            val parentEntry = remember { navController.getBackStackEntry("customer_dashboard") }
            val customerViewModel: CustomerViewModel = viewModel(parentEntry)
            val orderId = backStackEntry.arguments?.getInt("orderId") ?: 0
            CustomerOrderDetailScreen(
                token = userToken,
                orderId = orderId,
                viewModel = customerViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("customer/map/pickup") {
            val parentEntry = remember { navController.getBackStackEntry("customer_dashboard") }
            val customerViewModel: CustomerViewModel = viewModel(parentEntry)
            val uiState by customerViewModel.uiState.collectAsState()
            CustomerMapPickerScreen(
                title = "Chọn vị trí lấy hàng",
                initialLat = uiState.pickupLat,
                initialLng = uiState.pickupLng,
                onPicked = { lat, lng ->
                    customerViewModel.setPickupLocation(lat, lng)
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("customer/map/delivery") {
            val parentEntry = remember { navController.getBackStackEntry("customer_dashboard") }
            val customerViewModel: CustomerViewModel = viewModel(parentEntry)
            val uiState by customerViewModel.uiState.collectAsState()
            CustomerMapPickerScreen(
                title = "Chọn vị trí giao hàng",
                initialLat = uiState.deliveryLat,
                initialLng = uiState.deliveryLng,
                onPicked = { lat, lng ->
                    customerViewModel.setDeliveryLocation(lat, lng)
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("notifications") {
            val notificationsViewModel: NotificationViewModel = viewModel()
            NotificationsScreen(
                token = userToken,
                viewModel = notificationsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- Register Flow ---
        navigation(startDestination = "register/phone", route = "register") {
            composable("register/phone") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("register")
                }
                val registerViewModel: RegisterViewModel = viewModel(parentEntry)
                RegisterPhoneScreen(
                    modifier = modifier,
                    viewModel = registerViewModel,
                    onOtpReady = { navController.navigate("register/otp") },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("register/otp") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("register")
                }
                val registerViewModel: RegisterViewModel = viewModel(parentEntry)
                RegisterOtpScreen(
                    modifier = modifier,
                    viewModel = registerViewModel,
                    onVerified = { navController.navigate("register/profile") },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("register/profile") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("register")
                }
                val registerViewModel: RegisterViewModel = viewModel(parentEntry)
                RegisterProfileScreen(
                    modifier = modifier,
                    viewModel = registerViewModel,
                    onBackToLogin = {
                        navController.popBackStack("login", inclusive = false)
                    }
                )
            }
        }

        // --- Driver Routes ---
        composable("driver_dashboard") {
            DriverDashboardScreen(
                onNavigateAvailable = { navController.navigate("driver/orders/available") },
                onNavigateActive = { navController.navigate("driver/orders/active") },
                onNavigateHistory = { navController.navigate("driver/orders/history") },
                onNavigateEarnings = { navController.navigate("driver/earnings") },
                onNavigateWallet = { navController.navigate("driver/wallet") },
                onNavigateProfile = { navController.navigate("driver/profile") },
                onNavigateNotifications = { navController.navigate("notifications") },
                onLogout = {
                    userToken = ""
                    navController.navigate("login") {
                        popUpTo("driver_dashboard") { inclusive = true }
                    }
                }
            )
        }

        composable("driver/orders/available") {
            val parentEntry = remember { navController.getBackStackEntry("driver_dashboard") }
            val driverViewModel: DriverViewModel = viewModel(parentEntry)
            DriverAvailableOrdersScreen(
                token = userToken,
                viewModel = driverViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("driver/orders/active") {
            val parentEntry = remember { navController.getBackStackEntry("driver_dashboard") }
            val driverViewModel: DriverViewModel = viewModel(parentEntry)
            DriverActiveOrdersScreen(
                token = userToken,
                viewModel = driverViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateDetail = { orderId -> navController.navigate("driver/orders/$orderId") }
            )
        }

        composable("driver/orders/history") {
            val parentEntry = remember { navController.getBackStackEntry("driver_dashboard") }
            val driverViewModel: DriverViewModel = viewModel(parentEntry)
            DriverHistoryScreen(
                token = userToken,
                viewModel = driverViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "driver/orders/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.IntType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getInt("orderId") ?: 0
            val parentEntry = remember { navController.getBackStackEntry("driver_dashboard") }
            val driverViewModel: DriverViewModel = viewModel(parentEntry)
            DriverOrderDetailScreen(
                token = userToken,
                orderId = orderId,
                viewModel = driverViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("driver/wallet") {
            val parentEntry = remember { navController.getBackStackEntry("driver_dashboard") }
            val driverViewModel: DriverViewModel = viewModel(parentEntry)
            DriverWalletScreen(
                token = userToken,
                viewModel = driverViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("driver/profile") {
            val parentEntry = remember { navController.getBackStackEntry("driver_dashboard") }
            val driverViewModel: DriverViewModel = viewModel(parentEntry)
            DriverProfileScreen(
                token = userToken,
                viewModel = driverViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("driver/earnings") {
            val parentEntry = remember { navController.getBackStackEntry("driver_dashboard") }
            val driverViewModel: DriverViewModel = viewModel(parentEntry)
            DriverEarningsScreen(
                token = userToken,
                viewModel = driverViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}