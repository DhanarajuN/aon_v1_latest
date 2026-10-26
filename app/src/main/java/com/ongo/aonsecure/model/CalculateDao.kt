package com.ongo.aonsecure.model

import android.util.Log
import androidx.room.*

@Dao
interface CalculateDao {
//
//    @get:Query("SELECT * FROM calculate_table1 ")
//    val all: List<Calculate1>

        @Query("SELECT * FROM calculate_table1 WHERE Zone LIKE '%' || :zone || '%'")
    fun all(zone: String): List<Calculate1>



   /* @Query("SELECT * FROM calculate_table WHERE job =:zone")
    fun getQuery(zone: String): List<Job>?*/

    @Insert
    fun insert(note: Calculate1)

    @Update
    fun update(note: Calculate1)

    @Delete
    fun delete(note: Calculate1)

    @Query("DELETE FROM calculate_table1")
    fun deleteAllNotes()

/*    @Query("SELECT * FROM note_table ORDER BY priority DESC")
    fun getAllNotes(): LiveData<List<Note>>*/

    @Query("DELETE FROM calculate_table1")
    fun clear()

    @Insert
    fun insertAll(vararg note: Calculate)

}
