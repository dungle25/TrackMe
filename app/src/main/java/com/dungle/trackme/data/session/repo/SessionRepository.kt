package com.dungle.trackme.data.session.repo

import com.dungle.trackme.model.Session

interface SessionRepository {
    suspend fun getSessionCount() : Int
    suspend fun getAllSession() : List<Session>
    suspend fun getSessionById(id : Int) : Session
    suspend fun saveSession(session: Session) : Long
}