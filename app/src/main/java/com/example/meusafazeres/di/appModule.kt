package com.example.meusafazeres.di

import com.example.meusafazeres.repository.AuthRepository
import com.example.meusafazeres.repository.TaskRepository
import com.example.meusafazeres.repository.UserRepository
import com.example.meusafazeres.ui.viewmodel.AuthViewModel
import com.example.meusafazeres.ui.viewmodel.TaskViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    singleOf(::AuthRepository)
    singleOf(::UserRepository)
    singleOf(::TaskRepository)
    viewModelOf(::AuthViewModel)
    viewModelOf(::TaskViewModel)
}
