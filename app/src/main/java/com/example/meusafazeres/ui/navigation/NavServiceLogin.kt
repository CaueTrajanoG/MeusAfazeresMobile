package com.example.meusafazeres.ui.navigation

import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.androidx.compose.koinViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.meusafazeres.ui.screens.LoginScreen
import com.example.meusafazeres.ui.screens.RegisterScreen
import com.example.meusafazeres.ui.viewmodel.AuthViewModel
import com.example.meusafazeres.ui.viewmodel.TaskViewModel

@Composable
fun NavServiceLogin() {
    val rootNavController = rememberNavController()
    val authViewModel: AuthViewModel = koinViewModel()
    val taskViewModel: TaskViewModel = koinViewModel()

    val startDestination = if (authViewModel.currentUser != null) "home_structure" else "login"

    NavHost(
        navController = rootNavController,
        startDestination = startDestination,
        modifier = Modifier.systemBarsPadding()
    ) {
        composable("login") {
            LoginScreen(
                navController = rootNavController,
                viewModel = authViewModel
            )
        }
        
        composable("register") {
            RegisterScreen(
                navController = rootNavController,
                viewModel = authViewModel
            )
        }

        composable("home_structure") {
            NavServiceIntern(
                authViewModel = authViewModel,
                taskViewModel = taskViewModel,
                onLogout = {
                    authViewModel.logout()
                    rootNavController.navigate("login") {
                        popUpTo("home_structure") { inclusive = true }
                    }
                }
            )
        }
    }
}
