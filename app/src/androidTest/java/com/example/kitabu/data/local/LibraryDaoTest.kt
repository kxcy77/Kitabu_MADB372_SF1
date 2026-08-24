package com.example.kitabu.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class LibraryDaoTest {
    private lateinit var database: KitabuDatabase
    private lateinit var dao: LibraryDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, KitabuDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.libraryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun reserveAndReturn_updateBothTablesAtomically() = runTest {
        val bookId = dao.insertBook(BookEntity(title = "Test Book", author = "Test Author", category = "Test"))
        val now = System.currentTimeMillis()
        val booking = BookingEntity(
            bookOwnerId = bookId,
            userName = "Test Student",
            bookingDate = now,
            returnDeadline = now + TimeUnit.DAYS.toMillis(14),
            status = BookingStatus.PENDING,
        )

        assertEquals(TransactionResult.SUCCESS, dao.reserveBook(booking))
        assertFalse(dao.getBook(bookId)!!.isAvailable)
        val saved = dao.observeOpenBookings().first().single()
        assertEquals(BookingStatus.PENDING, saved.status)

        assertEquals(TransactionResult.SUCCESS, dao.pickUpBooking(saved.bookingId))
        assertEquals(TransactionResult.SUCCESS, dao.returnBooking(saved.bookingId))
        assertTrue(dao.getBook(bookId)!!.isAvailable)
        assertEquals(BookingStatus.RETURNED, dao.getBooking(saved.bookingId)!!.status)
        assertTrue(dao.observeOpenBookings().first().isEmpty())
    }

    @Test
    fun doubleReservation_isRejectedWithoutSecondBooking() = runTest {
        val bookId = dao.insertBook(BookEntity(title = "Only Copy", author = "Author", category = "Test"))
        val now = System.currentTimeMillis()
        val booking = BookingEntity(
            bookOwnerId = bookId,
            userName = "First Student",
            bookingDate = now,
            returnDeadline = now + TimeUnit.DAYS.toMillis(7),
            status = BookingStatus.PENDING,
        )

        assertEquals(TransactionResult.SUCCESS, dao.reserveBook(booking))
        assertEquals(TransactionResult.UNAVAILABLE, dao.reserveBook(booking.copy(userName = "Second Student")))
        assertEquals(1, dao.observeOpenBookings().first().size)
    }

    @Test
    fun pendingCancellation_deletesBookingAndReleasesBook() = runTest {
        val bookId = dao.insertBook(BookEntity(title = "Cancelable", author = "Author", category = "Test"))
        val now = System.currentTimeMillis()
        dao.reserveBook(
            BookingEntity(
                bookOwnerId = bookId,
                userName = "Student",
                bookingDate = now,
                returnDeadline = now + TimeUnit.DAYS.toMillis(7),
                status = BookingStatus.PENDING,
            ),
        )
        val bookingId = dao.observeOpenBookings().first().single().bookingId

        assertEquals(TransactionResult.SUCCESS, dao.cancelBooking(bookingId))
        assertNull(dao.getBooking(bookingId))
        assertTrue(dao.getBook(bookId)!!.isAvailable)
    }

    @Test
    fun invalidTransitions_doNotMutateBookingOrBook() = runTest {
        val bookId = dao.insertBook(BookEntity(title = "Pending", author = "Author", category = "Test"))
        val now = System.currentTimeMillis()
        dao.reserveBook(
            BookingEntity(
                bookOwnerId = bookId,
                userName = "Student",
                bookingDate = now,
                returnDeadline = now + TimeUnit.DAYS.toMillis(7),
                status = BookingStatus.PENDING,
            ),
        )
        val bookingId = dao.observeOpenBookings().first().single().bookingId

        assertEquals(TransactionResult.INVALID_STATE, dao.renewBooking(bookingId, now))
        assertEquals(TransactionResult.INVALID_STATE, dao.returnBooking(bookingId))
        assertEquals(BookingStatus.PENDING, dao.getBooking(bookingId)!!.status)
        assertFalse(dao.getBook(bookId)!!.isAvailable)
    }

    @Test
    fun search_isCaseInsensitiveAndSupportsAvailabilityFilter() = runTest {
        dao.insertBooks(
            listOf(
                BookEntity(title = "Kotlin in Action", author = "Dmitry Jemerov", category = "Programming"),
                BookEntity(title = "Clean Code", author = "Robert Martin", category = "Engineering", isAvailable = false),
            ),
        )

        assertEquals("Kotlin in Action", dao.observeBooks("kOtLiN", null).first().single().title)
        assertEquals("Clean Code", dao.observeBooks("martin", false).first().single().title)
        assertTrue(dao.observeBooks("Kotlin", false).first().isEmpty())
    }

    @Test
    fun deletingBook_cascadesToBooking() = runTest {
        val bookId = dao.insertBook(BookEntity(title = "Cascade", author = "Author", category = "Test"))
        val book = dao.getBook(bookId)!!
        val now = System.currentTimeMillis()
        dao.reserveBook(
            BookingEntity(
                bookOwnerId = bookId,
                userName = "Student",
                bookingDate = now,
                returnDeadline = now + TimeUnit.DAYS.toMillis(7),
                status = BookingStatus.PENDING,
            ),
        )
        val bookingId = dao.observeOpenBookings().first().single().bookingId

        dao.deleteBook(book.copy(isAvailable = false))
        assertNull(dao.getBooking(bookingId))
    }
}
