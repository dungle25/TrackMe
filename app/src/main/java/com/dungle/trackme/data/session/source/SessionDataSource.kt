package com.dungle.trackme.data.session.source

import com.dungle.trackme.model.Session

interface SessionDataSource {
    suspend fun getAllSession(): List<Session>
    suspend fun getSessionCount(): Int
    suspend fun getSessionById(id : Int) : Session
    suspend fun saveSession(session: Session) : Long
}