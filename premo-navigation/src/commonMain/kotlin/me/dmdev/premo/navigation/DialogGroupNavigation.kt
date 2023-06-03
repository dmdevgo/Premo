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

package me.dmdev.premo.navigation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.dmdev.premo.PresentationModel

interface DialogGroupNavigation {
    val dialogsFlow: StateFlow<List<PresentationModel>>
    fun onDismissRequest()
}

fun PresentationModel.DialogGroupNavigation(
    vararg dialogNavigators: DialogNavigator<*, *>
): DialogGroupNavigation {
    return DialogGroupNavigator(scope, dialogNavigators.asList())
}

class DialogGroupNavigator(
    scope: CoroutineScope,
    private val dialogNavigators: List<DialogNavigator<*, *>>
) : DialogGroupNavigation {

    private val _dialogsFlow: MutableStateFlow<List<PresentationModel>> =
        MutableStateFlow(listOf())
    override val dialogsFlow: StateFlow<List<PresentationModel>> =
        _dialogsFlow.asStateFlow()

    override fun onDismissRequest() {
        val topPm = _dialogsFlow.value.lastOrNull() ?: return
        dialogNavigators.find { it.dialogFlow.value === topPm }?.onDismissRequest()
    }

    init {
        dialogNavigators.forEach { dialogNavigator ->
            scope.launch {
                dialogNavigator.dialogFlow.collect { pm ->
                    if (pm != null) {
                        _dialogsFlow.value = _dialogsFlow.value
                            .filter { it != pm }
                            .plus(pm)
                    } else {
                        _dialogsFlow.value =
                            _dialogsFlow.value.filter { it == dialogNavigator.dialogFlow }
                    }
                }
            }
        }
    }
}
