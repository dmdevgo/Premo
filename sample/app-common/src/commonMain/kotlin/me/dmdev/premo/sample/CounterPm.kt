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

import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import me.dmdev.premo.PmDescription
import me.dmdev.premo.PmParams
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.state.SaveableFlow

class CounterPm(
    private val maxCount: Int,
    params: PmParams
) : PresentationModel(params) {

    @Serializable
    class Description(val maxCount: Int): PmDescription

    private val _count = SaveableFlow(key = "count", initialValue = 0)
    val count = _count.asStateFlow()

    val plusButtonEnabled = StateFlow(false) {
        count.map { it < maxCount }
    }

    val minusButtonEnabled = StateFlow(false) {
        count.map { it > 0 }
    }

    fun plus() {
        if (count.value < maxCount) {
            _count.value = count.value + 1
        }
    }

    fun minus(){
        if (count.value > 0 ) {
            _count.value = count.value - 1
        }
    }

    @Suppress("FunctionName")
    private fun <T> PresentationModel.StateFlow(
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
}