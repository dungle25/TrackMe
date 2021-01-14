package com.dungle.trackme.data.session.repo

import com.dungle.trackme.data.session.source.SessionDataSource
import com.dungle.trackme.model.Session
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