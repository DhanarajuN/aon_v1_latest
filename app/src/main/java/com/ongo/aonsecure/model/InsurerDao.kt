package com.ongo.aonsecure.model

import androidx.room.*

@Dao
interface InsurerDao {

    @get:Query("SELECT * FROM insurer_table")
    val all: List<Insurer>

    @Insert
    fun insert(note: Insurer)

    @Update
    fun update(note: Insurer)

    @Delete
    fun delete(note: Insurer)

    @Query("DELETE FROM insurer_table")
    fun deleteAllNotes()

/*    @Query("SELECT * FROM note_table ORDER BY priority DESC")
    fun getAllNotes(): LiveData<List<Note>>*/

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg note: Insurer)

}
