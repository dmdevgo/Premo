/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020-2023 Dmitriy Gorbunov (dmitriy.goto@gmail.com)
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

package me.dmdev.premo.sample

import androidx.compose.runtime.*
import androidx.compose.ui.unit.*

enum class WindowSizeClass { Compact, Medium, Expanded }

data class WindowSizes(
    val widthSizeClass: WindowSizeClass,
    val heightSizeClass: WindowSizeClass
)

val LocalWindowSizes = staticCompositionLocalOf {
    mutableStateOf(WindowSizes(WindowSizeClass.Compact, WindowSizeClass.Compact))
}

fun windowSizes(width: Dp, height: Dp): WindowSizes {
    val widthWindowSizeClass = when {
        width < 600.dp -> WindowSizeClass.Compact
        width < 840.dp -> WindowSizeClass.Medium
        else -> WindowSizeClass.Expanded
    }

    val heightWindowSizeClass = when {
        height < 480.dp -> WindowSizeClass.Compact
        height < 900.dp -> WindowSizeClass.Medium
        else -> WindowSizeClass.Expanded
    }

    return WindowSizes(widthWindowSizeClass, heightWindowSizeClass)
}
