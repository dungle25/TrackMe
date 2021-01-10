package com.dungle.getlocationsample.data.repo

import com.dungle.getlocationsample.model.Session

interface SessionRepository {
    suspend fun getSessionCount() : Int
    suspend fun getAllSession() : List<Session>
}