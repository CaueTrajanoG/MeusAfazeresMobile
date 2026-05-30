package com.example.meusafazeres.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.meusafazeres.model.Task
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
    var taskToDelete: Task? by remember { mutableStateOf<Task?>(null) }
    
    val userId = authViewModel.currentUser?.uid ?: ""
    val taskListState by taskViewModel.taskListState

    if (taskToDelete != null) {
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            title = { 
                Text(
                    text = "Excluir Afazer",
                    style = MaterialTheme.typography.headlineLarge,
                    fontSize = 28.sp,
                    color = MaterialTheme.colorScheme.primary
                ) 
            },
            text = { Text(text = "Tem certeza que deseja excluir o afazer \"${taskToDelete?.titulo}\"? Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        taskToDelete?.id?.let { id ->
                            taskViewModel.deleteTask(id, userId)
                        }
                        taskToDelete = null
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

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
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = Color.Black // Keep greeting black for legibility
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
            trailingIcon = {
                if (searchText.isNotEmpty()) {
                    IconButton(onClick = {
                        searchText = ""
                        taskViewModel.loadTasks(userId, null)
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Limpar busca")
                    }
                }
            },
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
                val isLoadingMore by taskViewModel.isLoadingMore
                
                if (tasks.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Nenhum afazer encontrado", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    val listState = rememberLazyListState()

                    // Detect scroll to bottom
                    val shouldLoadMore = remember {
                        derivedStateOf {
                            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                                ?: return@derivedStateOf false

                            lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 1
                        }
                    }

                    LaunchedEffect(shouldLoadMore.value) {
                        if (shouldLoadMore.value && !isLoadingMore) {
                            taskViewModel.loadNextPage(userId)
                        }
                    }

                    LazyColumn(
                        state = listState,
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
                                    taskToDelete = task
                                }
                            )
                        }

                        if (isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
            else -> {}
        }
    }
}