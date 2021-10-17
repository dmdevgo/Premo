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
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import me.dmdev.premo.PmLifecycle.State.DESTROYED

abstract class PresentationModel(params: PmParams) {

    private val pmFactory: PmFactory = params.factory
    private val pmStateSaver: PmStateSaver = params.stateSaver

    val tag: String = params.tag
    val description: PmDescription = params.description
    val parent: PresentationModel? = params.parent
    val lifecycle: PmLifecycle = PmLifecycle()
    val scope = MainScope()
    var inForegroundScope: CoroutineScope? = null
        private set
    val messageHandler: PmMessageHandler = PmMessageHandler(parent?.messageHandler)
    val stateHandler: PmStateHandler = PmStateHandler(pmStateSaver, params.state)

    private val attachedChildren = mutableListOf<PresentationModel>()

    init {
        initSaver()
        subscribeToLifecycle()
    }

    fun attachChild(pm: PresentationModel) {
        pm.lifecycle.moveTo(lifecycle.state)
        attachedChildren.add(pm)
    }

    fun detachChild(pm: PresentationModel) {
        pm.lifecycle.moveTo(DESTROYED)
        attachedChildren.remove(pm)
    }

    @Suppress("FunctionName")
    fun Child(
        description: PmDescription,
        tag: String = randomUUID()
    ): PresentationModel {
        val config = PmParams(
            tag = tag,
            parent = this,
            state = stateHandler.getSaved(tag) ?: mapOf(),
            factory = pmFactory,
            description = description,
            stateSaver = pmStateSaver
        )

        return pmFactory.createPm(config)
    }

    @Suppress("FunctionName")
    fun AttachedChild(
        description: PmDescription,
        tag: String
    ): PresentationModel {
        return Child(description, tag).apply {
            attachChild(this)
        }
    }

    private fun initSaver() {
        parent?.stateHandler?.setSaver(tag) {
            stateHandler.saveState()
        }
    }

    private fun subscribeToLifecycle() {
        lifecycle.addObserver(object : PmLifecycle.Observer {
            override fun onLifecycleChange(lifecycle: PmLifecycle, event: PmLifecycle.Event) {

                attachedChildren.forEach { pm ->
                    pm.lifecycle.moveTo(lifecycle.state)
                }

                when (event) {
                    PmLifecycle.Event.ON_FOREGROUND -> {
                        inForegroundScope = MainScope()
                    }
                    PmLifecycle.Event.ON_BACKGROUND -> {
                        inForegroundScope?.cancel()
                        inForegroundScope = null
                    }
                    PmLifecycle.Event.ON_DESTROY -> {
                        scope.cancel()
                        parent?.stateHandler?.removeSaver(tag)
                    }
                }
            }
        })
    }
}