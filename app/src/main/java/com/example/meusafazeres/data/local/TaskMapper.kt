package com.example.meusafazeres.data.local

import com.example.meusafazeres.model.Priority
import com.example.meusafazeres.model.Task
import com.example.meusafazeres.model.TaskStatus
import java.util.Date

fun Task.toEntity(generatedId: String = java.util.UUID.randomUUID().toString()): TaskEntity {
    return TaskEntity(
        id = this.id ?: generatedId,
        titulo = this.titulo,
        descricao = this.descricao,
        prioridade = this.prioridade.name.lowercase(),
        status = this.status.name.lowercase(),
        dataCriacao = this.dataCriacao.time,
        dueDate = this.dueDate?.time,
        removido = this.removido
    )
}

fun TaskEntity.toDomain(): Task {
    return Task(
        id = this.id,
        titulo = this.titulo,
        descricao = this.descricao,
        prioridade = try { Priority.valueOf(this.prioridade.uppercase()) } catch (e: Exception) { Priority.NORMAL },
        status = try { TaskStatus.valueOf(this.status.uppercase()) } catch (e: Exception) { TaskStatus.PENDENTE },
        dataCriacao = Date(this.dataCriacao),
        dueDate = this.dueDate?.let { Date(it) },
        removido = this.removido
    )
}
