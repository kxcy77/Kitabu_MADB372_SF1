package com.example.kitabu.ui.reservations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kitabu.R
import com.example.kitabu.domain.LibraryTime
import com.example.kitabu.domain.model.BookingState
import com.example.kitabu.domain.model.BookingSummary
import com.example.kitabu.domain.model.DueStatus
import com.example.kitabu.ui.common.StatusBadge
import com.example.kitabu.ui.common.uiMessageStrings
import com.example.kitabu.ui.theme.AvailableDark
import com.example.kitabu.ui.theme.AvailableLight
import com.example.kitabu.ui.theme.Dimens
import com.example.kitabu.ui.theme.PendingDark
import com.example.kitabu.ui.theme.PendingLight
import java.time.Clock

private enum class ConfirmationAction { RETURN, CANCEL }
private data class ConfirmationRequest(val bookingId: Long, val action: ConfirmationAction)

@Composable
fun ReservationsRoute(
    viewModel: ReservationsViewModel,
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

    ReservationsScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onPickUp = viewModel::pickUp,
        onRenew = viewModel::renew,
        onReturn = viewModel::returnBook,
        onCancel = viewModel::cancel,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationsScreen(
    state: ReservationsUiState,
    snackbarHostState: SnackbarHostState,
    onPickUp: (Long) -> Unit,
    onRenew: (Long) -> Unit,
    onReturn: (Long) -> Unit,
    onCancel: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var confirmation by remember { mutableStateOf<ConfirmationRequest?>(null) }
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.reservations_title)) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.bookings.isEmpty() -> EmptyReservations(Modifier.fillMaxSize().padding(padding))
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = Dimens.Space16).testTag("reservations_list"),
                verticalArrangement = Arrangement.spacedBy(Dimens.Space12),
            ) {
                item { Spacer(Modifier.height(Dimens.Space4)) }
                items(state.bookings, key = BookingSummary::id) { booking ->
                    BookingCard(
                        booking = booking,
                        isBusy = booking.id in state.busyBookingIds,
                        onPickUp = { onPickUp(booking.id) },
                        onRenew = { onRenew(booking.id) },
                        onReturn = { confirmation = ConfirmationRequest(booking.id, ConfirmationAction.RETURN) },
                        onCancel = { confirmation = ConfirmationRequest(booking.id, ConfirmationAction.CANCEL) },
                    )
                }
                item { Spacer(Modifier.height(Dimens.Space16)) }
            }
        }
    }

    confirmation?.let { request ->
        val isReturn = request.action == ConfirmationAction.RETURN
        AlertDialog(
            onDismissRequest = { confirmation = null },
            title = { Text(stringResource(if (isReturn) R.string.confirm_return_title else R.string.confirm_cancel_title)) },
            text = { Text(stringResource(if (isReturn) R.string.confirm_return_message else R.string.confirm_cancel_message)) },
            confirmButton = {
                Button(onClick = {
                    if (isReturn) onReturn(request.bookingId) else onCancel(request.bookingId)
                    confirmation = null
                }) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmation = null }) { Text(stringResource(R.string.dismiss)) }
            },
        )
    }
}

@Composable
private fun BookingCard(
    booking: BookingSummary,
    isBusy: Boolean,
    onPickUp: () -> Unit,
    onRenew: () -> Unit,
    onReturn: () -> Unit,
    onCancel: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("booking_${booking.id}"),
        shape = RoundedCornerShape(Dimens.CardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(Dimens.Space16)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(booking.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.by_author, booking.author), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatusBadge(
                    text = stringResource(if (booking.status == BookingState.PENDING) R.string.pending else R.string.active),
                    background = if (booking.status == BookingState.PENDING) PendingLight else AvailableLight,
                    foreground = if (booking.status == BookingState.PENDING) PendingDark else AvailableDark,
                )
            }
            Spacer(Modifier.height(Dimens.Space12))
            Text(stringResource(R.string.booking_date, LibraryTime.formatDate(booking.bookingDate)))
            Text(stringResource(R.string.return_deadline, LibraryTime.formatDate(booking.returnDeadline)))
            Text(
                dueStatusText(LibraryTime.dueStatus(booking.returnDeadline, Clock.systemDefaultZone())),
                style = MaterialTheme.typography.titleSmall,
                color = dueStatusColor(LibraryTime.dueStatus(booking.returnDeadline, Clock.systemDefaultZone())),
            )
            Spacer(Modifier.height(Dimens.Space12))
            if (isBusy) {
                Box(Modifier.fillMaxWidth().height(Dimens.TouchTarget), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (booking.status == BookingState.PENDING) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.Space8)) {
                    Button(onClick = onPickUp, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.pick_up)) }
                    OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.cancel_reservation)) }
                }
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.Space8)) {
                    OutlinedButton(onClick = onRenew, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.renew)) }
                    Button(onClick = onReturn, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.return_book)) }
                }
            }
        }
    }
}

@Composable
private fun dueStatusText(status: DueStatus): String = when (status) {
    is DueStatus.Remaining -> pluralStringResource(
        R.plurals.days_remaining,
        status.days.toInt(),
        status.days,
    )
    DueStatus.DueToday -> stringResource(R.string.due_today)
    is DueStatus.Overdue -> pluralStringResource(
        R.plurals.overdue_days,
        status.days.toInt(),
        status.days,
    )
}

@Composable
private fun dueStatusColor(status: DueStatus) = when (status) {
    is DueStatus.Overdue -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.primary
}

@Composable
private fun EmptyReservations(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.no_reservations), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(Dimens.Space8))
            Text(
                stringResource(R.string.no_reservations_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
