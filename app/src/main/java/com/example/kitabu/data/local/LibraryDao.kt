package com.example.kitabu.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

@Dao
abstract class LibraryDao {
    @Query(
        """
        SELECT * FROM books
        WHERE (:availability IS NULL OR is_available = :availability)
          AND (
            :query = '' OR
            title LIKE '%' || :query || '%' COLLATE NOCASE OR
            author LIKE '%' || :query || '%' COLLATE NOCASE
          )
        ORDER BY title COLLATE NOCASE ASC
        """,
    )
    abstract fun observeBooks(query: String, availability: Boolean?): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE is_available = :isAvailable ORDER BY title COLLATE NOCASE")
    abstract fun observeBooksByAvailability(isAvailable: Boolean): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE book_id = :bookId")
    abstract suspend fun getBook(bookId: Long): BookEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insertBook(book: BookEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insertBooks(books: List<BookEntity>): List<Long>

    @Update
    abstract suspend fun updateBook(book: BookEntity): Int

    @Delete
    abstract suspend fun deleteBook(book: BookEntity): Int

    @Query(
        """
        SELECT
            bookings.booking_id,
            bookings.book_owner_id,
            bookings.user_name,
            bookings.booking_date,
            bookings.return_deadline,
            bookings.status,
            books.title,
            books.author,
            books.category
        FROM bookings
        INNER JOIN books ON books.book_id = bookings.book_owner_id
        WHERE bookings.status IN ('PENDING', 'ACTIVE')
        ORDER BY bookings.return_deadline ASC
        """,
    )
    abstract fun observeOpenBookings(): Flow<List<BookingWithBook>>

    @Query("SELECT * FROM bookings WHERE booking_id = :bookingId")
    abstract suspend fun getBooking(bookingId: Long): BookingEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insertBooking(booking: BookingEntity): Long

    @Update
    abstract suspend fun updateBooking(booking: BookingEntity): Int

    @Delete
    abstract suspend fun deleteBooking(booking: BookingEntity): Int

    @Query("UPDATE books SET is_available = 0 WHERE book_id = :bookId AND is_available = 1")
    protected abstract suspend fun claimAvailableBook(bookId: Long): Int

    @Query("UPDATE books SET is_available = 1 WHERE book_id = :bookId")
    protected abstract suspend fun releaseBook(bookId: Long): Int

    @Query("UPDATE bookings SET status = 'ACTIVE' WHERE booking_id = :bookingId AND status = 'PENDING'")
    protected abstract suspend fun markActive(bookingId: Long): Int

    @Query("UPDATE bookings SET status = 'RETURNED' WHERE booking_id = :bookingId AND status = 'ACTIVE'")
    protected abstract suspend fun markReturned(bookingId: Long): Int

    @Query("UPDATE bookings SET return_deadline = :deadline WHERE booking_id = :bookingId AND status = 'ACTIVE'")
    protected abstract suspend fun updateDeadline(bookingId: Long, deadline: Long): Int

    @Query("DELETE FROM bookings WHERE booking_id = :bookingId AND status = 'PENDING'")
    protected abstract suspend fun deletePending(bookingId: Long): Int

    @Transaction
    open suspend fun reserveBook(booking: BookingEntity): TransactionResult {
        if (claimAvailableBook(booking.bookOwnerId) != 1) return TransactionResult.UNAVAILABLE
        insertBooking(booking.copy(status = BookingStatus.PENDING))
        return TransactionResult.SUCCESS
    }

    @Transaction
    open suspend fun pickUpBooking(bookingId: Long): TransactionResult {
        val booking = getBooking(bookingId) ?: return TransactionResult.NOT_FOUND
        if (booking.status != BookingStatus.PENDING) return TransactionResult.INVALID_STATE
        return if (markActive(bookingId) == 1) TransactionResult.SUCCESS else TransactionResult.INVALID_STATE
    }

    @Transaction
    open suspend fun renewBooking(bookingId: Long, now: Long): TransactionResult {
        val booking = getBooking(bookingId) ?: return TransactionResult.NOT_FOUND
        if (booking.status != BookingStatus.ACTIVE) return TransactionResult.INVALID_STATE
        val newDeadline = maxOf(booking.returnDeadline, now) + TimeUnit.DAYS.toMillis(7)
        return if (updateDeadline(bookingId, newDeadline) == 1) {
            TransactionResult.SUCCESS
        } else {
            TransactionResult.INVALID_STATE
        }
    }

    @Transaction
    open suspend fun returnBooking(bookingId: Long): TransactionResult {
        val booking = getBooking(bookingId) ?: return TransactionResult.NOT_FOUND
        if (booking.status != BookingStatus.ACTIVE) return TransactionResult.INVALID_STATE
        if (markReturned(bookingId) != 1) return TransactionResult.INVALID_STATE
        releaseBook(booking.bookOwnerId)
        return TransactionResult.SUCCESS
    }

    @Transaction
    open suspend fun cancelBooking(bookingId: Long): TransactionResult {
        val booking = getBooking(bookingId) ?: return TransactionResult.NOT_FOUND
        if (booking.status != BookingStatus.PENDING) return TransactionResult.INVALID_STATE
        if (deletePending(bookingId) != 1) return TransactionResult.INVALID_STATE
        releaseBook(booking.bookOwnerId)
        return TransactionResult.SUCCESS
    }
}
