package com.example.kitabu.ui.common

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.kitabu.R
import com.example.kitabu.domain.model.LibraryActionResult

enum class UiMessage(@param:StringRes val textRes: Int) {
    RESERVATION_CREATED(R.string.reservation_created),
    BOOKING_PICKED_UP(R.string.booking_picked_up),
    BOOKING_RENEWED(R.string.booking_renewed),
    BOOKING_RETURNED(R.string.booking_returned),
    BOOKING_CANCELLED(R.string.booking_cancelled),
    BOOK_UNAVAILABLE(R.string.book_unavailable),
    RECORD_NOT_FOUND(R.string.record_not_found),
    INVALID_TRANSITION(R.string.invalid_transition),
    VALIDATION_FAILED(R.string.validation_failed),
    ACTION_FAILED(R.string.action_failed),
}

fun LibraryActionResult.toErrorMessage(): UiMessage = when (this) {
    LibraryActionResult.UNAVAILABLE -> UiMessage.BOOK_UNAVAILABLE
    LibraryActionResult.NOT_FOUND -> UiMessage.RECORD_NOT_FOUND
    LibraryActionResult.INVALID_STATE -> UiMessage.INVALID_TRANSITION
    LibraryActionResult.VALIDATION_ERROR -> UiMessage.VALIDATION_FAILED
    LibraryActionResult.FAILURE,
    LibraryActionResult.SUCCESS,
    -> UiMessage.ACTION_FAILED
}

@Composable
fun uiMessageStrings(): Map<UiMessage, String> =
    UiMessage.entries.associateWith { message -> stringResource(message.textRes) }
