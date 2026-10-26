package com.ongo.aonsecure.model

import androidx.room.*

@Dao
interface ZoneDao {

    @get:Query("SELECT * FROM zone_table")
    val all: List<Zone>

    @Insert
    fun insert(note: Zone)

    @Update
    fun update(note: Zone)

    @Delete
    fun delete(note: Zone)

    @Query("DELETE FROM zone_table")
    fun deleteAllNotes()

/*    @Query("SELECT * FROM note_table ORDER BY priority DESC")
    fun getAllNotes(): LiveData<List<Note>>*/

    @Insert
    fun insertAll(vararg note: Zone)

}
