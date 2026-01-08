package com.example.ceramicflow_android.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.ceramicflow_android.ui.screens.BookingDetailScreen // IMPORTUL NOU
import com.example.ceramicflow_android.ui.screens.CreateCeramicScreen
import com.example.ceramicflow_android.ui.screens.CeramicListScreen
import com.example.ceramicflow_android.ui.screens.LoginScreen
import com.example.ceramicflow_android.ui.screens.ScheduleBookingScreen
import com.example.ceramicflow_android.ui.viewmodel.AddBookingViewModel
import com.example.ceramicflow_android.ui.viewmodel.BookingListViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object CeramicList : Screen("ceramic_list")

    // MODIFICARE: Am redenumit ruta si parametrul pentru a fi BookingDetail
    object BookingDetail : Screen("booking_detail/{bookingId}") {
        fun createRoute(bookingId: String) = "booking_detail/$bookingId"
    }

    object AddBookingFlow : Screen("add_booking_flow")
    object CreateCeramic : Screen("create_ceramic")
    object ScheduleBooking : Screen("schedule_booking")
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    // Folosim un ViewModel partajat sau instanțiat aici pentru a fi sigur că datele persistă
    // Notă: BookingDetailScreen are nevoie de lista încărcată.
    // În mod ideal, ViewModel-ul ar trebui să ia datele din Repository (Room), deci o nouă instanță e OK.

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
            // Aici putem folosi viewModel() simplu
            val bookingListViewModel: BookingListViewModel = viewModel()

            CeramicListScreen(
                onAddBookingClick = { navController.navigate(Screen.AddBookingFlow.route) },
                // MODIFICARE: Trimitem bookingId către ruta nouă
                onBookingClick = { bookingId ->
                    navController.navigate(Screen.BookingDetail.createRoute(bookingId))
                },
                viewModel = bookingListViewModel
            )
        }

        // MODIFICARE: Ruta pentru BookingDetailScreen
        composable(
            route = Screen.BookingDetail.route,
            arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: return@composable

            // Instanțiem noul ecran
            BookingDetailScreen(
                bookingId = bookingId,
                viewModel = viewModel(), // Va încărca datele din baza de date locală
                onBack = { navController.popBackStack() }
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