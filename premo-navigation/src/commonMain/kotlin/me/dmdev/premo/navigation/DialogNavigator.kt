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

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.dmdev.premo.AttachedChild
import me.dmdev.premo.PmMessage
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.SaveableFlow
import me.dmdev.premo.attachToParent
import me.dmdev.premo.detachFromParent
import kotlin.reflect.KClass

interface DialogNavigator<D : PresentationModel, R> : DialogNavigation<D> {
    fun show(pm: D)
    suspend fun showForResult(pm: D): R?
    fun sendResult(result: R)
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

@Suppress("FunctionName")
inline fun <D : PresentationModel, reified R : PmMessage> PresentationModel.DialogNavigator(
    key: String,
    noinline onDismissRequest: (navigator: DialogNavigator<D, R>) -> Unit = { navigator ->
        navigator.dismiss()
    }
): DialogNavigator<D, R> {
    return DialogNavigator(
        key = key,
        messageClass = R::class,
        onDismissRequest = onDismissRequest
    )
}

@Suppress("FunctionName")
fun <D : PresentationModel, R : PmMessage> PresentationModel.DialogNavigator(
    key: String,
    messageClass: KClass<R>,
    onDismissRequest: (navigator: DialogNavigator<D, R>) -> Unit
): DialogNavigator<D, R> {
    return DialogNavigatorImpl(
        hostPm = this,
        onDismissRequest = onDismissRequest,
        key = key,
        messageClass = messageClass
    )
}

internal class DialogNavigatorImpl<D : PresentationModel, R : PmMessage>(
    private val hostPm: PresentationModel,
    private val onDismissRequest: (navigator: DialogNavigator<D, R>) -> Unit,
    private val messageClass: KClass<R>,
    key: String
) : DialogNavigator<D, R> {

    private val _dialog: MutableStateFlow<D?> = hostPm.SaveableFlow(
        key = "${key}_dialog",
        initialValueProvider = { null },
        saveTypeMapper = { it?.description },
        restoreTypeMapper = { it?.let { hostPm.AttachedChild(it) as D } }
    )
    override val dialogFlow: StateFlow<D?> = _dialog.asStateFlow()

    private val resultChannel = Channel<R?>()

    init {
        subscribeToMessages()
    }

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
        _dialog.value?.detachFromParent()
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

    private fun subscribeToMessages() {
        hostPm.scope.launch {
            dialogFlow.collect {
                it?.messageHandler?.addHandler { message ->
                    if (messageClass.isInstance(message)) {
                        @Suppress("UNCHECKED_CAST")
                        sendResult(message as R)
                        true
                    } else {
                        false
                    }
                }
            }
        }
    }
}
