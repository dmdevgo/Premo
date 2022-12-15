/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020-2022 Dmitriy Gorbunov (dmitriy.goto@gmail.com)
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

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.dmdev.premo.*

interface DialogNavigator<D: PresentationModel, R> : DialogNavigation<D> {
    fun show(pm: D)
    suspend fun showForResult(pm: D): R?
    fun sendResult(result: R)
    fun dismiss()
}

inline fun <D : PresentationModel, reified R : PmMessage> PresentationModel.DialogNavigator(
    key: String = "dialog_navigator",
    noinline onDismissRequest: (navigator: DialogNavigator<D, R>) -> Unit = { navigator -> navigator.dismiss() },
): DialogNavigator<D, R> {

    val navigator = DialogNavigatorImpl(onDismissRequest)
    val savedDialogPm = stateHandler.getSaved<Pair<PmDescription, String>?>(key)
    if (savedDialogPm != null) {
        navigator.show(Child(savedDialogPm.first, savedDialogPm.second))
    }
    stateHandler.setSaver(key) {
        navigator.dialog.value?.let { detailPm ->
            Pair(detailPm.description, detailPm.tag)
        }
    }
    messageHandler.onMessage<R> { navigator.sendResult(it) }
    messageHandler.handle<BackMessage> { navigator.handleBack() }
    return navigator
}

class DialogNavigatorImpl<D: PresentationModel, R : PmMessage>(
    private val onDismissRequest: (navigator: DialogNavigator<D, R>) -> Unit
) : DialogNavigator<D, R> {

    private val _dialog: MutableStateFlow<D?> = MutableStateFlow(null)
    override val dialog: StateFlow<D?> = _dialog.asStateFlow()

    private val resultChannel = Channel<R?>()

    override fun show(pm: D) {
        if (isShowing) dismiss()
        _dialog.value = pm
        pm.attachToParent()
    }

    override suspend fun showForResult(pm: D): R? {
        show(pm)
        return resultChannel.receive()
    }

    override fun sendResult(result: R) {
        _dialog.value = null
        resultChannel.trySend(result)
    }

    override fun dismiss() {
        _dialog.value?.detachFromParent()
        _dialog.value = null
        resultChannel.trySend(null)
    }

    override fun onDismissRequest() {
        onDismissRequest(this)
    }
}