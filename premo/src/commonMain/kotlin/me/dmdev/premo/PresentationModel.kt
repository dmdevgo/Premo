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
import me.dmdev.premo.internal.randomUUID
import me.dmdev.premo.navigation.Navigation
import me.dmdev.premo.navigation.Navigator
import me.dmdev.premo.save.PmState
import me.dmdev.premo.save.PmStateCreator
import me.dmdev.premo.save.StateSaver
import kotlin.reflect.KType
import kotlin.reflect.typeOf

abstract class PresentationModel(params: PmParams) {

    interface Description

    private val pmState: PmState? = params.state
    private val pmFactory: PmFactory = params.factory
    private val stateSaver: StateSaver = params.stateSaver
    private val pmDescription: Description = params.description
    val tag: String = pmState?.tag ?: params.tag
    val parentPm: PresentationModel? = params.parent

    val pmScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    var pmInForegroundScope: CoroutineScope? = null
        private set

    private val saveableStates = mutableMapOf<String, SaveableState<*>>()

    internal val navigator: Navigator by lazy {

        val restoredPmBackStack = pmState?.backstack?.map { pmState ->

            val config = PmParams(
                tag = pmState.tag,
                parent = this,
                state = pmState,
                factory = pmFactory,
                description = pmState.description,
                stateSaver = stateSaver
            )

            pmFactory.createPm(config)
        }

        Navigator(
            parentNavigator = parentPm?.navigator,
            lifecycle = lifecycle,
            scope = pmScope
        ).apply {
            if (restoredPmBackStack != null) {
                setBackStack(restoredPmBackStack)
            }
        }
    }

    @Suppress("unused")
    val PresentationModel.navigator get() = navigator

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
                }
                PmLifecycle.Event.ON_FOREGROUND -> {
                    pmInForegroundScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
                }
                PmLifecycle.Event.ON_BACKGROUND -> {
                    pmInForegroundScope?.cancel()
                    pmInForegroundScope = null
                }
                PmLifecycle.Event.ON_DESTROY -> {
                    pmScope.cancel()
                }
            }
        }.launchIn(pmScope)
    }

    @Suppress("UNCHECKED_CAST", "FunctionName")
    fun <PM : PresentationModel> Child(
        description: Description,
        tag: String = randomUUID()
    ): PM {
        val config = PmParams(
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

    fun Navigation(
        initHandlers: Navigator.() -> Unit
    ): Navigation {

        navigator.initHandlers()

        if (navigator.backstack.isEmpty()) {
            navigator.handleStart()
        }

        return navigator
    }

    internal fun saveState(pmStateCreator: PmStateCreator): PmState {

        val backstack = navigator.backstack.map { pm ->
            pm.saveState(pmStateCreator)
        }

        return pmStateCreator.createPmState(
            tag = tag,
            backstack = backstack,
            children = children.mapValues { entry -> entry.value.saveState(pmStateCreator) },
            states = saveableStates.mapValues { entry ->
                stateSaver.saveState(entry.value.kType, entry.value.state.value)
            },
            description = pmDescription
        )
    }
}