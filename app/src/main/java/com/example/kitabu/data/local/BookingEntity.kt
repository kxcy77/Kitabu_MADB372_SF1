package com.example.kitabu.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class BookingStatus {
    PENDING,
    ACTIVE,
    RETURNED,
}

@Entity(
    tableName = "bookings",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["book_id"],
            childColumns = ["book_owner_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["book_owner_id"])],
)
data class BookingEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "booking_id")
    val bookingId: Long = 0,
    @ColumnInfo(name = "book_owner_id")
    val bookOwnerId: Long,
    @ColumnInfo(name = "user_name")
    val userName: String,
    @ColumnInfo(name = "booking_date")
    val bookingDate: Long,
    @ColumnInfo(name = "return_deadline")
    val returnDeadline: Long,
    val status: BookingStatus,
)
