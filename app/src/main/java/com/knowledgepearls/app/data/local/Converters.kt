package com.knowledgepearls.app.data.local

import androidx.room.TypeConverter

class Converters {
    private companion object {
        const val TAG_DELIMITER = "\u001F"
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String = value.joinToString(TAG_DELIMITER)

    @TypeConverter
    fun toStringList(value: String): List<String> =
        if (value.isBlank()) {
            emptyList()
        } else {
            value.split(TAG_DELIMITER)
        }
}
