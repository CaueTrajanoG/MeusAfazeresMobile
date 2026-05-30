package com.example.meusafazeres.repository

import com.example.meusafazeres.model.Priority
import com.example.meusafazeres.model.Task
import com.example.meusafazeres.model.TaskStatus
import com.example.meusafazeres.network.RetrofitClient
import com.example.meusafazeres.network.firestore.*
import java.text.SimpleDateFormat
import java.util.*

class TaskRepository {
    private val api = RetrofitClient.instance
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    suspend fun getTasks(userId: String, search: String? = null, page: Int = 1): List<Task> {
        val response = api.getTasks()
        val allTasks = response.documents?.map { doc ->
            mapFirestoreToTask(doc)
        } ?: emptyList()

        // Filter by userId (donoId) and search text locally since Firestore REST listing is limited
        return allTasks.filter { 
            it.donoId == userId && !it.removido && 
            (search == null || it.titulo.contains(search, ignoreCase = true) || it.descricao.contains(search, ignoreCase = true))
        }
    }

    suspend fun createTask(task: Task): Task {
        val id = UUID.randomUUID().toString()
        val doc = FirestoreDocument(fields = mapTaskToFirestore(task))
        val response = api.createTask(id, doc)
        return mapFirestoreToTask(response)
    }

    suspend fun updateTask(task: Task): Task {
        val doc = FirestoreDocument(fields = mapTaskToFirestore(task))
        // Identify which fields to update
        val updateMask = listOf("titulo", "descricao", "prioridade", "status", "removido")
        val response = api.updateTask(task.id!!, updateMask, doc)
        return mapFirestoreToTask(response)
    }

    suspend fun deleteTask(id: String) {
        api.deleteTask(id)
    }

    private fun mapTaskToFirestore(task: Task): TaskFields {
        return TaskFields(
            titulo = StringField(task.titulo),
            descricao = StringField(task.descricao),
            prioridade = StringField(task.prioridade.name.lowercase()),
            status = StringField(task.status.name.lowercase()),
            donoId = StringField(task.donoId ?: ""),
            dataCriacao = TimestampField(isoFormat.format(task.dataCriacao)),
            dueDate = task.dueDate?.let { TimestampField(isoFormat.format(it)) },
            removido = BooleanField(task.removido)
        )
    }

    private fun mapFirestoreToTask(doc: FirestoreDocument<TaskFields>): Task {
        val id = doc.name?.split("/")?.last() ?: ""
        val f = doc.fields
        return Task(
            id = id,
            titulo = f.titulo.stringValue,
            descricao = f.descricao.stringValue,
            prioridade = try { Priority.valueOf(f.prioridade.stringValue.uppercase()) } catch(e: Exception) { Priority.NORMAL },
            status = try { TaskStatus.valueOf(f.status.stringValue.uppercase()) } catch(e: Exception) { TaskStatus.PENDENTE },
            donoId = f.donoId.stringValue,
            dataCriacao = try { isoFormat.parse(f.dataCriacao.timestampValue) ?: Date() } catch(e: Exception) { Date() },
            dueDate = f.dueDate?.let { try { isoFormat.parse(it.timestampValue) } catch(e: Exception) { null } },
            removido = f.removido.booleanValue
        )
    }
}
