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

package me.dmdev.premo.sample.bottomnavigation

import kotlinx.serialization.Serializable
import me.dmdev.premo.PmArgs
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.childrenOf
import me.dmdev.premo.navigation.StackNavigation
import me.dmdev.premo.navigation.StackNavigator
import me.dmdev.premo.navigation.handleBack
import me.dmdev.premo.navigation.push
import me.dmdev.premo.onMessage
import me.dmdev.premo.sample.NextClickMessage
import me.dmdev.premo.sample.PreviousClickMessage

class TabPm(
    args: Args
) : PresentationModel(args) {

    val title: String = args.tabTitle

    @Serializable
    data class Args(
        val tabTitle: String
    ) : PmArgs() {
        override val key: String get() = tabTitle
    }

    private var number: Int = 1

    val navigation = StackNavigation(
        initBackStack = {
            childrenOf(
                TabItemPm.Args(nextScreenTitle(), args.tabTitle)
            )
        },
        backHandler = { navigator -> handleBack(navigator) }
    ) { navigator ->
        onMessage<NextClickMessage> {
            navigator.push(
                Child(
                    TabItemPm.Args(
                        screenTitle = nextScreenTitle(),
                        tabTitle = args.tabTitle
                    )
                )
            )
        }
        onMessage<PreviousClickMessage> {
            handleBack(navigator)
        }
    }

    private fun handleBack(navigator: StackNavigator): Boolean {
        return if (navigator.handleBack()) {
            number--
            true
        } else {
            false
        }
    }

    private fun nextScreenTitle(): String {
        return "Screen #${number++}"
    }
}
