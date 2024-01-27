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

import androidx.compose.runtime.*
import me.dmdev.premo.sample.ScreenBox
import me.dmdev.premo.sample.TextBox
import me.dmdev.premo.sample.TextButton
import me.dmdev.premo.sample.bind
import me.dmdev.premo.sample.stacknavigation.SimpleScreenPm
import me.dmdev.premo.sample.stacknavigation.StackNavigationPm
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Composable
fun StackNavigationScreen(pm: StackNavigationPm) {
    val backStack = pm.backstackAsStringState.bind()
    val currentPm = pm.stackNavigation.currentTopFlow.bind()
    ScreenBox("Stack Navigation") {
        Div(
            {
                style {
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Column)
                    alignItems(AlignItems.Center)
                }
            }

        ) {
            when (currentPm) {
                is SimpleScreenPm -> SimpleScreen(currentPm)
                else -> TextBox("Empty back stack.")
            }
            TextBox("Stack: $backStack")

            Div(
                {
                    style {
                        display(DisplayStyle.Flex)
                        flexDirection(FlexDirection.Row)
                        alignItems(AlignItems.Center)
                    }
                }

            ) {
                TextButton("Push") {
                    pm.pushClick()
                }
                TextButton("Pop") {
                    pm.popClick()
                }
                TextButton("Pop to root") {
                    pm.popToRootClick()
                }
            }

            Div(
                {
                    style {
                        display(DisplayStyle.Flex)
                        flexDirection(FlexDirection.Row)
                        alignItems(AlignItems.Center)
                    }
                }

            ) {
                TextButton("Replace top") {
                    pm.replaceTopClick()
                }
                TextButton("Replace all") {
                    pm.replaceAllClick()
                }
            }

            TextButton("Set back stack") {
                pm.setBackstackClick()
            }
        }
    }
}
