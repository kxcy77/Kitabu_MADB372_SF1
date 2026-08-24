package com.example.kitabu.data.local

import androidx.room.ColumnInfo

data class BookingWithBook(
    @ColumnInfo(name = "booking_id") val bookingId: Long,
    @ColumnInfo(name = "book_owner_id") val bookOwnerId: Long,
    @ColumnInfo(name = "user_name") val userName: String,
    @ColumnInfo(name = "booking_date") val bookingDate: Long,
    @ColumnInfo(name = "return_deadline") val returnDeadline: Long,
    val status: BookingStatus,
    val title: String,
    val author: String,
    val category: String,
)

enum class TransactionResult {
    SUCCESS,
    NOT_FOUND,
    INVALID_STATE,
    UNAVAILABLE,
}
