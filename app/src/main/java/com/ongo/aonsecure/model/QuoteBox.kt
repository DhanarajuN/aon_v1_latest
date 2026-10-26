package com.ongo.aonsecure.model

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class QuoteBox(
    @Id var id: Long = 0,
    var Name: String?,
    var Number: Int?

)