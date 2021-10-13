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
import me.dmdev.premo.lifecycle.LifecycleState
import me.dmdev.premo.save.PmStateHandler
import me.dmdev.premo.save.StateSaver

abstract class PresentationModel(params: PmParams) {

    private val pmFactory: PmFactory = params.factory
    private val stateSaver: StateSaver = params.stateSaver

    val tag: String = params.tag
    val description: PmDescription = params.description
    val parent: PresentationModel? = params.parent
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    var inForegroundScope: CoroutineScope? = null
        private set

    val messageHandler: PmMessageHandler = PmMessageHandler(parent?.messageHandler)
    val pmStateHandler: PmStateHandler = PmStateHandler(stateSaver, params.state)

    private val attachedChildren = mutableListOf<PresentationModel>()

    val lifecycle: Lifecycle = Lifecycle()

    init {
        initSaver()
        subscribeToLifecycle()
    }

    var <T> State<T>.value: T
        get() = mutableStateFlow.value
        set(value) {
            mutableStateFlow.value = value
        }

    fun attachChild(pm: PresentationModel) {
        pm.lifecycle.moveTo(lifecycle.state)
        attachedChildren.add(pm)
    }

    fun detachChild(pm: PresentationModel) {
        pm.lifecycle.moveTo(LifecycleState.DESTROYED)
        attachedChildren.remove(pm)
    }

    @Suppress("UNCHECKED_CAST", "FunctionName")
    fun <PM : PresentationModel> Child(
        description: PmDescription,
        tag: String = randomUUID()
    ): PM {
        val config = PmParams(
            tag = tag,
            parent = this,
            state = pmStateHandler.getSaved(tag) ?: mapOf(),
            factory = pmFactory,
            description = description,
            stateSaver = stateSaver
        )

        return pmFactory.createPm(config) as PM
    }

    @Suppress("FunctionName")
    fun <PM : PresentationModel> AttachedChild(
        description: PmDescription,
        tag: String
    ): PM {
        return Child<PM>(description, tag).apply {
            attachChild(this)
        }
    }

    private fun initSaver() {
        parent?.pmStateHandler?.setSaver(tag) {
            pmStateHandler.saveState()
        }
    }

    private fun subscribeToLifecycle() {
        lifecycle.addObserver(object : LifecycleObserver {
            override fun onLifecycleChange(lifecycle: Lifecycle, event: LifecycleEvent) {

                attachedChildren.forEach { pm ->
                    pm.lifecycle.moveTo(lifecycle.state)
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
                        parent?.pmStateHandler?.removeSaver(tag)
                    }
                }
            }
        })
    }
}