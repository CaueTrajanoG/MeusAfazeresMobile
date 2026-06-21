package com.example.meusafazeres.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val titulo: String,
    val descricao: String,
    val prioridade: String,
    val status: String,
    val dataCriacao: Long,
    val dueDate: Long?,
    val removido: Boolean = false
)
