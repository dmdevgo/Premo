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

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.*

typealias DialogComposableReference = (@Composable () -> Unit)?

val LocalDialogState = compositionLocalOf { mutableStateOf<DialogComposableReference>(null) }

@Composable
expect fun DialogScreen(
    title: String,
    message: String,
    okButtonText: String,
    cancelButtonText: String,
    onOkButtonClick: () -> Unit,
    onCancelButtonClick: () -> Unit,
    onDismissRequest: () -> Unit
)

@Composable
fun DialogContainer(
    isVisible: Boolean,
    content: @Composable () -> Unit
) {
    LocalDialogState.current.value = if (isVisible) {
        content
    } else {
        null
    }
}

@Composable
fun CommonDialog(
    title: String,
    message: String,
    okButtonText: String,
    cancelButtonText: String,
    onOkButtonClick: () -> Unit,
    onCancelButtonClick: () -> Unit,
    onDismissRequest: () -> Unit
) {
    val outerBoxBackground = Color.Gray.copy(alpha = 0.5f)
    Box(
        modifier = Modifier
            .background(outerBoxBackground)
            .fillMaxSize()
            .clickable(
                interactionSource = MutableInteractionSource(),
                indication = null
            ) {
                onDismissRequest()
            },
        contentAlignment = Alignment.Center
    ) {
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 24.dp)
        ) {
            Column(
                modifier = Modifier.widthIn(max = 280.dp)
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Text(title)
                Spacer(modifier = Modifier.height(16.dp))
                Text(message)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (cancelButtonText.isNotEmpty()) {
                        Button(
                            onClick = {
                                onCancelButtonClick()
                            }
                        ) {
                            Text(cancelButtonText)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    if (okButtonText.isNotEmpty()) {
                        Button(
                            onClick = {
                                onOkButtonClick()
                            }
                        ) {
                            Text(okButtonText)
                        }
                    }
                }
            }
        }
    }
}