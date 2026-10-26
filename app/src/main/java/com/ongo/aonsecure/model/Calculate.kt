package com.ongo.aonsecure.model

import androidx.room.*
import com.google.gson.annotations.SerializedName
import com.ongo.aonsecure.utils.JobConverter1


@Entity(tableName = "calculate_table")
data class Calculate(
    @PrimaryKey(autoGenerate = true)
    var status: Int,
    @TypeConverters(JobConverter1::class)
    @SerializedName("jobs")
    var jobs: List<Calculate1>
)

