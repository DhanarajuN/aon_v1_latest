package com.ongo.aonsecure.model

import androidx.room.*

@Dao
interface JobCommentDao {

    @get:Query("SELECT * FROM job_comment")
    val all: List<JobComments>

    @Insert
    fun insert(note: JobComments)

    @Update
    fun update(note: JobComments)

    @Delete
    fun delete(note: JobComments)

    @Query("DELETE FROM job_comment")
    fun deleteAllNotes()

/*    @Query("SELECT * FROM note_table ORDER BY priority DESC")
    fun getAllNotes(): LiveData<List<Note>>*/

    @Insert
    fun insertAll(vararg note: JobComments)
}