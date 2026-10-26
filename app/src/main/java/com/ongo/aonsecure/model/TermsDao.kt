package com.ongo.aonsecure.model

import androidx.room.*

@Dao
interface TermsDao {

    @get:Query("SELECT * FROM terms_table")
    val all: List<Terms>

    @Insert
    fun insert(note: Terms)

    @Update
    fun update(note: Terms)

    @Delete
    fun delete(note: Terms)

    @Query("DELETE FROM terms_table")
    fun deleteAllNotes()

/*    @Query("SELECT * FROM note_table ORDER BY priority DESC")
    fun getAllNotes(): LiveData<List<Note>>*/

    @Insert
    fun insertAll(vararg note: Terms)

}
