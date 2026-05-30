package com.example.meusafazeres.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    suspend fun login(email: String, pass: String): FirebaseUser? {
        return firebaseAuth.signInWithEmailAndPassword(email, pass).await().user
    }

    suspend fun register(email: String, pass: String): FirebaseUser? {
        return firebaseAuth.createUserWithEmailAndPassword(email, pass).await().user
    }

    fun logout() {
        firebaseAuth.signOut()
    }
}
