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

package me.dmdev.premo.navigation.dialog

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.dmdev.premo.AttachedChild
import me.dmdev.premo.PmMessage
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.SaveableFlow
import me.dmdev.premo.attachToParent
import me.dmdev.premo.destroy
import kotlin.reflect.KClass

interface DialogNavigator<PM, MESSAGE> : DialogNavigation<PM>
        where PM : PresentationModel,
              MESSAGE : PmMessage {
    fun show(pm: PM)
    fun dismiss()
}

fun DialogNavigator<*, *>.handleBack(): Boolean {
    return if (isShowing) {
        dismiss()
        true
    } else {
        false
    }
}

inline fun <PM, reified MESSAGE> PresentationModel.DialogNavigator(
    key: String,
    noinline onDismissRequest: (navigator: DialogNavigator<PM, MESSAGE>) -> Unit = { navigator ->
        navigator.handleBack()
    },
    noinline messageHandler: (message: MESSAGE) -> Unit = {}
): DialogNavigator<PM, MESSAGE>
        where PM : PresentationModel,
              MESSAGE : PmMessage {
    return DialogNavigator(
        key = key,
        messageClass = MESSAGE::class,
        onDismissRequest = onDismissRequest,
        messageHandler = messageHandler
    )
}

fun <PM, MESSAGE> PresentationModel.DialogNavigator(
    key: String,
    messageClass: KClass<MESSAGE>,
    onDismissRequest: (navigator: DialogNavigator<PM, MESSAGE>) -> Unit,
    messageHandler: (message: MESSAGE) -> Unit
): DialogNavigator<PM, MESSAGE>
        where PM : PresentationModel,
              MESSAGE : PmMessage {
    return DialogNavigatorImpl(
        hostPm = this,
        onDismissRequest = onDismissRequest,
        messageHandler = messageHandler,
        key = key,
        messageClass = messageClass
    )
}

internal class DialogNavigatorImpl<PM, MESSAGE>(
    private val hostPm: PresentationModel,
    private val onDismissRequest: (navigator: DialogNavigator<PM, MESSAGE>) -> Unit,
    private val messageHandler: (message: MESSAGE) -> Unit,
    private val messageClass: KClass<MESSAGE>,
    key: String
) : DialogNavigator<PM, MESSAGE>
        where PM : PresentationModel,
              MESSAGE : PmMessage {

    private val pmMessageHandler: (message: PmMessage) -> Boolean = { message ->
        if (messageClass.isInstance(message)) {
            @Suppress("UNCHECKED_CAST")
            handleResult(message as MESSAGE)
            true
        } else {
            false
        }
    }

    private val _dialog: MutableStateFlow<PM?> = hostPm.SaveableFlow(
        key = key,
        initialValueProvider = { null },
        saveTypeMapper = { it?.pmArgs },
        restoreTypeMapper = { args ->
            args?.let { hostPm.AttachedChild(it) as PM }
                ?.also { pm ->
                    pm.messageHandler.addHandler(pmMessageHandler)
                }
        }
    )

    override val dialogFlow: StateFlow<PM?> = _dialog.asStateFlow()

    override fun show(pm: PM) {
        if (isShowing) dismiss()
        pm.attachToParent()
        pm.messageHandler.addHandler(pmMessageHandler)
        _dialog.value = pm
    }

    override fun dismiss() {
        _dialog.value?.messageHandler?.removeHandler(pmMessageHandler)
        _dialog.value?.destroy()
        _dialog.value = null
    }

    override fun onDismissRequest() {
        onDismissRequest(this)
    }

    private fun handleResult(result: MESSAGE) {
        messageHandler(result)
        dismiss()
    }
}
