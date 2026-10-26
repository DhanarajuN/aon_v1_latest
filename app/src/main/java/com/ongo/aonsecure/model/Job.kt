package com.ongo.aonsecure.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName
import com.ongo.aonsecure.utils.CreateSubConverter

@Entity(tableName = "job_table")
data class Job(
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    var id: Int = 0,
    @SerializedName("Name")
    @ColumnInfo(name = "Name")
    var Name: String,
    @SerializedName("Tenure")
    var Tenure: String,
    @ColumnInfo(name = "Zone")
    @SerializedName("Zone")
    var Zone: String,
    @SerializedName("Insurer")
    var Insurer: String,
    @SerializedName("Category")
    var Category: String,
    @SerializedName("From")
    var From: String,
    @SerializedName("To")
    var To: String,
    @SerializedName("ProdCategory")
    var ProdCategory: String,
    @SerializedName("ProductCategory")
    var ProductCategory: String,
    @SerializedName("Product")
    var Product: String,
    @SerializedName("Product Type")
    var ProductType: String,
    @SerializedName("FundingType")
    var FundingType: String,
    @SerializedName("CalculatorVersion")
    var CalculatorVersion: String,
    @SerializedName("LengthOfPassword")
    var LengthOfPassword: String,
    @SerializedName("UpperCase")
    var UpperCase: String,
    @SerializedName("SpecialCharacter")
    var SpecialCharacter: String,
    @SerializedName("Current_Job_Status")
    var Current_Job_Status: String,
    @SerializedName("NotAllowed")
    var NotAllowed: String,
    @SerializedName("Gender")
    var Gender: String,
    @SerializedName("MinimumAge")
    var MinimumAge: String,
    @SerializedName("MaximumAge")
    var MaximumAge: String?,
    @SerializedName("TERMS_LINK")
    var TERMS_LINK:String,
    @SerializedName("IS_TERMS_ENABLE")
    var IS_TERMS_ENABLE:String,
    @SerializedName("MaximumSumAssured")
    var MaximumSumAssured:String,

    @TypeConverters(CreateSubConverter::class)
    @SerializedName("CreatedSubJobs")
    var CreatedSubJobs: List<CreatedSubJobs>
)