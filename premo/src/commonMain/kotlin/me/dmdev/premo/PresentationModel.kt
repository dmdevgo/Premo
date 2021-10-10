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
import me.dmdev.premo.internal.randomUUID
import me.dmdev.premo.lifecycle.Lifecycle
import me.dmdev.premo.lifecycle.LifecycleEvent
import me.dmdev.premo.lifecycle.LifecycleObserver
import me.dmdev.premo.navigation.NavigationMessageHandler
import me.dmdev.premo.save.PmState
import me.dmdev.premo.save.PmStateCreator
import me.dmdev.premo.save.PmStateHandler
import me.dmdev.premo.save.StateSaver
import kotlin.reflect.KType
import kotlin.reflect.typeOf

abstract class PresentationModel(params: PmParams) {

    private val pmState: PmState? = params.state
    private val pmFactory: PmFactory = params.factory
    private val stateSaver: StateSaver = params.stateSaver
    private val pmDescription: PmDescription = params.description

    val tag: String = pmState?.tag ?: params.tag
    val parent: PresentationModel? = params.parent
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    var inForegroundScope: CoroutineScope? = null
        private set

    val messageHandler: NavigationMessageHandler = NavigationMessageHandler(parent?.messageHandler)
    val pmStateHandler: PmStateHandler = PmStateHandler(stateSaver, pmState?.states ?: mapOf())

    private val children = mutableMapOf<String, PresentationModel>()
    private val attachedChildren = mutableMapOf<String, PresentationModel>()

    val lifecycle: Lifecycle = Lifecycle()

    init {
        subscribeToLifecycle()
    }

    @Suppress("UNCHECKED_CAST", "FunctionName")
    fun <PM : PresentationModel> Child(
        description: PmDescription,
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

        val child = pmFactory.createPm(config) as PM
        children[tag] = child
        return child
    }

    @Suppress("FunctionName")
    fun <PM : PresentationModel> SavedChild(tag: String): PM? {
        return pmState?.children?.get(tag)?.let { childState ->
            Child(childState.description, childState.tag)
        }
    }

    @Suppress("FunctionName")
    fun <PM : PresentationModel> AttachedChild(
        description: PmDescription,
        tag: String
    ): PM {

        val pm = Child<PM>(description, tag)
        pm.lifecycle.moveTo(lifecycle.state)
        attachedChildren[tag] = pm
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
        val savedState = pmStateHandler.getSaved<T>(kType, key)
        val state: State<T> = if (savedState != null) {
            State(savedState)
        } else {
            State(initialValue)
        }
        pmStateHandler.setSaver(kType, key) { state.value }
        return state
    }

    var <T> State<T>.value: T
        get() = mutableStateFlow.value
        set(value) {
            mutableStateFlow.value = value
        }

    internal fun saveState(pmStateCreator: PmStateCreator): PmState {

        return pmStateCreator.createPmState(
            tag = tag,
            children = children.mapValues { entry -> entry.value.saveState(pmStateCreator) },
            states = pmStateHandler.saveState(),
            description = pmDescription
        )
    }

    private fun subscribeToLifecycle() {
        lifecycle.addObserver(object : LifecycleObserver {
            override fun onLifecycleChange(lifecycle: Lifecycle, event: LifecycleEvent) {

                attachedChildren.forEach { entry ->
                    entry.value.lifecycle.moveTo(lifecycle.state)
                }

                when (event) {
                    LifecycleEvent.ON_CREATE -> {
                    }
                    LifecycleEvent.ON_FOREGROUND -> {
                        inForegroundScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
                    }
                    LifecycleEvent.ON_BACKGROUND -> {
                        inForegroundScope?.cancel()
                        inForegroundScope = null
                    }
                    LifecycleEvent.ON_DESTROY -> {
                        scope.cancel()
                    }
                }
            }
        })
    }
}