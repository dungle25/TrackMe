package com.dungle.getlocationsample.data.local

import androidx.room.*
import com.dungle.getlocationsample.data.local.model.LocalSession

@Dao
interface SessionDao {
    @Query("SELECT * FROM session_table")
    fun getAllSession(): List<LocalSession>

    @Query("SELECT * FROM session_table WHERE id LIKE :sessionId")
    fun getSessionById(sessionId: Int): LocalSession

    @Query("SELECT COUNT(id) FROM session_table")
    fun getSessionCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertSession(session: LocalSession): Long

    @Update
    fun updateSession(session: LocalSession)

    @Transaction
    fun saveToLocal(key: String, data: LocalSession) {
        val saveData = LocalSession()
        val id: Long = insertSession(saveData)
        if (id == -1L) {
            updateSession(saveData)
        }
    }
}