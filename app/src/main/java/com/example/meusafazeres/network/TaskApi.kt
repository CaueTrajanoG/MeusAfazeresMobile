package com.example.meusafazeres.network

import com.example.meusafazeres.network.firestore.FirestoreDocument
import com.example.meusafazeres.network.firestore.FirestoreListResponse
import com.example.meusafazeres.network.firestore.TaskFields
import retrofit2.http.*

interface TaskApi {
    @GET("documents/tasks")
    suspend fun getTasks(): FirestoreListResponse<TaskFields>

    @POST("documents/tasks")
    suspend fun createTask(
        @Query("documentId") id: String,
        @Body document: FirestoreDocument<TaskFields>
    ): FirestoreDocument<TaskFields>

    @PATCH("documents/tasks/{id}")
    suspend fun updateTask(
        @Path("id") id: String,
        @Query("updateMask.fieldPaths") updateMask: List<String>,
        @Body document: FirestoreDocument<TaskFields>
    ): FirestoreDocument<TaskFields>

    @DELETE("documents/tasks/{id}")
    suspend fun deleteTask(@Path("id") id: String)
}
