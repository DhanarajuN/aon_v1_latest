package com.ongo.aonsecure.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ongo.aonsecure.model.JobComments
import java.util.*

class JobCommentsConverter {
    var gson = Gson()

    @TypeConverter
    fun fromTimestamp(data: String?): List<JobComments>? {

        if (data == null) {
            return Collections.emptyList()
        }
        val listType = object : TypeToken<ArrayList<JobComments>>() {}.type

        return gson.fromJson(data, listType)


    }

    @TypeConverter
    fun someObjectListToString(someObjects: List<JobComments>?): String? {
        return gson.toJson(someObjects)
    }

}