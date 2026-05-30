package com.example.meusafazeres.network

import com.example.meusafazeres.network.firestore.FirestoreDocument
import com.example.meusafazeres.network.firestore.FirestoreListResponse
import com.example.meusafazeres.network.firestore.FirestoreQueryRequest
import com.example.meusafazeres.network.firestore.FirestoreQueryResponse
import com.example.meusafazeres.network.firestore.TaskFields
import retrofit2.http.*

interface TaskApi {
    @GET("tasks")
    suspend fun getTasks(): FirestoreListResponse<TaskFields>

    @POST(":runQuery")
    suspend fun queryTasks(@Body query: FirestoreQueryRequest): List<FirestoreQueryResponse<TaskFields>>

    @POST("tasks")
    suspend fun createTask(
        @Query("documentId") id: String,
        @Body document: FirestoreDocument<TaskFields>
    ): FirestoreDocument<TaskFields>

    @PATCH("tasks/{id}")
    suspend fun updateTask(
        @Path("id") id: String,
        @Query("updateMask.fieldPaths") updateMask: List<String>,
        @Body document: FirestoreDocument<TaskFields>
    ): FirestoreDocument<TaskFields>

    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") id: String)
}
