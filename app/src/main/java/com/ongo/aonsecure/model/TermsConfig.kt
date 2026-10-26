package com.ongo.aonsecure.model

import com.google.gson.annotations.SerializedName

data class TermsConfig (
    var status: Int,
    @SerializedName("jobs")
    var jobs: List<Job>)
