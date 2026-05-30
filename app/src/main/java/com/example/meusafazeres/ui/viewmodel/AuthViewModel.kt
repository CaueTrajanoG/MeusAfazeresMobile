package com.example.meusafazeres.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meusafazeres.model.User
import com.example.meusafazeres.repository.AuthRepository
import com.example.meusafazeres.repository.UserRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()
    private val userRepository = UserRepository()
    
    private val _authState = mutableStateOf<AuthState>(AuthState.Idle)
    val authState: State<AuthState> = _authState

    private val _currentUserData = mutableStateOf<User?>(null)
    val currentUserData: State<User?> = _currentUserData

    val currentUser: FirebaseUser? get() = repository.getCurrentUser()

    init {
        currentUser?.let { fbUser ->
            viewModelScope.launch {
                try {
                    val userData = userRepository.getUser(fbUser.uid)
                    _currentUserData.value = userData
                } catch (e: Exception) {
                    // User might not exist in json-server yet if only in Firebase
                }
            }
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val fbUser = repository.login(email, pass)
                if (fbUser != null) {
                    try {
                        val userData = userRepository.getUser(fbUser.uid)
                        _currentUserData.value = userData
                    } catch (e: Exception) {
                        // If user doesn't exist in Firestore (404), we still allow login
                        // but _currentUserData remains null. We can handle this in UI.
                        _currentUserData.value = null
                    }
                    _authState.value = AuthState.Success(fbUser)
                } else {
                    _authState.value = AuthState.Error("Falha ao realizar login")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }

    fun register(nome: String, email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val fbUser = repository.register(email, pass)
                if (fbUser != null) {
                    val newUser = User(id = fbUser.uid, nome = nome, email = email)
                    userRepository.createUser(newUser)
                    _currentUserData.value = newUser
                    _authState.value = AuthState.Success(fbUser)
                } else {
                    _authState.value = AuthState.Error("Falha ao realizar cadastro")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }

    fun logout() {
        repository.logout()
        _authState.value = AuthState.Idle
    }
}
