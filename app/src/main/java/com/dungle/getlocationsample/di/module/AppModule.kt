package com.dungle.getlocationsample.di.module

import android.content.Context
import com.dungle.getlocationsample.data.local.AppDatabase
import com.dungle.getlocationsample.data.local.LocalSessionDataSource
import com.dungle.getlocationsample.data.repo.SessionRepository
import com.dungle.getlocationsample.data.repo.SessionRepositoryImpl
import com.dungle.getlocationsample.data.source.SessionDataSource
import com.dungle.getlocationsample.ui.viewmodel.SessionViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        AppDatabase.getDatabase(get())
    }

    single { AppDatabase.getDatabase(get()).sessionDao() }

    single<SessionDataSource> { LocalSessionDataSource(get()) }

    single<SessionRepository> {
        SessionRepositoryImpl(get())
    }

    viewModel { SessionViewModel(get()) }
}
