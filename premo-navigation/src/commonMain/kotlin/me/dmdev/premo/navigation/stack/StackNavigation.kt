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

package me.dmdev.premo.navigation.stack

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import me.dmdev.premo.PmMessageHandler
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.annotation.ExperimentalPremoApi

interface StackNavigation {
    val currentTopFlow: StateFlow<PresentationModel?>
    val currentTop: PresentationModel? get() = backStack.lastOrNull()
    val backStackFlow: StateFlow<List<PresentationModel>>
    val backStack: List<PresentationModel> get() = backStackFlow.value

    @ExperimentalPremoApi
    val backStackChangesFlow: Flow<BackStackChange>
}

fun PresentationModel.StackNavigation(
    initBackStack: () -> List<PresentationModel>,
    key: String = DEFAULT_STACK_NAVIGATOR_KEY,
    backHandler: (StackNavigator) -> Boolean = DEFAULT_STACK_NAVIGATOR_BACK_HANDLER,
    initHandlers: PmMessageHandler.(navigator: StackNavigator) -> Unit = {}
): StackNavigation {
    return StackNavigator(
        initBackStack = initBackStack,
        key = key,
        backHandler = backHandler,
        initHandlers = initHandlers
    )
}
