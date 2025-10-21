package com.gamsung2.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface EventDao {
    @Query("SELECT * FROM events WHERE date BETWEEN :start AND :end ORDER BY date, startTime NULLS FIRST, id")
    fun observeBetween(start: LocalDate, end: LocalDate): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE date = :date ORDER BY startTime NULLS FIRST, id")
    fun observeOn(date: LocalDate): Flow<List<Event>>

    @Insert suspend fun insert(e: Event): Long
    @Update suspend fun update(e: Event)
    @Delete suspend fun delete(e: Event)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteById(id: Long)
}
