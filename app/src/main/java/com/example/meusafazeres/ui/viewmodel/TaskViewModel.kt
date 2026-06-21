package com.example.meusafazeres.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meusafazeres.model.Task
import com.example.meusafazeres.model.TaskStatus
import com.example.meusafazeres.repository.TaskRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class TaskUIState {
    object Idle : TaskUIState()
    object Loading : TaskUIState()
    data class Success(val tasks: List<Task>) : TaskUIState()
    data class Error(val message: String) : TaskUIState()
}

class TaskViewModel(
    private val repository: TaskRepository
) : ViewModel() {
    
    private val _errorEvents = MutableSharedFlow<String>()
    val errorEvents: SharedFlow<String> = _errorEvents.asSharedFlow()

    private val _uiState = MutableStateFlow<TaskUIState>(TaskUIState.Idle)
    val uiState: StateFlow<TaskUIState> = _uiState

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore

    private val _allLoadedTasks = mutableListOf<Task>()
    private var currentPage = 1
    private var currentSearch: String? = null
    private var lastUserId: String? = null
    private var isEndReached = false

    fun loadTasks(userId: String, search: String? = null, isRefresh: Boolean = false) {
        val isNewSearch = search != currentSearch
        val isNewUser = userId != lastUserId

        if (isRefresh || isNewSearch || isNewUser) {
            currentPage = 1
            _allLoadedTasks.clear()
            isEndReached = false
            _uiState.value = TaskUIState.Loading
        }

        currentSearch = search
        lastUserId = userId

        if (isEndReached && !isRefresh && !isNewSearch && !isNewUser) return
        if (currentPage > 1) _isLoadingMore.value = true

        viewModelScope.launch {
            try {
                val newTasks = repository.getTasks(userId, search, currentPage)
                
                // Filter out any duplicates that might already be in _allLoadedTasks
                val nonDuplicateNewTasks = newTasks.filter { newTask ->
                    _allLoadedTasks.none { it.id == newTask.id }
                }

                if (newTasks.isEmpty() || userId.isBlank()) {
                    isEndReached = true
                    if (newTasks.isEmpty()) {
                        if (currentPage == 1) _uiState.value = TaskUIState.Success(emptyList())
                    } else {
                        _allLoadedTasks.addAll(nonDuplicateNewTasks)
                        _uiState.value = TaskUIState.Success(_allLoadedTasks.toList())
                    }
                } else {
                    _allLoadedTasks.addAll(nonDuplicateNewTasks)
                    _uiState.value = TaskUIState.Success(_allLoadedTasks.toList())
                    currentPage++
                }
            } catch (e: Exception) {
                if (currentPage == 1) {
                    _uiState.value = TaskUIState.Error(e.message ?: "Erro ao carregar tarefas")
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
                    _uiState.value = TaskUIState.Success(_allLoadedTasks.toList())
                }
            } catch (e: Exception) {
                _errorEvents.emit("Erro ao atualizar status do afazer")
            }
        }
    }

    fun deleteTask(taskId: String, userId: String) {
        viewModelScope.launch {
            try {
                repository.deleteTask(taskId, userId)
                _allLoadedTasks.removeAll { it.id == taskId }
                _uiState.value = TaskUIState.Success(_allLoadedTasks.toList())
            } catch (e: Exception) {
                _errorEvents.emit("Erro ao excluir afazer")
            }
        }
    }

    fun addTask(task: Task, userId: String) {
        viewModelScope.launch {
            try {
                val createdTask = repository.createTask(task.copy(donoId = userId))
                _allLoadedTasks.add(0, createdTask)
                _uiState.value = TaskUIState.Success(_allLoadedTasks.toList())
            } catch (e: Exception) {
                _errorEvents.emit("Erro ao adicionar afazer")
            }
        }
    }

    fun getTaskById(taskId: String): Task? {
        return _allLoadedTasks.find { it.id == taskId }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            try {
                val updatedTask = repository.updateTask(task)
                val index = _allLoadedTasks.indexOfFirst { it.id == task.id }
                if (index != -1) {
                    _allLoadedTasks[index] = updatedTask
                    _uiState.value = TaskUIState.Success(_allLoadedTasks.toList())
                }
            } catch (e: Exception) {
                _errorEvents.emit("Erro ao salvar alterações do afazer")
            }
        }
    }
}
