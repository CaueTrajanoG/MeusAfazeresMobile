package com.example.meusafazeres.di

import androidx.room.Room
import com.example.meusafazeres.data.local.AppDatabase
import com.example.meusafazeres.repository.AuthRepository
import com.example.meusafazeres.repository.TaskRepository
import com.example.meusafazeres.repository.UserRepository
import com.example.meusafazeres.ui.viewmodel.AuthViewModel
import com.example.meusafazeres.ui.viewmodel.TaskViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    // Room
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "meus_afazeres_local.db"
        ).build()
    }
    single { get<AppDatabase>().taskDao() }

    // Repositories & ViewModels
    singleOf(::AuthRepository)
    singleOf(::UserRepository)
    single { TaskRepository(get()) }
    viewModelOf(::AuthViewModel)
    viewModelOf(::TaskViewModel)
}
