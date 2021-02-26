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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.dmdev.premo.navigation.NavigationMessage

abstract class PresentationModel {

    internal val pmScope = CoroutineScope(SupervisorJob() + Dispatchers.UI)
    internal var pmBindScope: CoroutineScope? = null

    internal val lifecycleState = MutableStateFlow(LifecycleState.INITIALIZED)
    internal val lifecycleEvent = MutableSharedFlow<LifecycleEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    internal var parentPm: PresentationModel? = null

    init {
        pmScope.launch {
            lifecycleEvent.collect { event ->
                @Suppress("MemberVisibilityCanBePrivate")
                when (event) {
                    LifecycleEvent.ON_CREATE -> {
                        lifecycleState.value = LifecycleState.CREATED
                        onCreate()
                    }
                    LifecycleEvent.ON_FOREGROUND -> {
                        pmBindScope = CoroutineScope(SupervisorJob() + Dispatchers.UI)
                        lifecycleState.value = LifecycleState.IN_FOREGROUND
                        onForeground()
                    }
                    LifecycleEvent.ON_BACKGROUND -> {
                        pmBindScope?.cancel()
                        lifecycleState.value = LifecycleState.CREATED
                        onBackground()
                    }
                    LifecycleEvent.ON_DESTROY -> {
                        lifecycleState.value = LifecycleState.DESTROYED
                        onDestroy()
                        pmScope.cancel()
                    }
                }
            }
        }
    }

    protected var <T> State<T>.value: T
        get() = mutableStateFlow.value
        set(value) {
            mutableStateFlow.value = value
        }

    protected fun <T> Flow<T>.consumeBy(state: State<T>): Flow<T> {
        return onEach { state.mutableStateFlow.value = it }
    }

    protected fun <T> Flow<T>.consumeBy(action: Action<T>): Flow<T> {
        return onEach { action.emit(it) }
    }

    protected fun <T> Flow<T>.consumeBy(command: Command<T>): Flow<T> {
        return onEach { command.emit(it) }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected open fun onCreate() {
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected open fun onForeground() {
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected open fun onBackground() {
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected open fun onDestroy() {
    }

    protected open fun handleNavigationMessage(message: NavigationMessage) {
        parentPm?.handleNavigationMessage(message)
    }

    open fun handleBack(): Boolean {
        return false
    }
}