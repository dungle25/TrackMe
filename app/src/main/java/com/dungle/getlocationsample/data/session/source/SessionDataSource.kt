package com.dungle.getlocationsample.data.session.source

import com.dungle.getlocationsample.model.Session

interface SessionDataSource {
    suspend fun getAllSession(): List<Session>
    suspend fun getSessionCount(): Int
    suspend fun getSessionById(id : Int) : Session
    suspend fun saveSession(session: Session) : Long
}