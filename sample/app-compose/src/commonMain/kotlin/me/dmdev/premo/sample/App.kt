package me.dmdev.premo.sample

import androidx.compose.material.*
import androidx.compose.runtime.*
import me.dmdev.premo.sample.screen.MainScreen

@Composable
fun App(
    pm: MainPm,
    windowSizes: WindowSizes
) {
    LocalWindowSizes.current.value = windowSizes
    MaterialTheme {
        FullScreenContainer {
            MainScreen(pm)
        }
    }
}
