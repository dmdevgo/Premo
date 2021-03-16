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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

open class State<T> internal constructor(
    internal val pm: PresentationModel,
    initialValue: T
) {
    internal val mutableStateFlow = MutableStateFlow(initialValue)

    fun stateFlow(): StateFlow<T> = mutableStateFlow

    infix fun bindTo(consumer: (T) -> Unit) {
        with(pm) {
            pmInForegroundScope?.launch {
                mutableStateFlow.collect { v ->
                    consumer(v)
                }
            }
        }
    }
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
    stateSource: (() -> Flow<T>)? = null
): State<T> {

    val state = State(pm = this, initialValue = initialValue)

    if (stateSource != null) {
        pmScope.launch {
            stateSource().collect { v ->
                state.mutableStateFlow.value = v
            }
        }
    }

    return state
}