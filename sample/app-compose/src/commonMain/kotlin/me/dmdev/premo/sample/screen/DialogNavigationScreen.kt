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

package me.dmdev.premo.sample.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import me.dmdev.premo.ExperimentalPremoApi
import me.dmdev.premo.navigation.back
import me.dmdev.premo.sample.ScreenBox
import me.dmdev.premo.sample.bind
import me.dmdev.premo.sample.dilaognavigation.DialogNavigationPm
import me.dmdev.premo.sample.dilaognavigation.SimpleDialogPm

@OptIn(ExperimentalPremoApi::class)
@Composable
fun DialogNavigationScreen(
    pm: DialogNavigationPm
) {
    ScreenBox(
        title = "Dialog Navigation",
        backHandler = { pm.back() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(0.5f))
            Button(onClick = { pm.showSimpleDialogClick() }) {
                Text("Show simple dialog")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { pm.showSimpleDialogForResultClick() }) {
                Text("Show dialog for result")
            }
            Spacer(Modifier.weight(0.5f))
        }

        pm.dialogGroupNavigation.dialogsFlow.bind().let { dialogs ->
            val dialogPm = dialogs.lastOrNull()
            if (dialogPm is SimpleDialogPm) {
                AlertDialog(
                    title = { Text(dialogPm.args.title) },
                    text = { Text(dialogPm.args.message) },
                    buttons = {
                        val okButtonText = dialogPm.args.okButtonText
                        val cancelButtonText = dialogPm.args.cancelButtonText

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (cancelButtonText.isNotEmpty()) {
                                Button(
                                    onClick = dialogPm::onCancelClick
                                ) {
                                    Text(cancelButtonText)
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            if (okButtonText.isNotEmpty()) {
                                Button(
                                    onClick = dialogPm::onOkClick
                                ) {
                                    Text(okButtonText)
                                }
                            }
                        }
                    },
                    onDismissRequest = pm.dialogGroupNavigation::onDismissRequest
                )
            }
        }
    }
}
