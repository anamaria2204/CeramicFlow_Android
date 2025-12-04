package com.example.ceramicflow_android.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.ceramicflow_android.ui.screens.BookingDetailScreen
import com.example.ceramicflow_android.ui.screens.BookingListScreen
import com.example.ceramicflow_android.ui.screens.LoginScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object BookingList : Screen("booking_list")
    object BookingDetail : Screen("booking_detail/{bookingId}") {
        fun createRoute(bookingId: String) = "booking_detail/$bookingId"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Login Screen
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.BookingList.route) {
                        // Clear login from back stack
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Booking List Screen (Master)
        composable(Screen.BookingList.route) {
            BookingListScreen(
                onBookingClick = { bookingId ->
                    navController.navigate(Screen.BookingDetail.createRoute(bookingId))
                }
            )
        }

        // Booking Detail Screen (Detail)
        composable(
            route = Screen.BookingDetail.route,
            arguments = listOf(
                navArgument("bookingId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: return@composable
            BookingDetailScreen(
                bookingId = bookingId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
