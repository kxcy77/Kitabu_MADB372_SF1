package com.example.kitabu.ui.reservations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kitabu.domain.model.BookingSummary
import com.example.kitabu.domain.model.LibraryActionResult
import com.example.kitabu.domain.repository.LibraryRepository
import com.example.kitabu.ui.common.UiMessage
import com.example.kitabu.ui.common.toErrorMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ReservationsUiState(
    val bookings: List<BookingSummary> = emptyList(),
    val busyBookingIds: Set<Long> = emptySet(),
    val isLoading: Boolean = true,
)

class ReservationsViewModel(
    private val repository: LibraryRepository,
) : ViewModel() {
    private val isLoading = MutableStateFlow(true)
    private val busyIds = MutableStateFlow<Set<Long>>(emptySet())
    private val eventsChannel = Channel<UiMessage>(Channel.BUFFERED)
    val events = eventsChannel.receiveAsFlow()

    val uiState: StateFlow<ReservationsUiState> = combine(
        repository.observeOpenBookings().onEach { isLoading.value = false },
        busyIds,
        isLoading,
    ) { bookings, busyBookingIds, loading ->
        ReservationsUiState(bookings, busyBookingIds, loading)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReservationsUiState(),
    )

    fun pickUp(bookingId: Long) = perform(
        bookingId,
        UiMessage.BOOKING_PICKED_UP,
        repository::pickUpBooking,
    )

    fun renew(bookingId: Long) = perform(
        bookingId,
        UiMessage.BOOKING_RENEWED,
        repository::renewBooking,
    )

    fun returnBook(bookingId: Long) = perform(
        bookingId,
        UiMessage.BOOKING_RETURNED,
        repository::returnBooking,
    )

    fun cancel(bookingId: Long) = perform(
        bookingId,
        UiMessage.BOOKING_CANCELLED,
        repository::cancelBooking,
    )

    private fun perform(
        bookingId: Long,
        successMessage: UiMessage,
        action: suspend (Long) -> LibraryActionResult,
    ) {
        if (bookingId in busyIds.value) return
        viewModelScope.launch {
            busyIds.value = busyIds.value + bookingId
            val result = action(bookingId)
            busyIds.value = busyIds.value - bookingId
            eventsChannel.send(if (result == LibraryActionResult.SUCCESS) successMessage else result.toErrorMessage())
        }
    }
}
