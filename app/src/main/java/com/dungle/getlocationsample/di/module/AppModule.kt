package com.dungle.getlocationsample.di.module

import com.dungle.getlocationsample.data.session.local.AppDatabase
import com.dungle.getlocationsample.data.session.local.LocalSessionDataSource
import com.dungle.getlocationsample.data.session.repo.SessionRepository
import com.dungle.getlocationsample.data.session.repo.SessionRepositoryImpl
import com.dungle.getlocationsample.data.session.source.SessionDataSource
import com.dungle.getlocationsample.ui.viewmodel.SessionViewModel
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

    single { SessionViewModel(get()) }
}
