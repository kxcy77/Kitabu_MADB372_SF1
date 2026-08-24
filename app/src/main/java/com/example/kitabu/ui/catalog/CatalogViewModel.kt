package com.example.kitabu.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kitabu.domain.LibraryTime
import com.example.kitabu.domain.model.AvailabilityFilter
import com.example.kitabu.domain.model.Book
import com.example.kitabu.domain.model.LibraryActionResult
import com.example.kitabu.domain.model.RentalDuration
import com.example.kitabu.domain.repository.LibraryRepository
import com.example.kitabu.ui.common.UiMessage
import com.example.kitabu.ui.common.toErrorMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ReservationFormState(
    val selectedBook: Book? = null,
    val userName: String,
    val duration: RentalDuration = RentalDuration.FOURTEEN_DAYS,
    val isSaving: Boolean = false,
)

data class CatalogUiState(
    val books: List<Book> = emptyList(),
    val query: String = "",
    val filter: AvailabilityFilter = AvailabilityFilter.ALL,
    val form: ReservationFormState,
    val isLoading: Boolean = true,
) {
    val isNameBlank: Boolean get() = LibraryTime.normalizedUserName(form.userName).isEmpty()
    val isNameTooLong: Boolean get() = LibraryTime.normalizedUserName(form.userName).length > LibraryTime.MAX_USER_NAME_LENGTH
    val canReserve: Boolean get() = !isNameBlank && !isNameTooLong && !form.isSaving
}

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModel(
    private val repository: LibraryRepository,
    defaultUserName: String,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(AvailabilityFilter.ALL)
    private val form = MutableStateFlow(ReservationFormState(userName = defaultUserName))
    private val isLoading = MutableStateFlow(true)
    private val eventsChannel = Channel<UiMessage>(Channel.BUFFERED)
    val events = eventsChannel.receiveAsFlow()

    private val books = combine(query, filter) { currentQuery, currentFilter ->
        currentQuery to currentFilter
    }.flatMapLatest { (currentQuery, currentFilter) ->
        repository.observeBooks(currentQuery, currentFilter)
    }.onEach { isLoading.value = false }

    val uiState: StateFlow<CatalogUiState> = combine(
        books,
        query,
        filter,
        form,
        isLoading,
    ) { currentBooks, currentQuery, currentFilter, currentForm, loading ->
        CatalogUiState(currentBooks, currentQuery, currentFilter, currentForm, loading)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CatalogUiState(form = form.value),
    )

    fun updateQuery(value: String) {
        query.value = value
    }

    fun updateFilter(value: AvailabilityFilter) {
        filter.value = value
    }

    fun openReservation(book: Book) {
        if (book.isAvailable) form.value = form.value.copy(selectedBook = book)
    }

    fun closeReservation() {
        if (!form.value.isSaving) form.value = form.value.copy(selectedBook = null)
    }

    fun updateUserName(value: String) {
        if (value.length <= LibraryTime.MAX_USER_NAME_LENGTH + 10) {
            form.value = form.value.copy(userName = value)
        }
    }

    fun updateDuration(value: RentalDuration) {
        form.value = form.value.copy(duration = value)
    }

    fun reserveSelectedBook() {
        val selected = form.value.selectedBook ?: return
        if (!LibraryTime.isValidUserName(form.value.userName) || form.value.isSaving) return
        viewModelScope.launch {
            form.value = form.value.copy(isSaving = true)
            val result = repository.reserveBook(selected.id, form.value.userName, form.value.duration.days)
            form.value = form.value.copy(
                selectedBook = if (result == LibraryActionResult.SUCCESS) null else selected,
                isSaving = false,
            )
            eventsChannel.send(
                if (result == LibraryActionResult.SUCCESS) UiMessage.RESERVATION_CREATED else result.toErrorMessage(),
            )
        }
    }
}
