package com.example.ceramicflow_android.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.ceramicflow_android.ui.screens.CeramicDetailScreen
import com.example.ceramicflow_android.ui.screens.LoginScreen
import com.example.ceramicflow_android.ui.screens.CeramicListScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object CeramicList : Screen("ceramic_list")
    object CeramicDetail : Screen("ceramic_detail/{ceramicId}") {
        fun createRoute(ceramicId: String) = "ceramic_detail/$ceramicId"
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
                    navController.navigate(Screen.CeramicList.route) {
                        // Clear login from back stack
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                viewModel = viewModel()
            )
        }

        // Ceramic List Screen (Master)
        composable(Screen.CeramicList.route) {
            CeramicListScreen(
                onCeramicClick = { ceramicId ->
                    navController.navigate(Screen.CeramicDetail.createRoute(ceramicId))
                },
                viewModel = viewModel()
            )
        }

        // Ceramic Detail Screen (Detail)
        composable(
            route = Screen.CeramicDetail.route,
            arguments = listOf(
                navArgument("ceramicId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val ceramicId = backStackEntry.arguments?.getString("ceramicId") ?: return@composable
            CeramicDetailScreen(
                ceramicId = ceramicId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = viewModel()
            )
        }
    }
}