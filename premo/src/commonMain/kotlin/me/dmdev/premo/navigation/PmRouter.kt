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
import me.dmdev.premo.*

class PmRouter internal constructor(
    initialDescription: Saveable,
    private val hostPm: PresentationModel,
    private val pmFactory: PmFactory
) {

    val pmStack: State<List<BackStackEntry>> = State(listOf())

    init {

        hostPm.pmState?.routerState?.let { restoreBackStack(it) }

        if (pmStack.value.isEmpty()) {
            push(initialDescription)
        }
    }

    private fun restoreBackStack(backStackState: List<BackStackEntryState>) {
        backStackState.forEach { entry ->
            val pm = pmFactory.createPm(entry.description, entry.pmState)
            pm.parentPm = hostPm
            pm.moveLifecycleTo(hostPm.lifecycleState.value)
            pmStack.value = pmStack.value.plus(BackStackEntry(pm, entry.description))
        }
    }

    val pmStackChanges: Flow<PmStackChange> = flow {
        var oldPmStack: List<BackStackEntry> = pmStack.value
        pmStack.flow().collect { newPmStack ->

            val oldTop = oldPmStack.lastOrNull()
            val newTop = newPmStack.lastOrNull()

            val pmStackChange = if (newTop != null && oldTop != null) {
                when {
                    oldTop === newTop -> {
                        PmStackChange.Set(newTop.pm)
                    }
                    oldPmStack.any { it === newTop } -> {
                        PmStackChange.Pop(newTop.pm, oldTop.pm)
                    }
                    else -> {
                        PmStackChange.Push(newTop.pm, oldTop.pm)
                    }
                }
            } else if (newTop != null) {
                PmStackChange.Set(newTop.pm)
            } else {
                PmStackChange.Empty
            }

            emit(pmStackChange)
            oldPmStack = newPmStack
        }
    }

    fun push(description: Saveable, pmTag: String = randomUUID()) {
        pmStack.value.lastOrNull()?.pm?.moveLifecycleTo(LifecycleState.CREATED)
        val pm = pmFactory.createPm(description, null)
        pm.tag = pmTag
        pm.parentPm = hostPm
        pm.moveLifecycleTo(hostPm.lifecycleState.value)
        pmStack.value = pmStack.value.plus(BackStackEntry(pm, description))
    }

    fun pop(): Boolean {
        return if (pmStack.value.isNotEmpty()) {
            pmStack.value.lastOrNull()?.pm?.moveLifecycleTo(LifecycleState.DESTROYED)
            if (pmStack.value.isNotEmpty()) pmStack.value = pmStack.value.dropLast(1)
            pmStack.value.lastOrNull()?.pm?.moveLifecycleTo(hostPm.lifecycleState.value)
            true
        } else {
            false
        }
    }

    class BackStackEntry(
        val pm: PresentationModel,
        val description: Saveable
    )
}