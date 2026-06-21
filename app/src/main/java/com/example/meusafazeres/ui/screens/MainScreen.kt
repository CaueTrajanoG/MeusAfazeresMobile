package com.example.meusafazeres.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.meusafazeres.model.Priority
import com.example.meusafazeres.model.Task
import com.example.meusafazeres.model.TaskStatus
import com.example.meusafazeres.ui.components.TaskCard
import com.example.meusafazeres.ui.viewmodel.AuthViewModel
import com.example.meusafazeres.ui.viewmodel.AuthUIState
import com.example.meusafazeres.ui.viewmodel.TaskUIState
import com.example.meusafazeres.ui.viewmodel.TaskViewModel
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    taskViewModel: TaskViewModel,
    isHeaderExpanded: Boolean = true,
    onToggleHeader: () -> Unit = {}
) {
    var searchText by remember { mutableStateOf("") }
    var taskToDelete: Task? by remember { mutableStateOf<Task?>(null) }
    
    val authState by authViewModel.uiState.collectAsState()
    val userId = (authState as? AuthUIState.Success)?.user?.uid ?: ""
    val taskListState by taskViewModel.uiState.collectAsState()

    // Multiselect Filter states
    var showFilterDialog by remember { mutableStateOf(false) }
    var statusFilters by remember { mutableStateOf(emptySet<TaskStatus>()) }
    var priorityFilters by remember { mutableStateOf(emptySet<Priority>()) }

    var tempStatusFilters by remember { mutableStateOf(emptySet<TaskStatus>()) }
    var tempPriorityFilters by remember { mutableStateOf(emptySet<Priority>()) }

    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            containerColor = Color.White,
            tonalElevation = 0.dp,
            title = {
                Text(
                    text = "Filtrar Afazeres",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Status",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChipCustom(
                            text = "Todos",
                            selected = tempStatusFilters.isEmpty(),
                            onClick = { tempStatusFilters = emptySet() }
                        )
                        FilterChipCustom(
                            text = "Pendente",
                            selected = tempStatusFilters.contains(TaskStatus.PENDENTE),
                            onClick = {
                                tempStatusFilters = if (tempStatusFilters.contains(TaskStatus.PENDENTE)) {
                                    tempStatusFilters - TaskStatus.PENDENTE
                                } else {
                                    tempStatusFilters + TaskStatus.PENDENTE
                                }
                            }
                        )
                        FilterChipCustom(
                            text = "Feito",
                            selected = tempStatusFilters.contains(TaskStatus.FEITO),
                            onClick = {
                                tempStatusFilters = if (tempStatusFilters.contains(TaskStatus.FEITO)) {
                                    tempStatusFilters - TaskStatus.FEITO
                                } else {
                                    tempStatusFilters + TaskStatus.FEITO
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Prioridade",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChipCustom(
                                text = "Todas",
                                selected = tempPriorityFilters.isEmpty(),
                                onClick = { tempPriorityFilters = emptySet() }
                            )
                            FilterChipCustom(
                                text = "Alta",
                                selected = tempPriorityFilters.contains(Priority.ALTA),
                                onClick = {
                                    tempPriorityFilters = if (tempPriorityFilters.contains(Priority.ALTA)) {
                                        tempPriorityFilters - Priority.ALTA
                                    } else {
                                        tempPriorityFilters + Priority.ALTA
                                    }
                                }
                            )
                            FilterChipCustom(
                                text = "Média",
                                selected = tempPriorityFilters.contains(Priority.MEDIA),
                                onClick = {
                                    tempPriorityFilters = if (tempPriorityFilters.contains(Priority.MEDIA)) {
                                        tempPriorityFilters - Priority.MEDIA
                                    } else {
                                        tempPriorityFilters + Priority.MEDIA
                                    }
                                }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChipCustom(
                                text = "Leve",
                                selected = tempPriorityFilters.contains(Priority.LEVE),
                                onClick = {
                                    tempPriorityFilters = if (tempPriorityFilters.contains(Priority.LEVE)) {
                                        tempPriorityFilters - Priority.LEVE
                                    } else {
                                        tempPriorityFilters + Priority.LEVE
                                    }
                                }
                            )
                            FilterChipCustom(
                                text = "Normal",
                                selected = tempPriorityFilters.contains(Priority.NORMAL),
                                onClick = {
                                    tempPriorityFilters = if (tempPriorityFilters.contains(Priority.NORMAL)) {
                                        tempPriorityFilters - Priority.NORMAL
                                    } else {
                                        tempPriorityFilters + Priority.NORMAL
                                    }
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        statusFilters = tempStatusFilters
                        priorityFilters = tempPriorityFilters
                        showFilterDialog = false
                    }
                ) {
                    Text("Aplicar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        tempStatusFilters = emptySet()
                        tempPriorityFilters = emptySet()
                        statusFilters = emptySet()
                        priorityFilters = emptySet()
                        showFilterDialog = false
                    }
                ) {
                    Text("Limpar Filtros")
                }
            }
        )
    }

    if (taskToDelete != null) {
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            containerColor = Color.White,
            tonalElevation = 0.dp,
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

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        taskViewModel.errorEvents.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(userId) {
        taskViewModel.loadTasks(userId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "Meus Afazeres",
                    style = MaterialTheme.typography.headlineLarge,
                    fontSize = 36.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            actions = {
                IconButton(
                    onClick = onToggleHeader
                ) {
                    Icon(
                        imageVector = if (isHeaderExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isHeaderExpanded) "Recolher busca e saudação" else "Expandir busca e saudação",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )
        val userData by authViewModel.currentUserData.collectAsState()
        val currentUser = (authState as? AuthUIState.Success)?.user
        
        if (isHeaderExpanded) {
            if (currentUser != null) {
                Text(
                    text = "Olá, ${userData?.nome ?: currentUser.email ?: "Usuário"}!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Custom Search Bar Row Layout
        var isSearchFocused by remember { mutableStateOf(false) }
        if (isHeaderExpanded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Less rounded Filter Button (Outlined, colors inverted: background transparent, icon/border primary)
                OutlinedIconButton(
                    onClick = {
                        tempStatusFilters = statusFilters
                        tempPriorityFilters = priorityFilters
                        showFilterDialog = true
                    },
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    colors = IconButtonDefaults.outlinedIconButtonColors(
                        containerColor = Color.White,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Filtro"
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Less rounded Search Field (RoundedCornerShape 8.dp) with purple placeholder
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { 
                        searchText = it
                        taskViewModel.loadTasks(userId, searchText)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .onFocusChanged { isSearchFocused = it.isFocused },
                    placeholder = { 
                        Text(
                            text = "Buscar afazeres...", 
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        ) 
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.primary,
                        unfocusedTextColor = MaterialTheme.colorScheme.primary,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Circular Search/Clear Button on the right (keeps circle shape, primary background, white icon)
                IconButton(
                    onClick = {
                        if (searchText.isNotEmpty()) {
                            searchText = ""
                            taskViewModel.loadTasks(userId, null)
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                ) {
                    Icon(
                        imageVector = if (searchText.isNotEmpty()) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = Color.White
                    )
                }
            }
        }



        when (taskListState) {
            is TaskUIState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is TaskUIState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = (taskListState as TaskUIState.Error).message, color = Color.Red)
                }
            }
            is TaskUIState.Success -> {
                val allTasks = (taskListState as TaskUIState.Success).tasks
                val tasks = remember(allTasks, statusFilters, priorityFilters) {
                    allTasks.filter { task ->
                        val matchesStatus = statusFilters.isEmpty() || statusFilters.contains(task.status)
                        val matchesPriority = priorityFilters.isEmpty() || priorityFilters.contains(task.prioridade)
                        matchesStatus && matchesPriority
                    }
                }
                val isLoadingMore by taskViewModel.isLoadingMore.collectAsState()
                
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
                                },
                                onEdit = {
                                    navController.navigate("cadastro_task?taskId=${task.id}")
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

@Composable
fun FilterChipCustom(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (selected) MaterialTheme.colorScheme.primary else Color.White,
        contentColor = if (selected) Color.White else MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        modifier = Modifier.padding(2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}