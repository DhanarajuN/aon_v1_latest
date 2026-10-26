package com.ongo.aonsecure.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "tax_table")
data class Tax(
    @PrimaryKey(autoGenerate = true)
    var status: Int,
    @SerializedName("tax")
    @ColumnInfo(name = "tax")
    var tax: String
)