package com.ongo.aonsecure.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName
import com.ongo.aonsecure.utils.CreateSubConverter

@Entity(tableName = "calculate_table1")
data class Calculate1(
    @PrimaryKey(autoGenerate = true)
    @SerializedName("jobId")
    var jobId: Int = 0,
    @SerializedName("id")
    var id: Int = 0,
    @SerializedName("Name")
    @ColumnInfo(name = "Name")
    var Name: String,
    @SerializedName("Tenure")
    var Tenure: String?=null ,
    @ColumnInfo(name = "Zone")
    @SerializedName("Zone")
    var Zone: String,
    @SerializedName("Insurer")
    var Insurer: String,
    @SerializedName("Category")
    var Category: String?=null,
    @SerializedName("From")
    var From: String,
    @SerializedName("To")
    var To: String,
    @SerializedName("ProdCategory")
    var ProdCategory: String?=null,
    @SerializedName("ProductCategory")
    var ProductCategory: String,
    @SerializedName("Product")
    var Product: String,

    @SerializedName("Product_Type")
    val productType: String? = "Unknown",
    @SerializedName("Type")
    val Type: String? = "Unknown",

    @SerializedName("FundingType")
    var FundingType: String,
    @SerializedName("CalculatorVersion")
    var CalculatorVersion: String,
    @SerializedName("LengthOfPassword")
    var LengthOfPassword: String?=null,
    @SerializedName("UpperCase")
    var UpperCase: String?=null,
    @SerializedName("SpecialCharacter")
    var SpecialCharacter: String?=null,
    @SerializedName("Current_Job_Status")
    var Current_Job_Status: String,
    @SerializedName("NotAllowed")
    var NotAllowed: String?=null,
    @SerializedName("Gender")
    var Gender: String,


    @TypeConverters(CreateSubConverter::class)
    @SerializedName("CreatedSubJobs")
    var CreatedSubJobs: List<CreatedSubJobs>
)