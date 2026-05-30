package com.example.meusafazeres.model

import java.util.Date

data class User(
    val id: String,
    val nome: String,
    val email: String,
    val dataCadastro: Date = Date()
)
