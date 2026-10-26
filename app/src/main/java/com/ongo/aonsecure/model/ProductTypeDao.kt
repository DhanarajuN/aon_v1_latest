package com.ongo.aonsecure.model

import androidx.room.*

@Dao
interface ProductTypeDao {

    @get:Query("SELECT * FROM PRODUCT_TYPE_EMPTY _table")
    val all: List<ProductType>

    @Insert
    fun insert(note: ProductType)

    @Update
    fun update(note: ProductType)

    @Delete
    fun delete(note: ProductType)

    @Query("DELETE FROM PRODUCT_TYPE_EMPTY _table")
    fun deleteAllNotes()

    /*    @Query("SELECT * FROM note_table ORDER BY priority DESC")
        fun getAllNotes(): LiveData<List<Note>>*/

    @Insert
    fun insertAll(vararg note: ProductType)


}
