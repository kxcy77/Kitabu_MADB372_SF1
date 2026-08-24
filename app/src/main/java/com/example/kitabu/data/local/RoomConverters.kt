package com.example.kitabu.data.local

import androidx.room.TypeConverter

class RoomConverters {
    @TypeConverter
    fun fromBookingStatus(status: BookingStatus): String = status.name

    @TypeConverter
    fun toBookingStatus(value: String): BookingStatus = BookingStatus.valueOf(value)
}
