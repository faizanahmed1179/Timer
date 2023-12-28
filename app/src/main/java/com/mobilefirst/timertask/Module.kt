package com.mobilefirst.timertask

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { NotificationHelper(androidContext()) }
    viewModel { TimerViewModel() }
}