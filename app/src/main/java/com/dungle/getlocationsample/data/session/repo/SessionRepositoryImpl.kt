package com.dungle.getlocationsample.data.session.repo

import com.dungle.getlocationsample.data.session.source.SessionDataSource
import com.dungle.getlocationsample.model.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SessionRepositoryImpl(
    private val localSessionDataSource: SessionDataSource
) : SessionRepository {
    override suspend fun getSessionCount(): Int {
        return localSessionDataSource.getSessionCount()
    }

    override suspend fun getAllSession(): List<Session> {
        return localSessionDataSource.getAllSession()
    }

    override suspend fun getSessionById(id: Int): Session {
        return localSessionDataSource.getSessionById(id)
    }

    override suspend fun saveSession(session: Session): Long {
        return withContext(Dispatchers.IO) {
            localSessionDataSource.saveSession(session)
        }
    }
}