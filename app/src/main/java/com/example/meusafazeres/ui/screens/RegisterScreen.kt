package com.example.meusafazeres.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.meusafazeres.model.Task
import com.example.meusafazeres.ui.components.AppLogo
import com.example.meusafazeres.ui.viewmodel.AuthState
import com.example.meusafazeres.ui.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(navController: NavController, viewModel: AuthViewModel) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    
    val authState by viewModel.authState

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            navController.navigate("home_structure") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

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
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirmar Senha") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        
        val displayError = if (errorMessage.isNotEmpty()) errorMessage 
                          else if (authState is AuthState.Error) (authState as AuthState.Error).message 
                          else ""

        if (displayError.isNotEmpty()) {
            Text(
                text = displayError,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (authState is AuthState.Loading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { 
                    if (nome.isEmpty()) {
                        errorMessage = "Digite seu nome"
                    } else if (password == confirmPassword) {
                        errorMessage = ""
                        viewModel.register(nome, email, password) 
                    } else {
                        errorMessage = "Senhas não conferem"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cadastrar")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            TextButton(onClick = { navController.navigateUp() }) {
                Text("Já tem uma conta? Faça login")
            }
        }
    }
}
