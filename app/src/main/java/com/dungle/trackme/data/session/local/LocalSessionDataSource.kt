package com.dungle.trackme.data.session.local

import com.dungle.trackme.data.session.local.model.LocalSession
import com.dungle.trackme.data.session.source.SessionDataSource
import com.dungle.trackme.model.Session

class LocalSessionDataSource(private val sessionDao: SessionDao) : SessionDataSource {
    override suspend fun getAllSession(): List<Session>{
        return convertToSessions(sessionDao.getAllSession())
    }

    private fun convertToSessions(sessions: List<LocalSession>): MutableList<Session> {
        val sessionList : MutableList<Session> = ArrayList()
        sessions.forEach{
            sessionList.add(it.toSession())
        }
        return sessionList
    }

    override suspend fun getSessionCount(): Int {
        return sessionDao.getSessionCount()
    }

    override suspend fun getSessionById(id: Int): Session {
        return sessionDao.getSessionById(id).toSession()
    }

    override suspend fun saveSession(session: Session): Long {
        return sessionDao.insertSession(session.toLocalSession())
    }
}