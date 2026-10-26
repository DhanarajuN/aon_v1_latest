package com.ongo.aonsecure.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ongo.aonsecure.model.Job
import java.util.*

class JobConverter {
    var gson = Gson()

    @TypeConverter
    fun fromTimestamp(data: String?): List<Job>? {

        if (data == null) {
            return Collections.emptyList()
        }
        val listType = object : TypeToken<ArrayList<Job>>() {}.type

        return gson.fromJson(data, listType)


    }

    @TypeConverter
    fun someObjectListToString(someObjects: List<Job>?): String? {
        return gson.toJson(someObjects)
    }


}