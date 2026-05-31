package com.example.meusafazeres.network

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://firestore.googleapis.com/v1/projects/todo-app-pdm/databases/(default)/documents/"

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            return@Interceptor chain.proceed(originalRequest)
        }

        // We use runBlocking here because we need the token before proceeding with the network call.
        // Get the ID Token from Firebase
        val token = runBlocking {
            try {
                user.getIdToken(false).await().token
            } catch (e: Exception) {
                null
            }
        }

        if (token != null) {
            val authenticatedRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            chain.proceed(authenticatedRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()

    val instance: TaskApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(TaskApi::class.java)
    }

    val userInstance: UserApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(UserApi::class.java)
    }
}
