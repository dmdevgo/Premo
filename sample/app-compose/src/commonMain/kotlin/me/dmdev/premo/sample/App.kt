package me.dmdev.premo.sample

import androidx.compose.material.*
import androidx.compose.runtime.*

@Composable
fun App(
    pm: MainPm,
    windowSizes: WindowSizes
) {
    MaterialTheme {
        FullScreenContainer {
            MainScreen(pm, windowSizes)
        }
    }
}
