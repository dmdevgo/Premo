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

package me.dmdev.premo.sample

import kotlinx.serialization.Serializable
import me.dmdev.premo.PmArgs
import me.dmdev.premo.SaveableFlow
import me.dmdev.premo.SingleStatePresentationModel
import me.dmdev.premo.sample.CounterPm.State

class CounterPm(
    args: Args
) : SingleStatePresentationModel<State>(args) {

    @Serializable
    data class Args(val maxCount: Int) : PmArgs() {
        override val key: String get() = "counter_$maxCount"
    }

    @Serializable
    data class State(
        val count: Int,
        val maxCount: Int
    ) {
        val plusEnabled: Boolean get() = count < maxCount
        val minusEnabled: Boolean get() = count > 0
    }

    override val mutableStateFlow = SaveableFlow(
        key = "count",
        initialValue = State(maxCount = args.maxCount, count = 0)
    )

    fun plus() {
        state = state.copy(
            count = state.count + 1
        )
    }

    fun minus() {
        if (state.count > 0) {
            mutableStateFlow.value = state.copy(
                count = state.count - 1
            )
        }
    }
}
