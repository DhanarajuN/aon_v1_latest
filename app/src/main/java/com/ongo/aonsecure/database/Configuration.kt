package com.ongo.aonsecure.database

import android.app.Application

/**
 * Created by Amirahmad Adibi.
 * ObjectBoxSample | Copyrights 2019-11-10.
 */

class Configuration:Application() {
    override fun onCreate() {
        super.onCreate()
        ObjectBox.init(this)
    }
}