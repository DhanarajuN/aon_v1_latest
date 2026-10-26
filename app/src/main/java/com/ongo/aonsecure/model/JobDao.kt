package com.ongo.aonsecure.model

import androidx.room.*

@Dao
interface JobDao {

    @get:Query("SELECT * FROM job_table")
    val all: List<Job>

    @Query("SELECT * FROM job_table WHERE Name=:id")
    fun loadSingle(id: String): Job

    @Insert
    fun insert(note: Job)

    @Update
    fun update(note: Job)

    @Delete
    fun delete(note: Job)

    @Query("DELETE FROM job_table")
    fun deleteAllNotes()

/*    @Query("SELECT * FROM note_table ORDER BY priority DESC")
    fun getAllNotes(): LiveData<List<Note>>*/

    @Insert
    fun insertAll(vararg note: Job)
}