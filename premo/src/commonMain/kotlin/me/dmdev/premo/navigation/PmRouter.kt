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

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import me.dmdev.premo.LifecycleState
import me.dmdev.premo.Parcelable
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.State
import kotlin.reflect.KClass

class PmRouter internal constructor(
    private val hostPm: PresentationModel,
    private val pmFactory: PmFactory
) {

    private var _pmStack = mutableListOf<BackStackEntry>()
    val pmStack: List<BackStackEntry> get() = _pmStack

    private val _pmStackChanges = MutableSharedFlow<List<BackStackEntry>>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val pmStackChanges: Flow<List<BackStackEntry>> get() = _pmStackChanges
    val pmOnTop = hostPm.State(null) {
        pmStackChanges.map { it.lastOrNull()?.pm }
    }

    fun push(clazz: KClass<out PresentationModel>, params: Parcelable?) {
        _pmStack.lastOrNull()?.pm?.moveLifecycleTo(LifecycleState.CREATED)
        val pm = pmFactory.createPm(clazz, params)
        pm.parentPm = hostPm
        _pmStack.add(BackStackEntry(pm, params))
        pm.moveLifecycleTo(hostPm.lifecycleState.value)
        notifyChanges()
    }

    fun pop() {
        _pmStack.removeLast().pm.moveLifecycleTo(LifecycleState.DESTROYED)
        _pmStack.lastOrNull()?.pm?.moveLifecycleTo(hostPm.lifecycleState.value)
        notifyChanges()
    }

    private fun notifyChanges() {
        _pmStackChanges.tryEmit(_pmStack)
    }

    class BackStackEntry(
        val pm: PresentationModel,
        val params: Parcelable?
    )
}