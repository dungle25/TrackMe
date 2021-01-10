package com.dungle.getlocationsample.data.repo

import com.dungle.getlocationsample.data.local.LocalSessionDataSource
import com.dungle.getlocationsample.model.Session

class SessionRepositoryImpl(
    private val localSessionDataSource: LocalSessionDataSource
) : SessionRepository {
    override suspend fun getSessionCount(): Int {
        return localSessionDataSource.getSessionCount()
    }

    override suspend fun getAllSession(): List<Session> {
        return localSessionDataSource.getAllSession()
    }
}