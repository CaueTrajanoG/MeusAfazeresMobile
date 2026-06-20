package com.example.meusafazeres.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.meusafazeres.model.Task
import com.example.meusafazeres.ui.components.AppLogo
import com.example.meusafazeres.ui.viewmodel.AuthUIState
import com.example.meusafazeres.ui.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(navController: NavController, viewModel: AuthViewModel) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var localErrorMessage by remember { mutableStateOf("") }
    
    val authState by viewModel.uiState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthUIState.Success) {
            navController.navigate("home_structure") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    val displayError = if (localErrorMessage.isNotEmpty()) localErrorMessage 
                      else if (authState is AuthUIState.Error) (authState as AuthUIState.Error).message 
                      else ""
    
    val isError = displayError.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AppLogo()

        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome") },
            modifier = Modifier.fillMaxWidth(),
            isError = isError && (nome.isEmpty() || displayError == "Digite seu nome"),
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            isError = isError && (email.isEmpty() || displayError.contains("E-mail")),
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = isError && (password.isEmpty() || displayError.contains("Senha")),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) }
        )

        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirmar Senha") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = isError && (confirmPassword.isEmpty() || displayError == "Senhas não conferem"),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            supportingText = {
                if (isError) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = displayError,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (authState is AuthUIState.Loading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { 
                    if (nome.trim().isEmpty()) {
                        localErrorMessage = "Digite seu nome"
                    } else if (email.trim().isEmpty()) {
                        localErrorMessage = "Digite seu e-mail"
                    } else if (password.isEmpty()) {
                        localErrorMessage = "Digite uma senha"
                    } else if (password == confirmPassword) {
                        localErrorMessage = ""
                        viewModel.register(nome, email, password) 
                    } else {
                        localErrorMessage = "Senhas não conferem"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cadastrar")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            TextButton(onClick = { 
                viewModel.resetAuthState()
                navController.navigateUp() 
            }) {
                Text("Já tem uma conta? Faça login")
            }
        }
    }
}
