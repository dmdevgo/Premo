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

package me.dmdev.premo.sample

import me.dmdev.premo.sample.dilaognavigation.DialogNavigationPm
import mui.material.Alert
import mui.material.AlertColor
import mui.material.AlertVariant
import mui.material.Button
import mui.material.ButtonVariant
import mui.material.Dialog
import mui.material.DialogActions
import mui.material.DialogContent
import mui.material.DialogTitle
import mui.material.Stack
import mui.material.StackDirection
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import web.cssom.AlignItems
import web.cssom.JustifyContent

external interface DialogNavigationComponentProps : Props {
    var pm: DialogNavigationPm
}

val DialogNavigationComponent = FC<DialogNavigationComponentProps> { props ->

    val pm = props.pm
    val message = pm.messagesFlow.collectAsState("")
    val dialogPm = pm.dialogNavigation.dialog.collectAsState()

    Stack {

        direction = responsive(StackDirection.column)
        spacing = responsive(2)

        sx {
            justifyContent = JustifyContent.center
            alignItems = AlignItems.center
        }

        Button {
            onClick = {
                pm.showSimpleDialogClick()
            }
            variant = ButtonVariant.contained
            +"Show simple dialog"
        }

        Button {
            onClick = {
                pm.showSimpleDialogForResultClick()
            }
            variant = ButtonVariant.contained
            +"Show dialog for result"
        }

        if (message.isNotEmpty()) {
            Alert {
                severity = AlertColor.info
                variant = AlertVariant.outlined
                +message
            }
        }

        if (dialogPm != null) {
            Dialog {

                open = true

                onClose = { _, _ ->
                    pm.dialogNavigation.onDismissRequest()
                }

                if (dialogPm.title.isNotEmpty()) {
                    DialogTitle {
                        +dialogPm.title
                    }
                }

                if (dialogPm.message.isNotEmpty()) {
                    DialogContent {
                        +dialogPm.message
                    }
                }

                DialogActions {
                    if (dialogPm.cancelButtonText.isNotEmpty()) {
                        Button {
                            onClick = {
                                dialogPm.onCancelClick()
                            }
                            variant = ButtonVariant.contained
                            +"Cancel"
                        }
                    }
                    if (dialogPm.okButtonText.isNotEmpty()) {
                        Button {
                            onClick = {
                                dialogPm.onOkClick()
                            }
                            variant = ButtonVariant.contained
                            +"Ok"
                        }
                    }
                }
            }
        }
    }
}
