package com.example.kitabu.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kitabu.domain.repository.LibraryRepository
import com.example.kitabu.ui.catalog.CatalogViewModel
import com.example.kitabu.ui.reservations.ReservationsViewModel

class KitabuViewModelFactory(
    private val repository: LibraryRepository,
    private val defaultUserName: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(CatalogViewModel::class.java) ->
            CatalogViewModel(repository, defaultUserName) as T
        modelClass.isAssignableFrom(ReservationsViewModel::class.java) ->
            ReservationsViewModel(repository) as T
        else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
