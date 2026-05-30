package com.example.meusafazeres.model

import com.google.gson.annotations.SerializedName
import java.util.Date

enum class Priority {
    @SerializedName("normal") NORMAL,
    @SerializedName("leve") LEVE,
    @SerializedName("media") MEDIA,
    @SerializedName("alta") ALTA
}

enum class TaskStatus {
    @SerializedName("pendente") PENDENTE,
    @SerializedName("feito") FEITO
}

data class Task(
    val id: String? = null,
    val titulo: String,
    val descricao: String,
    val prioridade: Priority,
    val status: TaskStatus = TaskStatus.PENDENTE,
    val donoId: String? = null,
    val dataCriacao: Date = Date(),
    val dueDate: Date? = null,
    val dataAlteracao: Date? = null,
    val removido: Boolean = false
)
