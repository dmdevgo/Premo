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

package me.dmdev.premo.sample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.StateFlow
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.sample.stack_navigation.SimpleScreenPm
import me.dmdev.premo.sample.stack_navigation.StackNavigationPm
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H4
import org.jetbrains.compose.web.dom.Text

@Composable
fun <T> StateFlow<T>.bind(): T {
    return collectAsState().value
}

@Composable
fun TextButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        attrs = {
            onClick { onClick() }
            style {
                padding(4.px)
                margin(4.px)
            }
            contentEditable(false)
            if (!enabled) disabled()
        }
    ) { Text(text) }
}

@Composable
fun TextBox(text: String) {
    Div(
        {
            style {
                padding(16.px)
            }
        }
    ) {
        Text(text)
    }
}

@Composable
fun ScreenBox(
    title: String,
    content: @Composable () -> Unit
) {
    Div({
        style {
            padding(16.px)
            margin(16.px)
            width(200.px)
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            alignItems(AlignItems.Center)
        }
    }) {
        H4 {
            Text(title)
        }
        content()
    }
}

@Composable
fun PresentationModel?.mapToComposable() {
    when (this) {
        is MainPm -> MainScreen(this)
        is SamplesPm -> SamplesScreen(this)
        is CounterPm -> CounterScreen(this)
        is StackNavigationPm -> StackNavigationScreen(this)
        is SimpleScreenPm -> SimpleScreen(this)
        else -> EmptyScreen()
    }
}