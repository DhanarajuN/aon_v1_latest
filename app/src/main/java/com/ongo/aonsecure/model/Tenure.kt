package com.ongo.aonsecure.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName
import com.ongo.aonsecure.utils.JobConverter


@Entity(tableName = "tenure_table")
data class Tenure(
    @PrimaryKey(autoGenerate = true)
    var status: Int,
    @TypeConverters(JobConverter::class)
    @SerializedName("jobs")
    var jobs: List<Job>
)

