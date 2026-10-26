package com.ongo.aonsecure.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "create_sub_job_table")
data class CreatedSubJobs(
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    var id: Int = 0,
    @SerializedName("MinimumAge")
    var MinimumAge: String,
    @SerializedName("minAge")
    var minAge: String,
    @SerializedName("maxAge")
    var maxAge: String,
    @SerializedName("MaximumAge")
    var MaximumAge: String,
    @SerializedName("Category")
    var Category: String,
    @SerializedName("SumInsured")
    var SumInsured: String,
    @SerializedName("minSumInsured")
    var minSumInsured: String,
    @SerializedName("maxSumInsured")
    var maxSumInsured: String,
    @SerializedName("Tenure")
    var Tenure: String,
    @SerializedName("minTenure")
    var minTenure: String,
    @SerializedName("maxTenure")
    var maxTenure: String,
    @SerializedName("Premium")
    var Premium: String,
    @SerializedName("CalculatorVersion")
    var CalculatorVersion: String,
    @SerializedName("message")
    var message: String,
    @SerializedName("jobTypeName")
    var jobTypeName: String,
    @SerializedName("Start_Age")
    var Start_Age: String,
    @SerializedName("End_Age")
    var End_Age: String,
    @SerializedName("Sum_Insured")
    var Sum_Insured: String

)