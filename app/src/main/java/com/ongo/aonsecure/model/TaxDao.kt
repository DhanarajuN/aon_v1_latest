package com.ongo.aonsecure.model

import androidx.room.*

@Dao
interface TaxDao {
    @get:Query("SELECT * FROM tax_table")
    val all: List<Tax>

    @Query("SELECT * FROM tax_table WHERE tax=:id")
    fun loadSingle(id: String): Tax

    @Insert
    fun insert(note: Tax)

    @Update
    fun update(note: Tax)

    @Delete
    fun delete(note: Tax)

    @Query("DELETE FROM tax_table")
    fun deleteAllNotes()


    @Insert
    fun insertAll(vararg note: Tax)
}