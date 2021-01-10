package com.dungle.getlocationsample

import android.app.Application
import com.dungle.getlocationsample.di.module.appModule
import com.dungle.getlocationsample.di.module.repositoryModule
import com.dungle.getlocationsample.di.module.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(listOf(appModule, viewModelModule, repositoryModule))
        }
    }
}