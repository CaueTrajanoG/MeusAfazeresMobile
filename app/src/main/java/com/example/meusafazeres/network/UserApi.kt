package com.example.meusafazeres.network

import com.example.meusafazeres.network.firestore.FirestoreDocument
import com.example.meusafazeres.network.firestore.UserFields
import retrofit2.http.*

interface UserApi {
    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: String): FirestoreDocument<UserFields>

    @POST("users")
    suspend fun createUser(
        @Query("documentId") id: String,
        @Body document: FirestoreDocument<UserFields>
    ): FirestoreDocument<UserFields>

    @PATCH("users/{id}")
    suspend fun updateUser(
        @Path("id") id: String,
        @Query("updateMask.fieldPaths") updateMask: List<String>,
        @Body document: FirestoreDocument<UserFields>
    ): FirestoreDocument<UserFields>
}
