package com.dungle.getlocationsample.data.session.local

import androidx.room.TypeConverter
import com.dungle.getlocationsample.model.LocationData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class Converters {
    @TypeConverter
    fun locationListToString(locations: List<LocationData>): String {
        return Gson().toJson(locations)
    }

    @TypeConverter
    fun stringLocationList(data: String): List<LocationData> {
        val listType: Type = object : TypeToken<List<LocationData>>() {}.type
        return Gson().fromJson(data, listType)
    }

    @TypeConverter
    fun stringToDoubleList(value: String): List<Double> {
        val listType = object : TypeToken<List<Double>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun doubleListToString(list: List<Double>): String {
        return Gson().toJson(list)
    }
}
