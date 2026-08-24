package com.example.kitabu

import android.app.Application
import com.example.kitabu.di.AppContainer

class KitabuApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
