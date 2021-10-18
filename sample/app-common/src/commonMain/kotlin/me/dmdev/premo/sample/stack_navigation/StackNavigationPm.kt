/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020-2021 Dmitriy Gorbunov (dmitriy.goto@gmail.com)
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

package me.dmdev.premo.sample.stack_navigation

import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import me.dmdev.premo.PmDescription
import me.dmdev.premo.PmParams
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.handle
import me.dmdev.premo.navigation.StackNavigation
import me.dmdev.premo.navigation.StackNavigator
import me.dmdev.premo.sample.StateFlow
import me.dmdev.premo.sample.SystemBackMessage

class StackNavigationPm(params: PmParams) : PresentationModel(params) {

    @Serializable
    object Description: PmDescription

    private val navigator = StackNavigator()
    val navigation: StackNavigation = navigator

    val backstackAsStringState = StateFlow("") {
        navigation.backstackState
            .map { backstack ->
                backstack.joinToString(
                    separator = " - ",
                    prefix = "[ ",
                    postfix = " ]"
                ) { it.tag }
            }
    }

    init {
        messageHandler.handle<SystemBackMessage> {
            navigator.pop()
        }
    }

    fun pushClick() {
        navigator.push(nextChild())
    }

    fun popClick() {
        navigator.pop()
    }

    fun setBackstackClick() {
        navigator.setBackStack(
            listOf(
                nextChild(),
                nextChild(),
                nextChild(),
            )
        )
    }

    private var screenNumber: Int = 0
    private fun nextChild(): PresentationModel {
        val number = screenNumber++
        return Child(
            description = SimpleScreenPm.Description(number),
            tag = number.toString()
        )
    }
}