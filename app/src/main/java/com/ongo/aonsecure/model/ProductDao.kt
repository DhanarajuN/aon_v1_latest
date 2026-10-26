package com.ongo.aonsecure.model

import androidx.room.*

@Dao
interface ProductDao {

    @get:Query("SELECT * FROM product_table")
    val all: List<Product>

    @Insert
    fun insert(note: Product)

    @Update
    fun update(note: Product)

    @Delete
    fun delete(note: Product)

    @Query("DELETE FROM product_table")
    fun deleteAllNotes()

/*    @Query("SELECT * FROM note_table ORDER BY priority DESC")
    fun getAllNotes(): LiveData<List<Note>>*/

    @Insert
    fun insertAll(vararg note: Product)


}
