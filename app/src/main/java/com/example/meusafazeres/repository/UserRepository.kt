package com.example.meusafazeres.repository

import com.example.meusafazeres.model.User
import com.example.meusafazeres.network.RetrofitClient
import com.example.meusafazeres.network.firestore.FirestoreDocument
import com.example.meusafazeres.network.firestore.StringField
import com.example.meusafazeres.network.firestore.TimestampField
import com.example.meusafazeres.network.firestore.UserFields
import java.text.SimpleDateFormat
import java.util.*

class UserRepository {
    private val api = RetrofitClient.userInstance
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    suspend fun getUser(id: String): User {
        val response = api.getUser(id)
        return mapFirestoreToUser(response)
    }

    suspend fun createUser(user: User): User {
        val doc = FirestoreDocument(fields = mapUserToFirestore(user))
        val response = api.createUser(user.id, doc)
        return mapFirestoreToUser(response)
    }

    suspend fun updateUser(user: User): User {
        val doc = FirestoreDocument(fields = mapUserToFirestore(user))
        val updateMask = listOf("nome", "email")
        val response = api.updateUser(user.id, updateMask, doc)
        return mapFirestoreToUser(response)
    }

    private fun mapUserToFirestore(user: User): UserFields {
        return UserFields(
            nome = StringField(user.nome),
            email = StringField(user.email),
            dataCadastro = TimestampField(isoFormat.format(user.dataCadastro))
        )
    }

    private fun mapFirestoreToUser(doc: FirestoreDocument<UserFields>): User {
        val id = doc.name?.split("/")?.last() ?: ""
        val f = doc.fields
        return User(
            id = id,
            nome = f.nome.stringValue,
            email = f.email.stringValue,
            dataCadastro = try { isoFormat.parse(f.dataCadastro.timestampValue) ?: Date() } catch(e: Exception) { Date() }
        )
    }
}
