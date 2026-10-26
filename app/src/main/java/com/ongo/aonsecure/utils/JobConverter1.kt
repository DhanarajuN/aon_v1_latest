package com.ongo.aonsecure.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ongo.aonsecure.model.Calculate1
import java.util.*

class JobConverter1 {
    var gson = Gson()

    @TypeConverter
    fun fromTimestamp(data: String?): List<Calculate1>? {

        if (data == null) {
            return Collections.emptyList()
        }
        val listType = object : TypeToken<ArrayList<Calculate1>>() {}.type

        return gson.fromJson(data, listType)


    }

    @TypeConverter
    fun someObjectListToString(someObjects: List<Calculate1>?): String? {
        return gson.toJson(someObjects)
    }


}