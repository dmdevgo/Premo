/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020-2022 Dmitriy Gorbunov (dmitriy.goto@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import me.dmdev.premo.PmLifecycle
import me.dmdev.premo.PmParams
import me.dmdev.premo.sample.MainPm
import me.dmdev.premo.sample.MainPmFactory
import me.dmdev.premo.sample.MainScreen
import me.dmdev.premo.sample.WindowSizeClass
import me.dmdev.premo.sample.WindowSizes
import me.dmdev.premo.sample.serialization.SimpleJsonPmStateSaverFactory

val pm = MainPm(
    params = PmParams(
        tag = "main",
        description = MainPm.Description,
        parent = null,
        factory = MainPmFactory(),
        stateSaverFactory = SimpleJsonPmStateSaverFactory(),
    )
).also {
    it.lifecycle.moveTo(PmLifecycle.State.IN_FOREGROUND)
}

@Preview
fun main() = application {
    val state = rememberWindowState()
    val windowSizeClass = rememberWindowSizes(state)
    Window(
        onCloseRequest = ::exitApplication,
        title = "Premo",
        state = state
    ) {
        MaterialTheme {
            MainScreen(pm, windowSizeClass)
        }
    }
}

@Composable
fun rememberWindowSizes(state: WindowState): WindowSizes {

    val widthWindowSizeClass = when {
        state.size.width < 600.dp -> WindowSizeClass.Compact
        state.size.width < 840.dp -> WindowSizeClass.Medium
        else -> WindowSizeClass.Expanded
    }

    val heightWindowSizeClass = when {
        state.size.height < 480.dp -> WindowSizeClass.Compact
        state.size.height < 900.dp -> WindowSizeClass.Medium
        else -> WindowSizeClass.Expanded
    }

    return WindowSizes(widthWindowSizeClass, heightWindowSizeClass)
}