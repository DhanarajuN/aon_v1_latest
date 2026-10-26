package com.ongo.aonsecure.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "job_comment")
data class JobComments (

    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    var id: Int = 0,
    @SerializedName("commentId")
    val commentid: String,
    @SerializedName("postedBy_Id")
    val postedbyId: String,
    @SerializedName("postedBy_Name")
    val postedbyName: String,
    @SerializedName("Job_Id")
    val jobId: String,
    @SerializedName("Job_Name")
    val jobName: String,
    @SerializedName("comment")
    val comment: String,
    @SerializedName("time")
    val time: String,
    @SerializedName("disabled")
    val disabled: String,
    @SerializedName("logo")
    val logo: String
)