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

package me.dmdev.premo.sample.dialognavigation

import kotlinx.serialization.Serializable
import me.dmdev.premo.PmArgs
import me.dmdev.premo.PmMessage
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.navigation.dialog.DialogNavigator
import kotlin.random.Random
import kotlin.random.nextUInt

class AlertDialogPm<T : DialogContextData>(
    val args: Args<T>
) : PresentationModel(args) {

    @Serializable
    data class Args<T : DialogContextData>(
        val title: String,
        val message: String,
        val okButtonText: String,
        val cancelButtonText: String,
        val contextData: T
    ) : PmArgs() {
        override val key: String = "simple_dialog_${Random.nextUInt()}"
    }

    sealed class ResultMessage<T : DialogContextData>(val contextData: T) : PmMessage
    inner class Ok(contextData: T) : ResultMessage<T>(contextData)
    inner class Cancel(contextData: T) : ResultMessage<T>(contextData)

    fun onOkClick() {
        messageHandler.send(Ok(args.contextData))
    }

    fun onCancelClick() {
        messageHandler.send(Cancel(args.contextData))
    }
}

@Suppress("FunctionName")
inline fun <CONTEXT : DialogContextData> PresentationModel.AlertDialogNavigator(
    key: String,
    crossinline onOk: (CONTEXT) -> Unit = {},
    crossinline onCancel: (CONTEXT) -> Unit = {}
): DialogNavigator<AlertDialogPm<CONTEXT>, AlertDialogPm.ResultMessage<CONTEXT>> {
    return DialogNavigator<AlertDialogPm<CONTEXT>, AlertDialogPm.ResultMessage<CONTEXT>>(
        key = key,
        messageHandler = { message ->
            when (message) {
                is AlertDialogPm.Cancel -> onCancel(message.contextData)
                is AlertDialogPm.Ok -> onOk(message.contextData)
            }
        }
    )
}
