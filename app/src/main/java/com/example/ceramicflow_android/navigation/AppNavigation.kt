package com.example.ceramicflow_android.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.ceramicflow_android.ui.screens.CreateCeramicScreen
import com.example.ceramicflow_android.ui.screens.CeramicDetailScreen
import com.example.ceramicflow_android.ui.screens.CeramicListScreen
import com.example.ceramicflow_android.ui.screens.LoginScreen
import com.example.ceramicflow_android.ui.screens.ScheduleBookingScreen
import com.example.ceramicflow_android.ui.viewmodel.AddBookingViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object CeramicList : Screen("ceramic_list")
    object CeramicDetail : Screen("ceramic_detail/{ceramicId}") {
        fun createRoute(ceramicId: String) = "ceramic_detail/$ceramicId"
    }
    // Routes for the nested graph
    object AddBookingFlow : Screen("add_booking_flow")
    object CreateCeramic : Screen("create_ceramic")
    object ScheduleBooking : Screen("schedule_booking")
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
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { navController.navigate(Screen.CeramicList.route) { popUpTo(Screen.Login.route) { inclusive = true } } },
                viewModel = viewModel()
            )
        }

        composable(Screen.CeramicList.route) {
            CeramicListScreen(
                onAddBookingClick = { navController.navigate(Screen.AddBookingFlow.route) },
                // Adăugăm parametrul lipsă și corectăm eroarea de scriere
                onBookingClick = { ceramicId ->
                    navController.navigate(Screen.CeramicDetail.createRoute(ceramicId))
                },
                viewModel = viewModel()
            )
        }

        composable(
            route = Screen.CeramicDetail.route,
            arguments = listOf(navArgument("ceramicId") { type = NavType.StringType })
        ) { backStackEntry ->
            val ceramicId = backStackEntry.arguments?.getString("ceramicId") ?: return@composable
            CeramicDetailScreen(
                ceramicId = ceramicId,
                onNavigateBack = { navController.popBackStack() },
                viewModel = viewModel()
            )
        }

        // --- Nested graph for the booking creation flow ---
        navigation(startDestination = Screen.CreateCeramic.route, route = Screen.AddBookingFlow.route) {
            composable(Screen.CreateCeramic.route) {
                val addBookingViewModel = it.sharedViewModel<AddBookingViewModel>(navController)
                CreateCeramicScreen(
                    viewModel = addBookingViewModel,
                    onNext = { navController.navigate(Screen.ScheduleBooking.route) }
                )
            }

            composable(Screen.ScheduleBooking.route) {
                val addBookingViewModel = it.sharedViewModel<AddBookingViewModel>(navController)
                ScheduleBookingScreen(
                    viewModel = addBookingViewModel,
                    onBookingSuccess = { navController.popBackStack(Screen.AddBookingFlow.route, true) }
                )
            }
        }
    }
}

// Helper function to get a shared ViewModel scoped to the parent navigation graph
@Composable
inline fun <reified T : ViewModel> NavBackStackEntry.sharedViewModel(navController: NavController): T {
    val navGraphRoute = destination.parent?.route ?: return viewModel()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return viewModel(parentEntry)
}