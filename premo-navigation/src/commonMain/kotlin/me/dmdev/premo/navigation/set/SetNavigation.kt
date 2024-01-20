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

package me.dmdev.premo.navigation.set

import kotlinx.coroutines.flow.StateFlow
import me.dmdev.premo.PresentationModel

interface SetNavigation {
    val valuesFlow: StateFlow<List<PresentationModel>>
    val values: List<PresentationModel> get() = valuesFlow.value
    val currentFlow: StateFlow<PresentationModel?>
    val current: PresentationModel? get() = currentFlow.value
    fun onChangeCurrent(index: Int)
    fun onChangeCurrent(pm: PresentationModel)
}

fun PresentationModel.SetNavigation(
    initValues: () -> List<PresentationModel>,
    key: String = DEFAULT_SET_NAVIGATOR_KEY,
    backHandler: (SetNavigator) -> Boolean = DEFAULT_SET_NAVIGATOR_BACK_HANDLER,
    onChangeCurrent: (index: Int, navigator: SetNavigator) -> Unit = DEFAULT_SET_NAVIGATOR_ON_CHANGE_CURRENT
): SetNavigation {
    return SetNavigator(
        initValues = initValues,
        key = key,
        backHandler = backHandler,
        onChangeCurrent = onChangeCurrent
    )
}
