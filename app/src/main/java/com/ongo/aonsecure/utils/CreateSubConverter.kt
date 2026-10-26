package com.ongo.aonsecure.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ongo.aonsecure.model.CreatedSubJobs
import java.util.*

class CreateSubConverter {
    var gson = Gson()

    @TypeConverter
    fun fromTimestamp(data: String?): List<CreatedSubJobs>? {

        if (data == null) {
            return Collections.emptyList()
        }
        val listType = object : TypeToken<ArrayList<CreatedSubJobs>>() {}.type

        return gson.fromJson(data, listType)


    }

    @TypeConverter
    fun someObjectListToString(someObjects: List<CreatedSubJobs>?): String? {
        return gson.toJson(someObjects)
    }


}