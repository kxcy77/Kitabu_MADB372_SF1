package com.example.kitabu.ui.catalog

import com.example.kitabu.MainDispatcherRule
import com.example.kitabu.domain.model.AvailabilityFilter
import com.example.kitabu.domain.model.Book
import com.example.kitabu.domain.model.BookingSummary
import com.example.kitabu.domain.model.LibraryActionResult
import com.example.kitabu.domain.repository.LibraryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `successful reservation closes sheet and emits success`() = runTest {
        val repository = FakeLibraryRepository()
        val viewModel = CatalogViewModel(repository, "Student User")
        val book = repository.books.value.single()
        val stateCollector = backgroundScope.async { viewModel.uiState.first { !it.isLoading } }
        advanceUntilIdle()
        stateCollector.await()

        val event = backgroundScope.async { viewModel.events.first() }
        viewModel.openReservation(book)
        viewModel.reserveSelectedBook()
        advanceUntilIdle()

        assertEquals(com.example.kitabu.ui.common.UiMessage.RESERVATION_CREATED, event.await())
        assertNull(viewModel.uiState.value.form.selectedBook)
        assertFalse(viewModel.uiState.value.form.isSaving)
    }

    @Test
    fun `query and availability filter are forwarded to repository`() = runTest {
        val repository = FakeLibraryRepository()
        val viewModel = CatalogViewModel(repository, "Student User")
        val collector = backgroundScope.async { viewModel.uiState.first { !it.isLoading } }
        advanceUntilIdle()
        collector.await()

        viewModel.updateQuery("Kotlin")
        viewModel.updateFilter(AvailabilityFilter.AVAILABLE)
        advanceUntilIdle()

        assertEquals("Kotlin", repository.lastQuery)
        assertEquals(AvailabilityFilter.AVAILABLE, repository.lastFilter)
    }
}

private class FakeLibraryRepository : LibraryRepository {
    val books = MutableStateFlow(
        listOf(Book(3, "Kotlin in Action", "Dmitry Jemerov", "Programming", true)),
    )
    var lastQuery: String = ""
    var lastFilter: AvailabilityFilter = AvailabilityFilter.ALL

    override fun observeBooks(query: String, filter: AvailabilityFilter): Flow<List<Book>> {
        lastQuery = query
        lastFilter = filter
        return books
    }

    override fun observeOpenBookings(): Flow<List<BookingSummary>> = MutableStateFlow(emptyList())
    override suspend fun reserveBook(bookId: Long, userName: String, durationDays: Int) = LibraryActionResult.SUCCESS
    override suspend fun pickUpBooking(bookingId: Long) = LibraryActionResult.SUCCESS
    override suspend fun renewBooking(bookingId: Long) = LibraryActionResult.SUCCESS
    override suspend fun returnBooking(bookingId: Long) = LibraryActionResult.SUCCESS
    override suspend fun cancelBooking(bookingId: Long) = LibraryActionResult.SUCCESS
}
