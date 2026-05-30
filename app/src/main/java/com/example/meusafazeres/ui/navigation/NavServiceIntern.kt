package com.example.meusafazeres.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.meusafazeres.model.Task
import com.example.meusafazeres.ui.model.NavItem
import com.example.meusafazeres.ui.screens.CadastroTaskScreen
import com.example.meusafazeres.ui.screens.MainScreen
import com.example.meusafazeres.ui.viewmodel.AuthViewModel
import com.example.meusafazeres.ui.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavServiceIntern(
    authViewModel: AuthViewModel,
    taskViewModel: TaskViewModel,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    var selectedIndex by remember { mutableIntStateOf(0) }
    var mostrarDialog by remember { mutableStateOf(false) }

    if (mostrarDialog) {
        AlertDialog(
            onDismissRequest = { mostrarDialog = false },
            title = { Text(text = "Confirmar Saída") },
            text = { Text(text = "Você realmente deseja sair do aplicativo?") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialog = false
                    onLogout()
                }) {
                    Text("Sair", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    val navItemList = listOf(
        NavItem("Afazeres", Icons.Default.List, "main"),
        NavItem("Novo", Icons.Default.Add, "cadastro_task"),
        NavItem("Sair", Icons.Default.ExitToApp, "sair")
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Meus Afazeres",
                        style = MaterialTheme.typography.headlineLarge,
                        fontSize = 36.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar {
                navItemList.forEachIndexed { index, navItem ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = {
                            selectedIndex = index
                            if (navItem.rota == "sair") {
                                mostrarDialog = true
                            } else {
                                navController.navigate(navItem.rota) {
                                    // Evita acumular instâncias da mesma tela na pilha
                                    popUpTo("main") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(imageVector = navItem.icon, contentDescription = navItem.label) },
                        label = { Text(text = navItem.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "main",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("main") {
                MainScreen(navController, authViewModel, taskViewModel)
            }
            composable("cadastro_task") {
                CadastroTaskScreen(navController, authViewModel, taskViewModel)
            }
        }
    }
}
