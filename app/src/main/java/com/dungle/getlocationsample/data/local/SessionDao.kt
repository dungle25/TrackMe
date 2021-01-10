package com.dungle.getlocationsample.data.local

import androidx.room.*
import com.dungle.getlocationsample.model.Session


@Dao
interface SessionDao {
    @Query("SELECT * FROM session_table")
    fun getAllSession(): List<Session>

    @Query("SELECT * FROM session_table WHERE id LIKE :sessionId")
    fun getSessionById(sessionId: Int): Session

    @Query("SELECT COUNT(id) FROM session_table")
    fun getSessionCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertSession(session: Session): Long

    @Update
    fun updateSession(session: Session)

    @Transaction
    fun saveToLocal(key: String, data: Session) {
        val saveData = Session()
        val id: Long = insertSession(saveData)
        if (id == -1L) {
            updateSession(saveData)
        }
    }
}