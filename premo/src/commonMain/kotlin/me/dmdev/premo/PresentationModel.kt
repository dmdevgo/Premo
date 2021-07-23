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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.dmdev.premo.navigation.NavigationMessage
import me.dmdev.premo.navigation.PmFactory
import me.dmdev.premo.navigation.PmRouter
import kotlin.reflect.KType
import kotlin.reflect.typeOf

abstract class PresentationModel(config: PmConfig) {

    interface Description

    private val pmState: PmState? = config.state
    private val pmFactory: PmFactory = config.factory
    private val stateSaver: StateSaver = config.stateSaver
    private val pmDescription: Description = config.description
    val tag: String = pmState?.tag ?: config.tag
    val parentPm: PresentationModel? = config.parent

    val pmScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    var pmInForegroundScope: CoroutineScope? = null
        private set

    private var routerOrNull: PmRouter? = null
    private val saveableStates = mutableMapOf<String, SaveableState<*>>()

    private class SaveableState<T>(
        val state: State<T>,
        val kType: KType
    )

    private val children = mutableMapOf<String, PresentationModel>()
    val lifecycle: PmLifecycle = PmLifecycle()

    init {

        lifecycle.stateFlow.onEach { state ->
            children.forEach { entry ->
                entry.value.lifecycle.moveTo(state)
            }
        }.launchIn(pmScope)

        lifecycle.eventFlow.onEach { event ->
            when (event) {
                PmLifecycle.Event.ON_CREATE -> {
                    onCreate()
                }
                PmLifecycle.Event.ON_FOREGROUND -> {
                    pmInForegroundScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
                    onForeground()
                }
                PmLifecycle.Event.ON_BACKGROUND -> {
                    onBackground()
                    pmInForegroundScope?.cancel()
                }
                PmLifecycle.Event.ON_DESTROY -> {
                    onDestroy()
                    pmScope.cancel()
                }
            }
        }.launchIn(pmScope)
    }

    @Suppress("FunctionName")
    protected fun Router(initialDescription: Description): PmRouter {

        val restoredPmBackStack = pmState?.backstack?.map { pmState ->

            val config = PmConfig(
                tag = pmState.tag,
                parent = this,
                state = pmState,
                factory = pmFactory,
                description = pmState.description,
                stateSaver = stateSaver
            )

            pmFactory.createPm(config)
        }

        return routerOrNull ?: PmRouter(
            hostPm = this,
        ).also { router ->

            if (restoredPmBackStack != null) {
                router.setBackStack(restoredPmBackStack)
            }

            if (router.pmStack.value.isEmpty()) {
                router.push(Child(initialDescription))
            }
            routerOrNull = router
        }
    }

    @Suppress("UNCHECKED_CAST", "FunctionName")
    fun <PM : PresentationModel> Child(
        description: Description,
        tag: String = randomUUID()
    ): PM {
        val config = PmConfig(
            tag = tag,
            parent = this,
            state = pmState?.children?.get(tag),
            factory = pmFactory,
            description = description,
            stateSaver = stateSaver
        )

        return pmFactory.createPm(config) as PM
    }

    @Suppress("FunctionName")
    fun <PM : PresentationModel> AttachedChild(
        description: Description,
        tag: String
    ): PM {

        val pm = Child<PM>(description, tag)
        pm.lifecycle.moveTo(lifecycle.state)
        children[tag] = pm
        return pm
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Suppress("UNCHECKED_CAST", "FunctionName")
    inline fun <reified T> SaveableState(
        initialValue: T,
        key: String
    ): State<T> {
        return SaveableState(initialValue, typeOf<T>(), key)
    }

    @Suppress("UNCHECKED_CAST", "FunctionName")
    fun <T> SaveableState(
        initialValue: T,
        kType: KType,
        key: String
    ): State<T> {
        val savedState = pmState?.states?.get(key)
        val state: State<T> = if (savedState != null) {
            State(stateSaver.restoreState(kType, savedState))
        } else {
            State(initialValue)
        }
        saveableStates[key] = SaveableState(state, kType)
        return state
    }

    var <T> State<T>.value: T
        get() = mutableStateFlow.value
        set(value) {
            mutableStateFlow.value = value
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

    open fun back() {
        val router = routerOrNull
        if (router != null && router.pmStack.value.size > 1) {
            router.pop()
        } else {
            parentPm?.back()
        }
    }

    open fun handleBack(): Boolean {
        val router = routerOrNull
        if (router != null) {
            val handledByNestedPm =
                router.pmStack.value.lastOrNull()?.handleBack() ?: false
            if (handledByNestedPm.not()) {
                if (router.pmStack.value.size > 1) {
                    router.pop()
                    return true
                }
            } else {
                return true
            }
            return false
        } else {
            return false
        }
    }

    internal fun saveState(pmStateCreator: PmStateCreator): PmState {

        val router = routerOrNull
        val routerState =
            router?.pmStack?.value?.map { pm -> pm.saveState(pmStateCreator) } ?: listOf()

        return pmStateCreator.createPmState(
            tag = tag,
            backstack = routerState,
            children = children.mapValues { entry -> entry.value.saveState(pmStateCreator) },
            states = saveableStates.mapValues { entry ->
                stateSaver.saveState(entry.value.kType, entry.value.state.value)
            },
            description = pmDescription
        )
    }
}