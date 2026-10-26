package com.ongo.aonsecure.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName
import com.ongo.aonsecure.utils.CreateSubConverter

@Entity(tableName = "insurer_job_table")
data class InsurerJob(
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    var id: Int = 0,
    @SerializedName("Name")
    @ColumnInfo(name = "Name")
    var Name: String,

    @ColumnInfo(name = "Zone")
    @SerializedName("Zone")
    var Zone: String,
    @SerializedName("Insurer")
    var Insurer: String

)