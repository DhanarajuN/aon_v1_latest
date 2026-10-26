package com.ongo.aonsecure.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName
import com.ongo.aonsecure.utils.JobConverter


@Entity(tableName = "prod_cat_table")
data class ProdCat(
    @PrimaryKey(autoGenerate = true)
    var status: Int,
    @TypeConverters(JobConverter::class)
    @SerializedName("jobs")
    @ColumnInfo(name = "jobs")
    var jobs: List<Job>
)

