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

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.dmdev.premo.PmLifecycle.State.*
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.State
import me.dmdev.premo.value
import kotlin.reflect.KClass

class Navigator internal constructor(
    val hostPm: PresentationModel,
) {

    private val handlers = mutableListOf<(message: NavigationMessage) -> Boolean>()

    val pmStack: State<List<PresentationModel>> = State(listOf())

    internal var exitHandler: (() -> Unit)? = null
    var startHandler: (() -> Unit)? = null
    var backHandler: (() -> Boolean)? = {
        if (pmStack.value.size > 1) {
            pop()
            true
        } else {
            hostPm.parentPm?.navigator?.handleBack() ?: false
        }
    }
    var systemBackHandler: (() -> Boolean) = {
        if (pmStack.value.lastOrNull()?.navigator?.systemBackHandler?.invoke() == true) {
            true
        } else {
            if (pmStack.value.size > 1) {
                pop()
                true
            } else {
                false
            }
        }
    }

    init {
        hostPm.lifecycle.stateFlow.onEach { state ->
            pmStack.value.lastOrNull()?.lifecycle?.moveTo(state)
        }.launchIn(hostPm.pmScope)
    }

    fun push(pm: PresentationModel) {
        pmStack.value.lastOrNull()?.lifecycle?.moveTo(CREATED)
        pm.lifecycle.moveTo(hostPm.lifecycle.state)
        pmStack.value = pmStack.value.plus(pm)
    }

    fun pop(): Boolean {
        return if (pmStack.value.isNotEmpty()) {
            pmStack.value.lastOrNull()?.lifecycle?.moveTo(DESTROYED)
            if (pmStack.value.isNotEmpty()) pmStack.value = pmStack.value.dropLast(1)
            pmStack.value.lastOrNull()?.lifecycle?.moveTo(hostPm.lifecycle.state)
            true
        } else {
            false
        }
    }

    fun setBackStack(pmList: List<PresentationModel>) {
        pmStack.value = pmList
        pmList.forEach { pm ->
            pm.lifecycle.moveTo(INITIALIZED)
        }
        pmList.lastOrNull()?.lifecycle?.moveTo(hostPm.lifecycle.state)
    }

    fun <M : NavigationMessage> addMessageHandler(
        kClass: KClass<M>,
        handler: (message: M) -> Unit
    ) {
        handlers.add {
            if (kClass.isInstance(it)) {
                handler(it as M)
                true
            } else {
                false
            }
        }
    }

    fun handleStart() {
        startHandler?.invoke()
    }

    fun handleBack(): Boolean {
        return backHandler?.invoke() ?: false
    }

    fun handleSystemBack(): Boolean {
        return systemBackHandler.invoke()
    }

    fun sendMessage(message: NavigationMessage) {
        if (!handlers.any { it.invoke(message) }) {
            hostPm.parentPm?.navigator?.sendMessage(message)
        }
    }
}

fun Navigator.onStart(handler: () -> Unit) {
    startHandler = handler
}

fun Navigator.onBack(handler: () -> Boolean) {
    backHandler = handler
}

fun Navigator.onSystemBack(handler: () -> Boolean) {
    systemBackHandler = handler
}

inline fun <reified M : NavigationMessage> Navigator.onMessage(
    noinline handler: (message: M) -> Unit
) {
    addMessageHandler(M::class, handler)
}