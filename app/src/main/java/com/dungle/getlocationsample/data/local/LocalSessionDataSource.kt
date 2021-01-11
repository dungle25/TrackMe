package com.dungle.getlocationsample.data.local

import com.dungle.getlocationsample.data.local.model.LocalSession
import com.dungle.getlocationsample.data.source.SessionDataSource
import com.dungle.getlocationsample.model.Session

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
}