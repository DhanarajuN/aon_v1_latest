package com.ongo.aonsecure.model

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id


@Entity
data class OfflineJson(

    @Id var ids: Long = 0,
    var id: Int,
    var QuoteNo: String?,
    var jsonData: String?
)