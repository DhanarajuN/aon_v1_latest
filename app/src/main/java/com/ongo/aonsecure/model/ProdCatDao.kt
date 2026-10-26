package com.ongo.aonsecure.model

import androidx.room.*

@Dao
interface ProdCatDao {

    @get:Query("SELECT * FROM prod_cat_table")
    val all: List<ProdCat>

    @Insert
    fun insert(note: ProdCat)

    @Update
    fun update(note: ProdCat)

    @Delete
    fun delete(note: ProdCat)

    @Query("DELETE FROM prod_cat_table")
    fun deleteAllNotes()

/*    @Query("SELECT * FROM note_table ORDER BY priority DESC")
    fun getAllNotes(): LiveData<List<Note>>*/

    @Insert
    fun insertAll(vararg note: ProdCat)

}
