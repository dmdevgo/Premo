package me.dmdev.premo.sample

import androidx.compose.material.*
import androidx.compose.runtime.*
import me.dmdev.premo.PmDelegate

@Composable
fun App(
    delegate: PmDelegate<MainPm> = PremoSample.createPmDelegate().apply {
        onCreate()
        onForeground()
    },
    windowSizes: WindowSizes = WindowSizes(WindowSizeClass.Expanded, WindowSizeClass.Expanded)
) {
    MaterialTheme {
        FullScreenContainer {
            MainScreen(delegate.presentationModel, windowSizes)
        }
    }
}
