package com.ongo.aonsecure.model

import androidx.room.*

@Dao
interface LoanTypeDao {

    @get:Query("SELECT * FROM loantype_table")
    val all: List<LoanType>

    @Insert
    fun insert(note: LoanType)

    @Update
    fun update(note: LoanType)

    @Delete
    fun delete(note: LoanType)

    @Query("DELETE FROM loantype_table")
    fun deleteAllNotes()

/*    @Query("SELECT * FROM note_table ORDER BY priority DESC")
    fun getAllNotes(): LiveData<List<Note>>*/

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg note: LoanType)

}
