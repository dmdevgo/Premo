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
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.navigation.dialog.DialogGroupNavigation
import me.dmdev.premo.navigation.dialog.DialogGroupNavigationHost

class DialogNavigationPm(
    args: Args
) : PresentationModel(args), DialogGroupNavigationHost {

    @Serializable
    object Args : PmArgs() {
        override val key: String get() = "dialog_navigation"
    }

    private val alertDialog = AlertDialogNavigator<DialogContextData.Simple>(
        key = "simple_dialog"
    )

    private val alertDialogForResult = AlertDialogNavigator<DialogContextData.ForResult>(
        key = "simple_dialog_for_result",
        onOk = {
            showResult("Ok")
        },
        onCancel = {
            showResult("Cancel")
        }
    )

    override val dialogGroupNavigation = DialogGroupNavigation(
        alertDialog,
        alertDialogForResult
    )

    fun onSimpleDialogClick() {
        alertDialog.show(
            Child(
                AlertDialogPm.Args(
                    title = "Simple dialog",
                    message = "This is a simple dialog, click ok to close.",
                    okButtonText = "Ok",
                    cancelButtonText = "",
                    contextData = DialogContextData.Simple
                )
            )
        )
    }

    fun onSimpleDialogForResultClick() {
        alertDialogForResult.show(
            Child(
                AlertDialogPm.Args(
                    title = "Simple result dialog",
                    message = "This is a simple dialog that sends a result message to show which button is clicked.",
                    okButtonText = "Ok",
                    cancelButtonText = "Cancel",
                    contextData = DialogContextData.ForResult
                )
            )
        )
    }

    private fun showResult(resultMessage: String) {
        alertDialog.show(
            Child(
                AlertDialogPm.Args(
                    title = "Dialog Result",
                    message = resultMessage,
                    okButtonText = "Close",
                    cancelButtonText = "",
                    contextData = DialogContextData.Simple
                )
            )
        )
    }
}
