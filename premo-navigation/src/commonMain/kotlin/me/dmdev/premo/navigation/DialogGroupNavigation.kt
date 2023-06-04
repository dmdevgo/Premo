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

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.SaveableFlow
import me.dmdev.premo.handle

interface DialogGroupNavigation {
    val dialogsFlow: StateFlow<List<PresentationModel>>
    val dialogs: List<PresentationModel> get() = dialogsFlow.value
    fun onDismissRequest()
}

fun PresentationModel.DialogGroupNavigation(
    vararg dialogNavigators: DialogNavigator<*, *>,
    key: String = "dialog_group",
    backHandler: (DialogGroupNavigation) -> Boolean = { it.handleBack() }
): DialogGroupNavigation {
    val navigator = DialogGroupNavigatorImpl(
        hostPm = this,
        dialogNavigators = dialogNavigators.asList(),
        key = key
    )
    messageHandler.handle<BackMessage> { backHandler.invoke(navigator) }
    return navigator
}

fun DialogGroupNavigation.handleBack(): Boolean {
    return if (dialogs.isNotEmpty()) {
        onDismissRequest()
        true
    } else {
        false
    }
}

internal class DialogGroupNavigatorImpl(
    private val hostPm: PresentationModel,
    private val dialogNavigators: List<DialogNavigator<*, *>>,
    key: String
) : DialogGroupNavigation {

    private val _dialogsFlow: MutableStateFlow<List<PresentationModel>> =
        hostPm.SaveableFlow(
            key = "${key}_dialogs",
            initialValueProvider = { listOf() },
            saveTypeMapper = { dialogs -> dialogs.map { it.description } },
            restoreTypeMapper = { descriptions ->
                descriptions.mapNotNull { description ->
                    dialogNavigators.find { it.dialog?.description == description }?.dialog
                }
            }
        )

    override val dialogsFlow: StateFlow<List<PresentationModel>> =
        _dialogsFlow.asStateFlow()

    override fun onDismissRequest() {
        val topPm = _dialogsFlow.value.lastOrNull() ?: return
        dialogNavigators.find { it.dialogFlow.value === topPm }?.onDismissRequest()
    }

    init {
        dialogNavigators.forEach { dialogNavigator ->
            hostPm.scope.launch {
                dialogNavigator.dialogFlow.collect { pm ->
                    if (pm != null) {
                        _dialogsFlow.value = _dialogsFlow.value
                            .filter { it != pm }
                            .plus(pm)
                    } else {
                        _dialogsFlow.value =
                            _dialogsFlow.value.filter { it == dialogNavigator.dialog }
                    }
                }
            }
        }
    }
}
