/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020-2024 Dmitriy Gorbunov (dmitriy.goto@gmail.com)
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

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import me.dmdev.premo.annotation.ExperimentalPremoApi
import me.dmdev.premo.compose.navigation.AnimatedNavigationBox
import me.dmdev.premo.navigation.back
import me.dmdev.premo.sample.CardBox
import me.dmdev.premo.sample.EmptyBox
import me.dmdev.premo.sample.ScreenBox
import me.dmdev.premo.sample.bind
import me.dmdev.premo.sample.stacknavigation.SimpleScreenPm
import me.dmdev.premo.sample.stacknavigation.StackNavigationPm

@OptIn(ExperimentalPremoApi::class)
@Composable
fun StackNavigationScreen(
    pm: StackNavigationPm
) {
    ScreenBox(
        title = "Stack Navigation",
        backHandler = { pm.back() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(0.5f))
            AnimatedNavigationBox(
                navigation = pm.stackNavigation,
                modifier = Modifier.size(100.dp),
                enterTransition = { _, _ -> slideInHorizontally { height -> height } },
                exitTransition = { _, _ -> slideOutHorizontally { height -> -height } },
                popEnterTransition = { _, _ -> slideInHorizontally { height -> -height } },
                popExitTransition = { _, _ -> slideOutHorizontally { height -> height } }
            ) { pm ->
                when (pm) {
                    is SimpleScreenPm -> SimpleScreen(pm)
                    else -> EmptyBox()
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Stack: ${pm.backstackAsStringState.bind()}",
                modifier = Modifier.padding(16.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Row {
                Button(onClick = { pm.pushClick() }) {
                    Text("Push")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = { pm.popClick() }) {
                    Text("Pop")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = { pm.popToRootClick() }) {
                    Text("Pop to root")
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Row {
                Button(onClick = { pm.replaceTopClick() }) {
                    Text("Replace top")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = { pm.replaceAllClick() }) {
                    Text("Replace all")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { pm.setBackstackClick() }) {
                Text("Set back stack")
            }
            Spacer(Modifier.weight(0.5f))
        }
    }
}

@Composable
fun SimpleScreen(pm: SimpleScreenPm) {
    CardBox {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#${pm.numberText}",
                fontSize = 24.sp
            )
        }
    }
}
