package com.ongo.aonsecure.database

import android.content.Context
import com.ongo.aonsecure.model.MyObjectBox
import io.objectbox.BoxStore

object ObjectBox {

    lateinit var boxStore: BoxStore
        private set

    fun init(context: Context) {
        boxStore = MyObjectBox.builder().androidContext(context.applicationContext).build()
    }
}