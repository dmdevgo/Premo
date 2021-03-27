/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020-2021 Dmitriy Gorbunov (dmitriy.goto@gmail.com)
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

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.dmdev.premo.invoke
import me.dmdev.premo.value

@Composable
fun dialogScreen(pm: DialogPm) {
    MaterialTheme {
        Scaffold(
            snackbarHost = {
                val result = pm.alertResult.bind()

                if (result.isNotEmpty()) {
                    Snackbar(
                        action = {
                            Button(onClick = { pm.hideResult() }) {
                                Text("Hide")
                            }
                        },
                        modifier = Modifier.padding(8.dp)
                    ) { Text(text = "Dialog result: $result") }
                }
            }
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = { pm.showDialog() }) {
                    Text("Show simple dialog")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { pm.showDialogForResult() }) {
                    Text("Show dialog for result")
                }

                if (pm.alertPm.isShown.bind()) {
                    alert(pm.alertPm)
                }
            }
        }
    }
}

@Composable
fun alert(pm: AlertPm) {
    AlertDialog(
        onDismissRequest = { pm.dismiss() },
        text = { Text(pm.message.value) },
        confirmButton = {
            Button(onClick = { pm.okClick() }) {
                Text("Ok")
            }
        },
        dismissButton = {
            Button(onClick = { pm.cancelClick() }) {
                Text("Cancel")
            }
        }
    )
}