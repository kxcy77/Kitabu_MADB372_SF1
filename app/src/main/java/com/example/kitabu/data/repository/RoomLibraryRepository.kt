package com.example.kitabu.data.repository

import com.example.kitabu.data.local.BookEntity
import com.example.kitabu.data.local.BookingEntity
import com.example.kitabu.data.local.BookingStatus
import com.example.kitabu.data.local.BookingWithBook
import com.example.kitabu.data.local.LibraryDao
import com.example.kitabu.data.local.TransactionResult
import com.example.kitabu.domain.LibraryTime
import com.example.kitabu.domain.model.AvailabilityFilter
import com.example.kitabu.domain.model.Book
import com.example.kitabu.domain.model.BookingState
import com.example.kitabu.domain.model.BookingSummary
import com.example.kitabu.domain.model.LibraryActionResult
import com.example.kitabu.domain.repository.LibraryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock

class RoomLibraryRepository(
    private val dao: LibraryDao,
    private val clock: Clock,
) : LibraryRepository {
    override fun observeBooks(query: String, filter: AvailabilityFilter): Flow<List<Book>> {
        val availability = when (filter) {
            AvailabilityFilter.ALL -> null
            AvailabilityFilter.AVAILABLE -> true
            AvailabilityFilter.BORROWED -> false
        }
        return dao.observeBooks(query.trim(), availability).map { books -> books.map(BookEntity::toDomain) }
    }

    override fun observeOpenBookings(): Flow<List<BookingSummary>> =
        dao.observeOpenBookings().map { bookings -> bookings.map(BookingWithBook::toDomain) }

    override suspend fun reserveBook(
        bookId: Long,
        userName: String,
        durationDays: Int,
    ): LibraryActionResult {
        if (!LibraryTime.isValidUserName(userName) || durationDays !in setOf(7, 14, 21)) {
            return LibraryActionResult.VALIDATION_ERROR
        }
        val now = clock.millis()
        return runCatching {
            dao.reserveBook(
                BookingEntity(
                    bookOwnerId = bookId,
                    userName = LibraryTime.normalizedUserName(userName),
                    bookingDate = now,
                    returnDeadline = LibraryTime.deadlineFrom(clock, durationDays),
                    status = BookingStatus.PENDING,
                ),
            ).toDomain()
        }.getOrDefault(LibraryActionResult.FAILURE)
    }

    override suspend fun pickUpBooking(bookingId: Long): LibraryActionResult =
        safely { dao.pickUpBooking(bookingId) }

    override suspend fun renewBooking(bookingId: Long): LibraryActionResult =
        safely { dao.renewBooking(bookingId, clock.millis()) }

    override suspend fun returnBooking(bookingId: Long): LibraryActionResult =
        safely { dao.returnBooking(bookingId) }

    override suspend fun cancelBooking(bookingId: Long): LibraryActionResult =
        safely { dao.cancelBooking(bookingId) }

    private suspend fun safely(block: suspend () -> TransactionResult): LibraryActionResult =
        runCatching { block().toDomain() }.getOrDefault(LibraryActionResult.FAILURE)
}

private fun BookEntity.toDomain() = Book(bookId, title, author, category, isAvailable)

private fun BookingWithBook.toDomain() = BookingSummary(
    id = bookingId,
    bookId = bookOwnerId,
    userName = userName,
    bookingDate = bookingDate,
    returnDeadline = returnDeadline,
    status = when (status) {
        BookingStatus.PENDING -> BookingState.PENDING
        BookingStatus.ACTIVE -> BookingState.ACTIVE
        BookingStatus.RETURNED -> error("Returned bookings are excluded by the query")
    },
    title = title,
    author = author,
    category = category,
)

private fun TransactionResult.toDomain() = when (this) {
    TransactionResult.SUCCESS -> LibraryActionResult.SUCCESS
    TransactionResult.NOT_FOUND -> LibraryActionResult.NOT_FOUND
    TransactionResult.INVALID_STATE -> LibraryActionResult.INVALID_STATE
    TransactionResult.UNAVAILABLE -> LibraryActionResult.UNAVAILABLE
}
