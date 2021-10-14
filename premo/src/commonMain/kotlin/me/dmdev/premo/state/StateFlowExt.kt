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

package me.dmdev.premo.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import me.dmdev.premo.PresentationModel
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@Suppress("FunctionName")
fun <T> PresentationModel.State(
    initialValue: T,
    stateSource: (() -> Flow<T>)
): StateFlow<T> {

    return stateSource
        .invoke()
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = initialValue
        )
}

@OptIn(ExperimentalStdlibApi::class)
@Suppress("UNCHECKED_CAST", "FunctionName")
inline fun <reified T> PresentationModel.SaveableState(
    initialValue: T,
    key: String
): MutableStateFlow<T> {
    return SaveableState(initialValue, typeOf<T>(), key)
}

@Suppress("UNCHECKED_CAST", "FunctionName")
fun <T> PresentationModel.SaveableState(
    initialValue: T,
    kType: KType,
    key: String
): MutableStateFlow<T> {
    val savedState = stateHandler.getSaved<T>(kType, key)
    val state: MutableStateFlow<T> = if (savedState != null) {
        MutableStateFlow(savedState)
    } else {
        MutableStateFlow(initialValue)
    }
    stateHandler.setSaver(kType, key) { state.value }
    return state
}

infix fun <T> MutableStateFlow<T>.bind(consumer: (T) -> Unit): Job {

    val job = Job()

    onEach { consumer(it) }.launchIn(CoroutineScope(Dispatchers.Main + job))

    return job
}