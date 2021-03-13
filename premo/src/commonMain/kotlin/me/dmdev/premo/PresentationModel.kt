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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.dmdev.premo.navigation.NavigationMessage
import me.dmdev.premo.navigation.PmFactory
import me.dmdev.premo.navigation.PmRouter

abstract class PresentationModel {

    val pmScope = CoroutineScope(SupervisorJob() + Dispatchers.UI)
    var pmInForegroundScope: CoroutineScope? = null

    internal val routers = mutableListOf<PmRouter>()
    internal val saveableStates = mutableListOf<State<*>>()

    internal val lifecycleState = MutableStateFlow(LifecycleState.INITIALIZED)
    private val lifecycleEvent = MutableSharedFlow<LifecycleEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    internal var parentPm: PresentationModel? = null

    protected var <T> State<T>.value: T
        get() = mutableStateFlow.value
        set(value) {
            mutableStateFlow.value = value
        }

    var tag: String = randomUUID()
        internal set

    protected fun <T> Flow<T>.consumeBy(state: State<T>): Flow<T> {
        return onEach { state.mutableStateFlow.value = it }
    }

    protected fun <T> Flow<T>.consumeBy(action: Action<T>): Flow<T> {
        return onEach { action.invoke(it) }
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
        routers.forEach { router ->
            val handledByNestedPm = router.pmStack.value.lastOrNull()?.pm?.handleBack() ?: false
            if (handledByNestedPm.not()) {
                if (router.pmStack.value.size > 1) {
                    router.pop()
                    return true
                }
            } else {
                return true
            }
        }
        return false
    }

    @Suppress("FunctionName")
    protected fun Router(
        pmFactory: PmFactory,
        initialDescription: Saveable,
    ): PmRouter {
        return PmRouter(this, pmFactory).also { router ->
            routers.add(router)
            pmScope.launch {
                lifecycleState
//                    .first { it == LifecycleState.CREATED }
                    .apply {
                        if (router.pmStack.value.isEmpty()) {
                            router.push(initialDescription)
                        }
                    }
            }
        }
    }

    internal fun moveLifecycleTo(targetLifecycle: LifecycleState) {

        fun moveRouterPm(targetLifecycle: LifecycleState) {
            routers.forEach { router ->
                router.pmStack.value.lastOrNull()?.pm?.moveLifecycleTo(targetLifecycle)
            }
        }

        fun doOnCreate() {
            lifecycleState.value = LifecycleState.CREATED
            lifecycleEvent.tryEmit(LifecycleEvent.ON_CREATE)
            onCreate()
            moveRouterPm(LifecycleState.CREATED)
        }

        fun doOnForeground() {
            pmInForegroundScope = CoroutineScope(SupervisorJob() + Dispatchers.UI)
            lifecycleState.value = LifecycleState.IN_FOREGROUND
            lifecycleEvent.tryEmit(LifecycleEvent.ON_FOREGROUND)
            onForeground()
            moveRouterPm(LifecycleState.IN_FOREGROUND)
        }

        fun doOnBackground() {
            moveRouterPm(LifecycleState.CREATED)
            pmInForegroundScope?.cancel()
            lifecycleState.value = LifecycleState.CREATED
            lifecycleEvent.tryEmit(LifecycleEvent.ON_BACKGROUND)
            onBackground()
        }

        fun doOnDestroy() {
            moveRouterPm(LifecycleState.DESTROYED)
            pmScope.cancel()
            lifecycleState.value = LifecycleState.DESTROYED
            lifecycleEvent.tryEmit(LifecycleEvent.ON_DESTROY)
            onDestroy()
        }

        when (targetLifecycle) {
            LifecycleState.INITIALIZED -> {
                // do nothing, initial lifecycle state is INITIALIZED
            }
            LifecycleState.CREATED -> {
                when (lifecycleState.value) {
                    LifecycleState.INITIALIZED -> {
                        doOnCreate()
                    }
                    LifecycleState.IN_FOREGROUND -> {
                        doOnBackground()
                    }
                    else -> { /*do nothing */ }
                }
            }
            LifecycleState.IN_FOREGROUND -> {
                when (lifecycleState.value) {
                    LifecycleState.INITIALIZED -> {
                        doOnCreate()
                        doOnForeground()
                    }
                    LifecycleState.CREATED -> {
                        doOnForeground()
                    }
                    else -> { /*do nothing */ }
                }
            }
            LifecycleState.DESTROYED -> {
                when (lifecycleState.value) {
                    LifecycleState.INITIALIZED -> {
                        doOnDestroy()
                    }
                    LifecycleState.CREATED -> {
                        doOnDestroy()
                    }
                    LifecycleState.IN_FOREGROUND -> {
                        doOnBackground()
                        doOnDestroy()
                    }
                    LifecycleState.DESTROYED -> { /*do nothing */ }
                }
            }
        }
    }
}