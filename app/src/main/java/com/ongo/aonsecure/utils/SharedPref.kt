package com.ongo.aonsecure.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SharedPref {
    lateinit var mSharedPref: SharedPreferences

    fun initialize(context: Context) {
        mSharedPref = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE)
    }


    fun read(key: String?, defValue: String?): String? {
        return mSharedPref.getString(key, defValue)
    }


    fun write(key: String?, value: String?) {
        val prefsEditor: SharedPreferences.Editor = mSharedPref.edit()
        prefsEditor.putString(key, value)
        prefsEditor.commit()
    }
    fun readArrayList(key: String?): ArrayList<String> {
        val json = mSharedPref.getString(key, null)
        return if (json == null) {
            ArrayList()
        } else {
            val type = object : TypeToken<ArrayList<String>>() {}.type
            Gson().fromJson(json, type)
        }
    }

    fun writeArrayList(key: String?, arrayList: ArrayList<String>) {
        val prefsEditor: SharedPreferences.Editor = mSharedPref.edit()
        val json = Gson().toJson(arrayList)
        prefsEditor.putString(key, json)
        prefsEditor.apply()
    }

    fun read(key: String?, defValue: Boolean): Boolean {
        return mSharedPref.getBoolean(key, defValue)
    }

    fun write(key: String?, value: Boolean) {
        val prefsEditor: SharedPreferences.Editor = mSharedPref.edit()
        prefsEditor.putBoolean(key, value)
        prefsEditor.commit()
    }

    fun clearPref(){
        val prefsEditor: SharedPreferences.Editor = mSharedPref.edit()
        prefsEditor.clear()
        prefsEditor.apply()

    }
}