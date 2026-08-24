package com.example.kitabu.domain.model

enum class AvailabilityFilter {
    ALL,
    AVAILABLE,
    BORROWED,
}

enum class RentalDuration(val days: Int) {
    SEVEN_DAYS(7),
    FOURTEEN_DAYS(14),
    TWENTY_ONE_DAYS(21),
}

enum class BookingState {
    PENDING,
    ACTIVE,
}

data class Book(
    val id: Long,
    val title: String,
    val author: String,
    val category: String,
    val isAvailable: Boolean,
)

data class BookingSummary(
    val id: Long,
    val bookId: Long,
    val userName: String,
    val bookingDate: Long,
    val returnDeadline: Long,
    val status: BookingState,
    val title: String,
    val author: String,
    val category: String,
)

sealed interface DueStatus {
    data class Remaining(val days: Long) : DueStatus
    data object DueToday : DueStatus
    data class Overdue(val days: Long) : DueStatus
}

enum class LibraryActionResult {
    SUCCESS,
    UNAVAILABLE,
    NOT_FOUND,
    INVALID_STATE,
    VALIDATION_ERROR,
    FAILURE,
}
