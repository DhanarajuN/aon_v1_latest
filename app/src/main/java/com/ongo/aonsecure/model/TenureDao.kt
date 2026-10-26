package com.ongo.aonsecure.model

import androidx.room.*

@Dao
interface TenureDao {

    @get:Query("SELECT * FROM tenure_table")
    val all: List<Tenure>

    @Insert
    fun insert(note: Tenure)

    @Update
    fun update(note: Tenure)

    @Delete
    fun delete(note: Tenure)

    @Query("DELETE FROM tenure_table")
    fun deleteAllNotes()

/*    @Query("SELECT * FROM note_table ORDER BY priority DESC")
    fun getAllNotes(): LiveData<List<Note>>*/

    @Insert
    fun insertAll(vararg note: Tenure)

}
