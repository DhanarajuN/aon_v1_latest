package com.ongo.aonsecure.model

import com.google.gson.annotations.SerializedName


data class QuoteList(
    var status: Int,
    @SerializedName("jobs")
    var jobs: List<Post>
)

