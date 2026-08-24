package com.example.kitabu.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.kitabu.R
import com.example.kitabu.ui.KitabuViewModelFactory
import com.example.kitabu.ui.catalog.CatalogRoute
import com.example.kitabu.ui.catalog.CatalogViewModel
import com.example.kitabu.ui.reservations.ReservationsRoute
import com.example.kitabu.ui.reservations.ReservationsViewModel

private enum class Destination(val route: String, val labelRes: Int) {
    CATALOG("catalog", R.string.catalog_nav),
    RESERVATIONS("reservations", R.string.reservations_nav),
}

@Composable
fun KitabuApp(factory: KitabuViewModelFactory) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val catalogViewModel: CatalogViewModel = viewModel(factory = factory)
    val reservationsViewModel: ReservationsViewModel = viewModel(factory = factory)

    Scaffold(
        bottomBar = {
            NavigationBar {
                Destination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (destination == Destination.CATALOG) Icons.Default.LocalLibrary else Icons.Default.Bookmarks,
                                contentDescription = stringResource(destination.labelRes),
                            )
                        },
                        label = { Text(stringResource(destination.labelRes)) },
                    )
                }
            }
        },
    ) { outerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.CATALOG.route,
            modifier = Modifier,
        ) {
            composable(Destination.CATALOG.route) {
                CatalogRoute(catalogViewModel, Modifier.padding(outerPadding))
            }
            composable(Destination.RESERVATIONS.route) {
                ReservationsRoute(reservationsViewModel, Modifier.padding(outerPadding))
            }
        }
    }
}
