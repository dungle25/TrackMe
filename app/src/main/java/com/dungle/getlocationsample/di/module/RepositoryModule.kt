package com.dungle.getlocationsample.di.module

import com.dungle.getlocationsample.data.repo.SessionRepository
import com.dungle.getlocationsample.data.repo.SessionRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module {
    single<SessionRepository> {
        SessionRepositoryImpl(get())
    }
}