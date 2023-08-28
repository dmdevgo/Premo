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

package me.dmdev.premo.sample.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import me.dmdev.premo.ExperimentalPremoApi
import me.dmdev.premo.navigation.back
import me.dmdev.premo.sample.CounterPm
import me.dmdev.premo.sample.ScreenBox
import me.dmdev.premo.sample.bind

@OptIn(ExperimentalPremoApi::class)
@Composable
fun CounterScreen(
    pm: CounterPm
) {
    ScreenBox(
        title = "Counter",
        backHandler = { pm.back() }
    ) {
        val state = pm.stateFlow.bind()
        Column {
            Spacer(Modifier.weight(0.5f))
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { pm.minus() },
                    enabled = state.minusEnabled
                ) {
                    Text(" - ")
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("Count: ${state.count}")
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = { pm.plus() },
                    enabled = state.plusEnabled
                ) {
                    Text(" + ")
                }
            }
            Spacer(Modifier.weight(0.5f))
        }
    }
}
