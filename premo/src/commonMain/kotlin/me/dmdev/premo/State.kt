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

package me.dmdev.premo

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*

class State<T>(
    initialValue: T
) {
    internal val mutableStateFlow = MutableStateFlow(initialValue)

    fun flow(): StateFlow<T> = mutableStateFlow
}

var <T> State<T>.value: T
    get() {
        return mutableStateFlow.value
    }
    internal set(value) {
        mutableStateFlow.value = value
    }

@Suppress("FunctionName")
fun <T> PresentationModel.State(
    initialValue: T,
    stateSource: (() -> Flow<T>)
): State<T> {

    val state = State(initialValue = initialValue)

    stateSource
        .invoke()
        .onEach { state.value = it }
        .launchIn(scope)

    return state
}

infix fun <T> State<T>.bind(consumer: (T) -> Unit): Job {

    val job = Job()

    mutableStateFlow
        .onEach { consumer(it) }
        .launchIn(CoroutineScope(Dispatchers.Main + job))

    return job
}