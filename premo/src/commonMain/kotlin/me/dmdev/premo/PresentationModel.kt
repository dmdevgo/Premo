/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020-2024 Dmitriy Gorbunov (dmitriy.goto@gmail.com)
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
import kotlinx.coroutines.flow.MutableStateFlow
import me.dmdev.premo.PmLifecycle.State.CREATED
import me.dmdev.premo.PmLifecycle.State.DESTROYED
import me.dmdev.premo.PmLifecycle.State.IN_FOREGROUND
import me.dmdev.premo.annotation.DelicatePremoApi
import me.dmdev.premo.saver.PmStateSaverFactory
import kotlin.reflect.KType
import kotlin.reflect.typeOf

abstract class PresentationModel(
    val pmArgs: PmArgs
) {

    internal val pmFactory: PmFactory = pmArgs.pmFactory
    internal val pmStateSaverFactory: PmStateSaverFactory = pmArgs.pmStateSaverFactory

    var parent: PresentationModel? = pmArgs.parent
        private set

    val tag: String = pmArgs.parent.let { parent ->
        if (parent != null) {
            "${parent.tag}/${pmArgs.key}"
        } else {
            pmArgs.key
        }
    }

    val lifecycle: PmLifecycle = PmLifecycle()
    val scope: CoroutineScope = MainScope()
    var inForegroundScope: CoroutineScope? = null
        private set

    @Suppress("LeakingThis")
    val messageHandler: PmMessageHandler = PmMessageHandler(this)

    @Suppress("LeakingThis")
    val stateHandler: PmStateHandler = PmStateHandler(this)

    internal val allChildren = mutableListOf<PresentationModel>()
    val children: List<PresentationModel> get() = allChildren.toList()

    private val _attachedChildren = mutableListOf<PresentationModel>()
    val attachedChildren: List<PresentationModel> get() = _attachedChildren.toList()

    init {
        subscribeToLifecycle()
    }

    @DelicatePremoApi
    fun attachChild(pm: PresentationModel) {
        if (pm.lifecycle.isDestroyed) {
            throw IllegalArgumentException("Destroyed presentation model cannot be attached to the parent.")
        }

        if (_attachedChildren.contains(pm)) {
            throw IllegalArgumentException("${pm::class.qualifiedName} is already attached to the parent it parent.")
        }

        if (pm.parent != this) {
            throw IllegalArgumentException("The presentation model must be attached only to its parent.")
        }

        pm.lifecycle.moveTo(lifecycle.state)
        _attachedChildren.add(pm)
    }

    @DelicatePremoApi
    fun detachChild(pm: PresentationModel) {
        pm.lifecycle.moveTo(DESTROYED)
        removeChild(pm)
    }

    @Suppress("FunctionName", "UNCHECKED_CAST")
    fun <PM : PresentationModel> Child(args: PmArgs): PM {
        if (lifecycle.isDestroyed) {
            throw IllegalArgumentException("A child can not be created for a destroyed presentation model.")
        }

        if (allChildren.any { it.pmArgs.key == args.key }) {
            throw IllegalArgumentException("Child presentation model with the key [${args.key}] already exists. The key from args must be unique.")
        }

        args.pmFactory = pmFactory
        args.pmStateSaverFactory = pmStateSaverFactory
        args.parent = this

        val childPm = pmFactory.createPresentationModel(args) as PM
        allChildren.add(childPm)
        return childPm
    }

    @DelicatePremoApi
    fun saveState() {
        if (lifecycle.isDestroyed) return

        stateHandler.saveState()
        allChildren.forEach { it.saveState() }
    }

    private fun removeChild(pm: PresentationModel) {
        allChildren.remove(pm)
        _attachedChildren.remove(pm)
    }

    private fun release() {
        scope.cancel()
        inForegroundScope?.cancel()
        inForegroundScope = null
        messageHandler.release()
        stateHandler.release()
        parent?.removeChild(this)
        parent = null
        pmArgs.parent = null
    }

    private fun subscribeToLifecycle() {
        lifecycle.addObserver { _, newState ->

            attachedChildren.forEach { pm ->
                pm.lifecycle.moveTo(newState)
            }

            when (newState) {
                IN_FOREGROUND -> {
                    inForegroundScope = MainScope()
                }

                CREATED -> {
                    inForegroundScope?.cancel()
                    inForegroundScope = null
                }

                DESTROYED -> {
                    children.forEach { pm ->
                        pm.lifecycle.moveTo(DESTROYED)
                    }
                    release()
                }
            }
        }
    }
}

fun PresentationModel.childrenOf(
    vararg args: PmArgs
): List<PresentationModel> {
    return args.map { Child(it) }
}

@DelicatePremoApi
fun PresentationModel.attachToParent() {
    parent?.attachChild(this)
}

@DelicatePremoApi
fun PresentationModel.detachFromParent() {
    parent?.detachChild(this)
}

@Suppress("FunctionName")
fun <PM : PresentationModel> PresentationModel.AttachedChild(args: PmArgs): PM {
    return Child<PM>(args).also {
        attachChild(it)
    }
}

@Suppress("FunctionName")
inline fun <reified T> PresentationModel.SaveableFlow(
    key: String,
    initialValue: T
): MutableStateFlow<T> {
    return stateHandler.SaveableFlow(
        key = key,
        initialValue = initialValue
    )
}

@Suppress("FunctionName")
inline fun <T, reified S> PresentationModel.SaveableFlow(
    key: String,
    noinline initialValueProvider: () -> T,
    noinline saveTypeMapper: (T) -> S,
    noinline restoreTypeMapper: (S) -> T
): MutableStateFlow<T> {
    return stateHandler.SaveableFlow(
        key = key,
        initialValueProvider = initialValueProvider,
        saveType = typeOf<S>(),
        saveTypeMapper = saveTypeMapper,
        restoreTypeMapper = restoreTypeMapper
    )
}

@Suppress("FunctionName")
fun <T, S> PresentationModel.SaveableFlow(
    key: String,
    initialValueProvider: () -> T,
    saveType: KType,
    saveTypeMapper: (T) -> S,
    restoreTypeMapper: (S) -> T
): MutableStateFlow<T> {
    return stateHandler.SaveableFlow(
        key = key,
        initialValueProvider = initialValueProvider,
        saveType = saveType,
        saveTypeMapper = saveTypeMapper,
        restoreTypeMapper = restoreTypeMapper
    )
}
