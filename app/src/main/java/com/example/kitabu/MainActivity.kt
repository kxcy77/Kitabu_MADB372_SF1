package com.example.kitabu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import com.example.kitabu.ui.KitabuViewModelFactory
import com.example.kitabu.ui.navigation.KitabuApp
import com.example.kitabu.ui.theme.KitabuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val application = application as KitabuApplication
            val factory = remember(application) {
                KitabuViewModelFactory(
                    repository = application.container.libraryRepository,
                    defaultUserName = getString(R.string.default_student_name),
                )
            }
            KitabuTheme {
                KitabuApp(factory)
            }
        }
    }
}
