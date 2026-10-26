package com.ongo.aonsecure.model

import androidx.room.*

@Dao
interface QuoteDao {

    /*@get:Query("SELECT * FROM quote_table")
    val all: List<Post>

    @Insert
    fun insert(note: Post)

    @Update
    fun update(note: Post)

    @Delete
    fun delete(note: Post)

    @Query("DELETE FROM quote_table")
    fun deleteAllNotes()

*//*    @Query("SELECT * FROM note_table ORDER BY priority DESC")
    fun getAllNotes(): LiveData<List<Note>>*//*

    @Insert
    fun insertAll(vararg note: Post)*/

}