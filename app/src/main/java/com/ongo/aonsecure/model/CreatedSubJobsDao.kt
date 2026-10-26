package com.ongo.aonsecure.model

import androidx.room.*

@Dao
interface CreatedSubJobsDao {

    @get:Query("SELECT * FROM create_sub_job_table")
    val all: List<CreatedSubJobs>

    @Insert
    fun insert(note: CreatedSubJobs)

    @Update
    fun update(note: CreatedSubJobs)

    @Delete
    fun delete(note: CreatedSubJobs)

    @Query("DELETE FROM create_sub_job_table")
    fun deleteAllNotes()

/*    @Query("SELECT * FROM note_table ORDER BY priority DESC")
    fun getAllNotes(): LiveData<List<Note>>*/

    @Insert
    fun insertAll(vararg note: CreatedSubJobs)

}
