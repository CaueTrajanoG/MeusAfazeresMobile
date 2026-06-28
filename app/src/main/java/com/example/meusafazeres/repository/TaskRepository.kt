package com.example.meusafazeres.repository

import android.util.Log
import com.example.meusafazeres.data.local.TaskDao
import com.example.meusafazeres.data.local.toDomain
import com.example.meusafazeres.data.local.toEntity
import com.example.meusafazeres.model.Priority
import com.example.meusafazeres.model.Task
import com.example.meusafazeres.model.TaskStatus
import com.example.meusafazeres.network.RetrofitClient
import com.example.meusafazeres.network.firestore.*
import java.text.SimpleDateFormat
import java.util.*

class TaskRepository(private val taskDao: TaskDao) {
    private val api = RetrofitClient.instance
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    suspend fun getTasks(userId: String, search: String? = null, page: Int = 1): List<Task> {
        if (userId.isBlank()) {
            val localEntities = taskDao.getAllLocalTasks()
            val domainTasks = localEntities.map { it.toDomain() }
            return domainTasks.filter {
                search == null || it.titulo.contains(search, ignoreCase = true) || it.descricao.contains(search, ignoreCase = true)
            }
        }

        val pageSize = 10
        val offset = (page - 1) * pageSize

        val query = FirestoreQueryRequest(
            structuredQuery = StructuredQuery(
                from = listOf(CollectionSelector(collectionId = "tasks")),
                where = Filter(
                    compositeFilter = CompositeFilter(
                        op = "AND",
                        filters = listOf(
                            Filter(fieldFilter = FieldFilter(
                                field = FieldReference("donoId"),
                                op = "EQUAL",
                                value = FirestoreValue(stringValue = userId)
                            )),
                            Filter(fieldFilter = FieldFilter(
                                field = FieldReference("removido"),
                                op = "EQUAL",
                                value = FirestoreValue(booleanValue = false)
                            ))
                        )
                    )
                ),
                orderBy = listOf(Order(
                    field = FieldReference("dataCriacao"),
                    direction = "DESCENDING"
                )),
                limit = pageSize,
                offset = offset
            )
        )

        val response = api.queryTasks(query)
        val tasks = response.mapNotNull { it.document }.map { doc ->
            mapFirestoreToTask(doc)
        }

        // Local filtering as safety and search
        return tasks.filter { 
            it.donoId == userId && !it.removido && 
            (search == null || it.titulo.contains(search, ignoreCase = true) || it.descricao.contains(search, ignoreCase = true))
        }
    }

    suspend fun createTask(task: Task): Task {
        if (task.donoId.isNullOrBlank()) {
            val localId = task.id ?: UUID.randomUUID().toString()
            val localTask = task.copy(id = localId)
            taskDao.insertLocalTask(localTask.toEntity(localId))
            return localTask
        }

        val id = UUID.randomUUID().toString()
        val doc = FirestoreDocument(fields = mapTaskToFirestore(task))
        val response = api.createTask(id, doc)
        return mapFirestoreToTask(response)
    }

    suspend fun updateTask(task: Task): Task {
        if (task.donoId.isNullOrBlank()) {
            taskDao.insertLocalTask(task.toEntity())
            return task
        }

        val doc = FirestoreDocument(fields = mapTaskToFirestore(task))
        // Identify which fields to update
        val updateMask = listOf("titulo", "descricao", "prioridade", "status", "dueDate", "removido")
        val response = api.updateTask(task.id!!, updateMask, doc)
        return mapFirestoreToTask(response)
    }

    suspend fun deleteTask(id: String, userId: String) {
        if (userId.isBlank()) {
            taskDao.logicalDeleteLocalTask(id)
            return
        }

        // Logical deletion: Fetch the task (or just create a stub with the flag)
        // Since we are using REST PATCH, we can just send the field we want to change.
        val doc = FirestoreDocument(fields = TaskFields(
            titulo = StringField(""), // Placeholder, won't be updated if not in mask
            descricao = StringField(""),
            prioridade = StringField(""),
            status = StringField(""),
            donoId = StringField(""),
            dataCriacao = TimestampField(isoFormat.format(Date())),
            removido = BooleanField(true)
        ))
        val updateMask = listOf("removido")
        api.updateTask(id, updateMask, doc)
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
