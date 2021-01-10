package com.dungle.getlocationsample.di.module

import android.content.Context
import com.dungle.getlocationsample.data.local.AppDatabase
import org.koin.dsl.module

val appModule = module {
    single {
        getDatabase(get())
    }
}

fun getDatabase(context : Context) = AppDatabase.getDatabase(context)