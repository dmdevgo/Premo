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

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.Serializable
import me.dmdev.premo.*

class CounterUdfPm(
    private val maxCount: Int,
    config: PmConfig
) : PresentationModel(config) {

    @Serializable
    class Description(val maxCount: Int) : PresentationModel.Description

    private sealed class ActionType {
        object Minus : ActionType()
        object Plus : ActionType()
    }

    @Serializable
    data class CounterState(
        val count: Int = 0,
        val maxCount: Int
    ) : Saveable {
        val plusEnabled get() = count < maxCount
        val minusEnabled get() = count > 0
    }

    private val action: Action<ActionType> = Action()

    val state = UDF(
        initialValue = CounterState(maxCount = maxCount),
        action = action
    ) { state, action ->
        when (action) {
            ActionType.Plus -> {
                if (state.count < maxCount) {
                    state.copy(count = state.count + 1)
                } else {
                    state
                }
            }
            ActionType.Minus -> {
                if (state.count > 0) {
                    state.copy(count = state.count - 1)
                } else {
                    state
                }
            }
        }
    }

    fun minusClick() {
        action.invoke(ActionType.Minus)
    }

    fun plusClick() {
        action.invoke(ActionType.Plus)
    }

    private inline fun <reified T, A> UDF(initialValue: T, action: Action<A>, crossinline reducer: (state: T, action: A) -> T): State<T> {
        val state = SaveableState(initialValue, "udf_state")
        action.flow()
            .map { act -> reducer(state.value, act) }
            .onEach { state.value = it }
            .launchIn(pmScope)
        return state
    }
}