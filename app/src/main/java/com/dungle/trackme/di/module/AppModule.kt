package com.dungle.trackme.di.module

import com.dungle.trackme.data.session.local.AppDatabase
import com.dungle.trackme.data.session.local.LocalSessionDataSource
import com.dungle.trackme.data.session.repo.SessionRepository
import com.dungle.trackme.data.session.repo.SessionRepositoryImpl
import com.dungle.trackme.data.session.source.SessionDataSource
import com.dungle.trackme.ui.viewmodel.SessionViewModel
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
