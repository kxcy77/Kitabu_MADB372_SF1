package com.example.kitabu.domain.repository

import com.example.kitabu.domain.model.AvailabilityFilter
import com.example.kitabu.domain.model.Book
import com.example.kitabu.domain.model.BookingSummary
import com.example.kitabu.domain.model.LibraryActionResult
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {
    fun observeBooks(query: String, filter: AvailabilityFilter): Flow<List<Book>>
    fun observeOpenBookings(): Flow<List<BookingSummary>>
    suspend fun reserveBook(bookId: Long, userName: String, durationDays: Int): LibraryActionResult
    suspend fun pickUpBooking(bookingId: Long): LibraryActionResult
    suspend fun renewBooking(bookingId: Long): LibraryActionResult
    suspend fun returnBooking(bookingId: Long): LibraryActionResult
    suspend fun cancelBooking(bookingId: Long): LibraryActionResult
}
