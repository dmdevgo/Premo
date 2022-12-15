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

package me.dmdev.premo.sample.dilaog_navigation

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import me.dmdev.premo.PmDescription
import me.dmdev.premo.PmParams
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.navigation.DialogNavigation
import me.dmdev.premo.navigation.DialogNavigator

class DialogNavigationPm(
    params: PmParams
) : PresentationModel(params) {

    @Serializable
    object Description : PmDescription

    private val _messagesFlow: MutableSharedFlow<String> = MutableSharedFlow(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val messagesFlow: SharedFlow<String> = _messagesFlow.asSharedFlow()

    private val dialogNavigator = DialogNavigator<SimpleDialogPm, SimpleDialogPm.ResultMessage>()
    val dialogNavigation: DialogNavigation<SimpleDialogPm> = dialogNavigator

    fun showSimpleDialogClick() {
        _messagesFlow.tryEmit("")
        dialogNavigator.show(
            Child(
                SimpleDialogPm.Description(
                    title = "Simple dialog",
                    message = "This is a simple dialog, click ok to close.",
                    okButtonText = "Ok",
                    cancelButtonText = ""
                )
            )
        )
    }

    fun showSimpleDialogForResultClick() {
        inForegroundScope?.launch {
            val result = dialogNavigator.showForResult(
                Child(
                    SimpleDialogPm.Description(
                        title = "Simple result dialog",
                        message = "This is a simple dialog that sends a result message to show which button is clicked.",
                        okButtonText = "Ok",
                        cancelButtonText = "Cancel"
                    )
                )
            )

            when (result) {
                SimpleDialogPm.Cancel -> _messagesFlow.emit("Cancel clicked")
                SimpleDialogPm.Ok -> _messagesFlow.emit("Ok clicked")
                null -> _messagesFlow.emit("Dialog dismissed")
            }
        }
    }
}
