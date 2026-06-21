package com.example.meusafazeres.ui.screens

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.meusafazeres.model.Priority
import com.example.meusafazeres.model.Task
import com.example.meusafazeres.ui.viewmodel.AuthViewModel
import com.example.meusafazeres.ui.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CadastroTaskScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    taskViewModel: TaskViewModel,
    taskId: String? = null
) {
    var titulo by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var prioridade by remember { mutableStateOf(Priority.NORMAL) }
    var dueDate by remember { mutableStateOf<Date?>(null) }

    LaunchedEffect(taskId) {
        if (taskId != null) {
            val task = taskViewModel.getTaskById(taskId)
            if (task != null) {
                titulo = task.titulo
                descricao = task.descricao
                prioridade = task.prioridade
                dueDate = task.dueDate
            }
        }
    }
    
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)
            dueDate = selectedCalendar.time
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.Start, // Mudar de Center para Start
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = if (taskId != null) "Editar Afazer" else "Novo Afazer",
            style = MaterialTheme.typography.headlineLarge,
            fontSize = 48.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it },
            label = { Text("Título") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = descricao,
            onValueChange = { descricao = it },
            label = { Text("Descrição") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Priority Selection
        Text(text = "Prioridade", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Priority.values().forEach { p ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { prioridade = p }
                ) {
                    RadioButton(
                        selected = (prioridade == p),
                        onClick = { prioridade = p }
                    )
                    Text(text = p.name.lowercase().replaceFirstChar { it.uppercase() })
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Date Selection
        OutlinedTextField(
            value = dueDate?.let { dateFormat.format(it) } ?: "",
            onValueChange = {},
            label = { Text("Data do Afazer") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { datePickerDialog.show() },
            enabled = false,
            readOnly = true,
            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = {
                if (titulo.isNotEmpty()) {
                    if (taskId != null) {
                        val existingTask = taskViewModel.getTaskById(taskId)
                        if (existingTask != null) {
                            val updatedTask = existingTask.copy(
                                titulo = titulo,
                                descricao = descricao,
                                prioridade = prioridade,
                                dueDate = dueDate
                            )
                            taskViewModel.updateTask(updatedTask)
                        }
                    } else {
                        val userId = authViewModel.currentUser?.uid ?: ""
                        val newTask = Task(
                            titulo = titulo,
                            descricao = descricao,
                            prioridade = prioridade,
                            dueDate = dueDate
                        )
                        taskViewModel.addTask(newTask, userId)
                    }
                    // Navigate back to Afazeres tab
                    navController.navigate("main") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Salvar")
        }
    }
}
