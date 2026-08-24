package com.example.kitabu.ui.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kitabu.R
import com.example.kitabu.domain.LibraryTime
import com.example.kitabu.domain.model.AvailabilityFilter
import com.example.kitabu.domain.model.Book
import com.example.kitabu.domain.model.RentalDuration
import com.example.kitabu.ui.common.StatusBadge
import com.example.kitabu.ui.common.uiMessageStrings
import com.example.kitabu.ui.theme.AvailableDark
import com.example.kitabu.ui.theme.AvailableLight
import com.example.kitabu.ui.theme.BorrowedDark
import com.example.kitabu.ui.theme.BorrowedLight
import com.example.kitabu.ui.theme.Dimens
import java.time.Clock

@Composable
fun CatalogRoute(
    viewModel: CatalogViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val messages = uiMessageStrings()

    LaunchedEffect(viewModel, messages) {
        viewModel.events.collect { message ->
            snackbarHostState.showSnackbar(messages.getValue(message))
        }
    }

    CatalogScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onQueryChange = viewModel::updateQuery,
        onFilterChange = viewModel::updateFilter,
        onBookSelected = viewModel::openReservation,
        onSheetDismiss = viewModel::closeReservation,
        onUserNameChange = viewModel::updateUserName,
        onDurationChange = viewModel::updateDuration,
        onReserve = viewModel::reserveSelectedBook,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    state: CatalogUiState,
    snackbarHostState: SnackbarHostState,
    onQueryChange: (String) -> Unit,
    onFilterChange: (AvailabilityFilter) -> Unit,
    onBookSelected: (Book) -> Unit,
    onSheetDismiss: () -> Unit,
    onUserNameChange: (String) -> Unit,
    onDurationChange: (RentalDuration) -> Unit,
    onReserve: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.catalog_title)) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Dimens.Space16),
        ) {
            CatalogSearch(
                query = state.query,
                selectedFilter = state.filter,
                onQueryChange = onQueryChange,
                onFilterChange = onFilterChange,
            )
            Spacer(Modifier.height(Dimens.Space12))
            when {
                state.isLoading -> LoadingContent()
                state.books.isEmpty() -> EmptyCatalogContent()
                else -> LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 168.dp),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.Space12),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Space12),
                    modifier = Modifier.fillMaxSize().testTag("catalog_grid"),
                ) {
                    items(state.books, key = Book::id) { book ->
                        BookCard(book = book, onClick = { onBookSelected(book) })
                    }
                }
            }
        }
    }

    state.form.selectedBook?.let { book ->
        ReservationSheet(
            book = book,
            state = state,
            onDismiss = onSheetDismiss,
            onUserNameChange = onUserNameChange,
            onDurationChange = onDurationChange,
            onReserve = onReserve,
        )
    }
}

@Composable
private fun CatalogSearch(
    query: String,
    selectedFilter: AvailabilityFilter,
    onQueryChange: (String) -> Unit,
    onFilterChange: (AvailabilityFilter) -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth().testTag("catalog_search"),
        label = { Text(stringResource(R.string.search_books)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.clear_search))
                }
            }
        } else null,
        singleLine = true,
    )
    Spacer(Modifier.height(Dimens.Space8))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.Space8),
    ) {
        AvailabilityFilter.entries.forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterChange(filter) },
                label = {
                    Text(
                        stringResource(
                            when (filter) {
                                AvailabilityFilter.ALL -> R.string.filter_all
                                AvailabilityFilter.AVAILABLE -> R.string.filter_available
                                AvailabilityFilter.BORROWED -> R.string.filter_borrowed
                            },
                        ),
                    )
                },
            )
        }
    }
}

@Composable
private fun BookCard(book: Book, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = book.isAvailable, onClick = onClick)
            .testTag("book_${book.id}"),
        shape = RoundedCornerShape(Dimens.CardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(Dimens.Space16)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(Dimens.Space12)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Book,
                    contentDescription = stringResource(R.string.book_cover_description, book.title),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Spacer(Modifier.height(Dimens.Space12))
            Text(
                book.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                stringResource(R.string.by_author, book.author),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(Dimens.Space8))
            Text(
                stringResource(R.string.category_label, book.category),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Dimens.Space12))
            StatusBadge(
                text = stringResource(if (book.isAvailable) R.string.available else R.string.borrowed),
                background = if (book.isAvailable) AvailableLight else BorrowedLight,
                foreground = if (book.isAvailable) AvailableDark else BorrowedDark,
            )
            if (book.isAvailable) {
                TextButton(onClick = onClick, modifier = Modifier.align(Alignment.End)) {
                    Text(stringResource(R.string.reserve))
                }
            } else {
                Spacer(Modifier.height(Dimens.TouchTarget))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReservationSheet(
    book: Book,
    state: CatalogUiState,
    onDismiss: () -> Unit,
    onUserNameChange: (String) -> Unit,
    onDurationChange: (RentalDuration) -> Unit,
    onReserve: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets.safeDrawing },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.Space24),
        ) {
            Text(
                stringResource(R.string.reserve_book, book.title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(stringResource(R.string.by_author, book.author), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(Dimens.Space16))
            OutlinedTextField(
                value = state.form.userName,
                onValueChange = onUserNameChange,
                modifier = Modifier.fillMaxWidth().testTag("student_name"),
                label = { Text(stringResource(R.string.student_name)) },
                supportingText = {
                    when {
                        state.isNameBlank -> Text(stringResource(R.string.name_required))
                        state.isNameTooLong -> Text(stringResource(R.string.name_too_long))
                    }
                },
                isError = state.isNameBlank || state.isNameTooLong,
                singleLine = true,
                enabled = !state.form.isSaving,
            )
            Spacer(Modifier.height(Dimens.Space12))
            Text(stringResource(R.string.rental_duration), style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.Space8),
            ) {
                RentalDuration.entries.forEach { duration ->
                    FilterChip(
                        selected = state.form.duration == duration,
                        onClick = { onDurationChange(duration) },
                        label = { Text(pluralStringResource(R.plurals.duration_days, duration.days, duration.days)) },
                        enabled = !state.form.isSaving,
                    )
                }
            }
            val deadline = LibraryTime.deadlineFrom(Clock.systemDefaultZone(), state.form.duration.days)
            Text(
                stringResource(R.string.return_deadline_preview, LibraryTime.formatDate(deadline)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Dimens.Space16))
            Button(
                onClick = onReserve,
                enabled = state.canReserve,
                modifier = Modifier.fillMaxWidth().height(Dimens.TouchTarget).testTag("confirm_reservation"),
            ) {
                if (state.form.isSaving) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(Dimens.Space8))
                }
                Text(stringResource(R.string.reserve))
            }
            Spacer(Modifier.height(Dimens.Space32))
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.height(Dimens.Space12))
            Text(stringResource(R.string.loading))
        }
    }
}

@Composable
private fun EmptyCatalogContent() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(stringResource(R.string.no_books_found), style = MaterialTheme.typography.bodyLarge)
    }
}
