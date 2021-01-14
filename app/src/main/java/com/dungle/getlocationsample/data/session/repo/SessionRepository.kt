package com.dungle.getlocationsample.data.session.repo

import com.dungle.getlocationsample.model.Session

interface SessionRepository {
    suspend fun getSessionCount() : Int
    suspend fun getAllSession() : List<Session>
    suspend fun getSessionById(id : Int) : Session
    suspend fun saveSession(session: Session) : Long
}