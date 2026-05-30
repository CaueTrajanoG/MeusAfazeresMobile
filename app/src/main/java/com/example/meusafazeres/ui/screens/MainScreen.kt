package com.example.meusafazeres.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.meusafazeres.ui.components.TaskCard
import com.example.meusafazeres.ui.viewmodel.AuthViewModel
import com.example.meusafazeres.ui.viewmodel.TaskListState
import com.example.meusafazeres.ui.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    taskViewModel: TaskViewModel
) {
    var searchText by remember { mutableStateOf("") }
    val userId = authViewModel.currentUser?.uid ?: ""
    val taskListState by taskViewModel.taskListState

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            taskViewModel.loadTasks(userId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val userData by authViewModel.currentUserData
        val currentUser = authViewModel.currentUser
        
        if (userData != null || currentUser != null) {
            Text(
                text = "Olá, ${userData?.nome ?: currentUser?.email ?: "Usuário"}!",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Search Bar
        OutlinedTextField(
            value = searchText,
            onValueChange = { 
                searchText = it
                taskViewModel.loadTasks(userId, searchText)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Buscar afazeres...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true
        )

        when (taskListState) {
            is TaskListState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is TaskListState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = (taskListState as TaskListState.Error).message, color = Color.Red)
                }
            }
            is TaskListState.Success -> {
                val tasks = (taskListState as TaskListState.Success).tasks
                if (tasks.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Nenhum afazer encontrado", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(tasks) { task ->
                            TaskCard(
                                task = task,
                                onStatusChange = { 
                                    taskViewModel.toggleTaskStatus(task)
                                },
                                onDelete = {
                                    taskViewModel.deleteTask(task.id!!, userId)
                                }
                            )
                        }
                    }
                }
            }
            else -> {}
        }
    }
}
