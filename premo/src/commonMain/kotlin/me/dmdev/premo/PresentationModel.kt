/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020-2023 Dmitriy Gorbunov (dmitriy.goto@gmail.com)
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
import me.dmdev.premo.PmLifecycle.Event.ON_BACKGROUND
import me.dmdev.premo.PmLifecycle.Event.ON_DESTROY
import me.dmdev.premo.PmLifecycle.Event.ON_FOREGROUND
import me.dmdev.premo.PmLifecycle.State.DESTROYED
import kotlin.random.Random

abstract class PresentationModel(params: PmParams) {

    private val pmFactory: PmFactory = params.factory
    private val pmStateSaverFactory = params.stateSaverFactory

    val tag: String = params.tag
    val description: PmDescription = params.description
    val parent: PresentationModel? = params.parent
    val lifecycle: PmLifecycle = PmLifecycle()
    val scope: CoroutineScope = MainScope()
    var inForegroundScope: CoroutineScope? = null
        private set
    val messageHandler: PmMessageHandler = PmMessageHandler(parent?.messageHandler)
    val stateHandler: PmStateHandler = PmStateHandler(pmStateSaverFactory.createPmStateSaver(tag))

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

    @Suppress("FunctionName", "UNCHECKED_CAST")
    fun <PM : PresentationModel> Child(
        description: PmDescription,
        key: String = Random.Default.nextLong().toString()
    ): PM {
        val tag = if (key.contains(tag)) key else "$tag/$key"
        val config = PmParams(
            tag = tag,
            parent = this,
            description = description,
            factory = pmFactory,
            stateSaverFactory = pmStateSaverFactory
        )

        return pmFactory.createPm(config) as PM
    }

    @Suppress("FunctionName")
    fun <PM : PresentationModel> AttachedChild(
        description: PmDescription,
        key: String
    ): PM {
        return Child<PM>(description, key).also {
            attachChild(it)
        }
    }

    private fun initSaver() {
        parent?.stateHandler?.setSaver(tag) {
            stateHandler.saveState()
        }
    }

    private fun subscribeToLifecycle() {
        lifecycle.addObserver { lifecycle, event ->
            attachedChildren.forEach { pm ->
                pm.lifecycle.moveTo(lifecycle.state)
            }

            when (event) {
                ON_FOREGROUND -> {
                    inForegroundScope = MainScope()
                }
                ON_BACKGROUND -> {
                    inForegroundScope?.cancel()
                    inForegroundScope = null
                }
                ON_DESTROY -> {
                    scope.cancel()
                    parent?.stateHandler?.removeSaver(tag)
                }
            }
        }
    }
}

fun PresentationModel.attachToParent() {
    parent?.attachChild(this)
}

fun PresentationModel.detachFromParent() {
    parent?.detachChild(this)
}
