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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import me.dmdev.premo.PmLifecycle
import me.dmdev.premo.PmLifecycle.State.*
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.State
import me.dmdev.premo.value
import kotlin.reflect.KClass

class Navigator internal constructor(
    private val parentNavigator: Navigator?,
    private val lifecycle: PmLifecycle,
    private val scope: CoroutineScope,
) : Navigation {

    private val handlers = mutableListOf<(message: NavigationMessage) -> Boolean>()
    private val backstackState: State<List<PresentationModel>> = State(listOf())

    var backstack: List<PresentationModel>
        get() = backstackState.value
        private set(value) {
            backstackState.value = value
        }

    internal var exitHandler: (() -> Boolean)? = null
    var startHandler: (() -> Unit)? = null
    var backHandler: (() -> Boolean)? = null
    var systemBackHandler: (() -> Boolean)? = null

    init {
        lifecycle.stateFlow.onEach { state ->
            backstack.lastOrNull()?.lifecycle?.moveTo(state)
        }.launchIn(scope)
    }

    override val backstackChanges: Flow<BackstackChange>
        get() = flow {
            var oldPmStack: List<PresentationModel> = backstackState.value
            backstackState.flow().collect { newPmStack ->

                val oldTopPm = oldPmStack.lastOrNull()
                val newTopPm = newPmStack.lastOrNull()

                val pmStackChange = if (newTopPm != null && oldTopPm != null) {
                    when {
                        oldTopPm === newTopPm -> {
                            BackstackChange.Set(newTopPm)
                        }
                        oldPmStack.any { it === newTopPm } -> {
                            BackstackChange.Pop(newTopPm, oldTopPm)
                        }
                        else -> {
                            BackstackChange.Push(newTopPm, oldTopPm)
                        }
                    }
                } else if (newTopPm != null) {
                    BackstackChange.Set(newTopPm)
                } else {
                    BackstackChange.Empty
                }

                emit(pmStackChange)
                oldPmStack = newPmStack
            }
        }

    fun push(pm: PresentationModel) {
        backstack.lastOrNull()?.lifecycle?.moveTo(CREATED)
        pm.lifecycle.moveTo(lifecycle.state)
        backstack = backstack.plus(pm)
    }

    fun pop(): Boolean {
        return if (backstack.isNotEmpty()) {
            backstack.lastOrNull()?.lifecycle?.moveTo(DESTROYED)
            if (backstack.isNotEmpty()) backstack = backstack.dropLast(1)
            backstack.lastOrNull()?.lifecycle?.moveTo(lifecycle.state)
            true
        } else {
            false
        }
    }

    fun setBackStack(pmList: List<PresentationModel>) {
        backstack = pmList
        pmList.forEach { pm ->
            pm.lifecycle.moveTo(INITIALIZED)
        }
        pmList.lastOrNull()?.lifecycle?.moveTo(lifecycle.state)
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
        return backHandler?.invoke()
            ?: if (backstack.size > 1) {
                pop()
            } else {
                parentNavigator?.handleBack()
                    ?: exitHandler?.invoke()
                    ?: false
            }
    }

    fun handleSystemBack(): Boolean {
        val handled = systemBackHandler?.invoke() ?: false
        return if (!handled) {
            val handledByChild = backstack.lastOrNull()?.navigator?.systemBackHandler?.invoke()
                ?: false
            if (!handledByChild) {
                if (backstack.size > 1) pop() else false
            } else {
                true
            }
        } else {
            true
        }
    }

    fun sendMessage(message: NavigationMessage) {
        if (!handlers.any { it.invoke(message) }) {
            parentNavigator?.sendMessage(message)
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