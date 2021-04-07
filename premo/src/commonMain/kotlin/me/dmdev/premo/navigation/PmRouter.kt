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

package me.dmdev.premo.navigation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import me.dmdev.premo.LifecycleState
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.State
import me.dmdev.premo.value

class PmRouter internal constructor(
    private val hostPm: PresentationModel,
) {

    val pmStack: State<List<PresentationModel>> = State(listOf())

    val pmStackChanges: Flow<PmStackChange> = flow {
        var oldPmStack: List<PresentationModel> = pmStack.value
        pmStack.flow().collect { newPmStack ->

            val oldTopPm = oldPmStack.lastOrNull()
            val newTopPm = newPmStack.lastOrNull()

            val pmStackChange = if (newTopPm != null && oldTopPm != null) {
                when {
                    oldTopPm === newTopPm -> {
                        PmStackChange.Set(newTopPm)
                    }
                    oldPmStack.any { it === newTopPm } -> {
                        PmStackChange.Pop(newTopPm, oldTopPm)
                    }
                    else -> {
                        PmStackChange.Push(newTopPm, oldTopPm)
                    }
                }
            } else if (newTopPm != null) {
                PmStackChange.Set(newTopPm)
            } else {
                PmStackChange.Empty
            }

            emit(pmStackChange)
            oldPmStack = newPmStack
        }
    }

    fun push(pm: PresentationModel) {
        pmStack.value.lastOrNull()?.moveLifecycleTo(LifecycleState.CREATED)
        pm.moveLifecycleTo(hostPm.lifecycleState.value)
        pmStack.value = pmStack.value.plus(pm)
    }

    fun pop(): Boolean {
        return if (pmStack.value.isNotEmpty()) {
            pmStack.value.lastOrNull()?.moveLifecycleTo(LifecycleState.DESTROYED)
            if (pmStack.value.isNotEmpty()) pmStack.value = pmStack.value.dropLast(1)
            pmStack.value.lastOrNull()?.moveLifecycleTo(hostPm.lifecycleState.value)
            true
        } else {
            false
        }
    }

    fun setBackStack(pmList: List<PresentationModel>) {
        pmStack.value = pmList
        pmList.forEach { pm ->
            pm.moveLifecycleTo(LifecycleState.INITIALIZED)
        }
        pmList.lastOrNull()?.moveLifecycleTo(hostPm.lifecycleState.value)
    }
}