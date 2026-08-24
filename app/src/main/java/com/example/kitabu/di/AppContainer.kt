package com.example.kitabu.di

import android.content.Context
import com.example.kitabu.data.local.KitabuDatabase
import com.example.kitabu.data.repository.RoomLibraryRepository
import com.example.kitabu.domain.repository.LibraryRepository
import java.time.Clock

class AppContainer(context: Context) {
    private val database = KitabuDatabase.getInstance(context)
    val libraryRepository: LibraryRepository = RoomLibraryRepository(
        dao = database.libraryDao(),
        clock = Clock.systemDefaultZone(),
    )
}
