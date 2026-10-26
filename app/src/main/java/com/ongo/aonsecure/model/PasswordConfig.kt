package com.ongo.aonsecure.model

import com.google.gson.annotations.SerializedName


data class PasswordConfig(
    var status: Int,
    @SerializedName("jobs")
    var jobs: List<Job>
)

