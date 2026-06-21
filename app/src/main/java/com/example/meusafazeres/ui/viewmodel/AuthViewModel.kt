package com.example.meusafazeres.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meusafazeres.model.User
import com.example.meusafazeres.repository.AuthRepository
import com.example.meusafazeres.repository.UserRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuthActionCodeException
import com.google.firebase.auth.FirebaseAuthEmailException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.FirebaseAuthMultiFactorException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseAuthWebException
import com.google.firebase.FirebaseNetworkException

sealed class AuthUIState {
    object Idle : AuthUIState()
    object Loading : AuthUIState()
    data class Success(val user: FirebaseUser) : AuthUIState()
    data class Error(val message: String) : AuthUIState()
}

class AuthViewModel(
    private val repository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<AuthUIState>(AuthUIState.Idle)
    val uiState: StateFlow<AuthUIState> = _uiState

    private val _currentUserData = MutableStateFlow<User?>(null)
    val currentUserData: StateFlow<User?> = _currentUserData

    val currentUser: FirebaseUser? get() = repository.getCurrentUser()

    init {
        currentUser?.let { fbUser ->
            _uiState.value = AuthUIState.Success(fbUser)
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
            _uiState.value = AuthUIState.Loading
            try {
                val fbUser = repository.login(email.trim(), pass)
                if (fbUser != null) {
                    try {
                        val userData = userRepository.getUser(fbUser.uid)
                        _currentUserData.value = userData
                    } catch (e: Exception) {
                        _currentUserData.value = null
                    }
                    _uiState.value = AuthUIState.Success(fbUser)
                } else {
                    _uiState.value = AuthUIState.Error("Falha ao realizar login")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUIState.Error(mapError(e))
            }
        }
    }

    fun register(nome: String, email: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = AuthUIState.Loading
            try {
                val fbUser = repository.register(email.trim(), pass)
                if (fbUser != null) {
                    val newUser = User(id = fbUser.uid, nome = nome.trim(), email = email.trim())
                    userRepository.createUser(newUser)
                    _currentUserData.value = newUser
                    _uiState.value = AuthUIState.Success(fbUser)
                } else {
                    _uiState.value = AuthUIState.Error("Falha ao realizar cadastro")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUIState.Error(mapError(e))
            }
        }
    }

    fun logout() {
        repository.logout()
        _currentUserData.value = null
        _uiState.value = AuthUIState.Idle
    }

    fun resetAuthState() {
        _uiState.value = AuthUIState.Idle
    }

    private fun mapError(e: Exception): String {
        return when (e) {
            is FirebaseAuthWeakPasswordException -> "A senha é muito fraca. Use pelo menos 6 caracteres."
            is FirebaseAuthInvalidCredentialsException -> "E-mail e/ou senha inválidos."
            is FirebaseAuthInvalidUserException -> "Esta conta não foi encontrada ou está desativada."
            is FirebaseAuthUserCollisionException -> "Este e-mail já está cadastrado em outra conta."
            is FirebaseAuthActionCodeException -> "O código de confirmação é inválido ou expirou."
            is FirebaseAuthEmailException -> "Erro com o e-mail fornecido. Verifique o endereço."
            is FirebaseAuthRecentLoginRequiredException -> "Por segurança, faça login novamente para realizar esta ação."
            is FirebaseAuthMissingActivityForRecaptchaException -> "Erro de verificação de segurança (Recaptcha)."
            is FirebaseAuthMultiFactorException -> "Erro na autenticação de dois fatores."
            is FirebaseAuthWebException -> "Erro de operação via web. Tente novamente."
            is FirebaseNetworkException -> "Erro de conexão. Verifique sua internet."
            is FirebaseAuthException -> "Erro na autenticação. Tente novamente."
            else -> "Erro ao processar solicitação. Verifique seus dados."
        }
    }
}
