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

    private var currentPage = 1
    private var currentSearch: String? = null

    fun loadTasks(userId: String, search: String? = null, isRefresh: Boolean = false) {
        if (isRefresh) {
            currentPage = 1
        }
        currentSearch = search
        
        viewModelScope.launch {
            if (currentPage == 1) _taskListState.value = TaskListState.Loading
            try {
                val tasks = repository.getTasks(userId, search, currentPage)
                _taskListState.value = TaskListState.Success(tasks)
            } catch (e: Exception) {
                _taskListState.value = TaskListState.Error(e.message ?: "Erro ao carregar tarefas")
            }
        }
    }

    fun toggleTaskStatus(task: Task) {
        val newStatus = if (task.status == TaskStatus.FEITO) TaskStatus.PENDENTE else TaskStatus.FEITO
        val updatedTask = task.copy(status = newStatus)
        
        viewModelScope.launch {
            try {
                repository.updateTask(updatedTask)
                // Refresh list or update local state
                if (taskListState.value is TaskListState.Success) {
                    val currentTasks = (taskListState.value as TaskListState.Success).tasks.map {
                        if (it.id == task.id) updatedTask else it
                    }
                    _taskListState.value = TaskListState.Success(currentTasks)
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
                loadTasks(userId, currentSearch, isRefresh = true)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun addTask(task: Task, userId: String) {
        viewModelScope.launch {
            try {
                repository.createTask(task.copy(donoId = userId))
                loadTasks(userId, currentSearch, isRefresh = true)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
