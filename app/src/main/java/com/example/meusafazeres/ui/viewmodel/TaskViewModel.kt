package com.example.meusafazeres.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meusafazeres.model.Task
import com.example.meusafazeres.model.TaskStatus
import com.example.meusafazeres.repository.TaskRepository
import kotlinx.coroutines.launch

sealed class TaskListState {
    object Idle : TaskListState()
    object Loading : TaskListState()
    data class Success(val tasks: List<Task>) : TaskListState()
    data class Error(val message: String) : TaskListState()
}

class TaskViewModel : ViewModel() {
    private val repository = TaskRepository()
    
    private val _taskListState = mutableStateOf<TaskListState>(TaskListState.Idle)
    val taskListState: State<TaskListState> = _taskListState

    private val _isLoadingMore = mutableStateOf(false)
    val isLoadingMore: State<Boolean> = _isLoadingMore

    private val _allLoadedTasks = mutableStateListOf<Task>()
    private var currentPage = 1
    private var currentSearch: String? = null
    private var isEndReached = false

    fun loadTasks(userId: String, search: String? = null, isRefresh: Boolean = false) {
        val isNewSearch = search != currentSearch
        
        if (isRefresh || isNewSearch) {
            currentPage = 1
            _allLoadedTasks.clear()
            isEndReached = false
            _taskListState.value = TaskListState.Loading
        }
        
        if (isEndReached && !isRefresh && !isNewSearch) return
        if (currentPage > 1) _isLoadingMore.value = true
        
        currentSearch = search

        viewModelScope.launch {
            try {
                val newTasks = repository.getTasks(userId, search, currentPage)
                
                // Filter out any duplicates that might already be in _allLoadedTasks
                val nonDuplicateNewTasks = newTasks.filter { newTask ->
                    _allLoadedTasks.none { it.id == newTask.id }
                }

                if (newTasks.isEmpty()) {
                    isEndReached = true
                    if (currentPage == 1) _taskListState.value = TaskListState.Success(emptyList())
                } else {
                    _allLoadedTasks.addAll(nonDuplicateNewTasks)
                    _taskListState.value = TaskListState.Success(_allLoadedTasks.toList())
                    currentPage++
                }
            } catch (e: Exception) {
                if (currentPage == 1) {
                    _taskListState.value = TaskListState.Error(e.message ?: "Erro ao carregar tarefas")
                }
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    fun loadNextPage(userId: String) {
        loadTasks(userId, currentSearch)
    }

    fun toggleTaskStatus(task: Task) {
        val newStatus = if (task.status == TaskStatus.FEITO) TaskStatus.PENDENTE else TaskStatus.FEITO
        val updatedTask = task.copy(status = newStatus)
        
        viewModelScope.launch {
            try {
                repository.updateTask(updatedTask)
                // Update local list
                val index = _allLoadedTasks.indexOfFirst { it.id == task.id }
                if (index != -1) {
                    _allLoadedTasks[index] = updatedTask
                    _taskListState.value = TaskListState.Success(_allLoadedTasks.toList())
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteTask(taskId: String, userId: String) {
        viewModelScope.launch {
            try {
                repository.deleteTask(taskId)
                _allLoadedTasks.removeAll { it.id == taskId }
                _taskListState.value = TaskListState.Success(_allLoadedTasks.toList())
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun addTask(task: Task, userId: String) {
        viewModelScope.launch {
            try {
                val createdTask = repository.createTask(task.copy(donoId = userId))
                _allLoadedTasks.add(0, createdTask)
                _taskListState.value = TaskListState.Success(_allLoadedTasks.toList())
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
