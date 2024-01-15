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
import me.dmdev.premo.saver.PmStateSaverFactory
import kotlin.reflect.KType
import kotlin.reflect.typeOf

abstract class PresentationModel(
    val pmArgs: PmArgs
) {

    private val pmFactory: PmFactory = pmArgs.pmFactory
    private val pmStateSaverFactory: PmStateSaverFactory = pmArgs.pmStateSaverFactory

    val parent: PresentationModel? = pmArgs.parent
    val tag: String = if (parent != null) {
        "${parent.tag}/${pmArgs.key}"
    } else {
        pmArgs.key
    }
    val lifecycle: PmLifecycle = PmLifecycle()
    val scope: CoroutineScope = MainScope()
    var inForegroundScope: CoroutineScope? = null
        private set

    val messageHandler: PmMessageHandler = PmMessageHandler(this)
    val stateHandler: PmStateHandler = PmStateHandler(pmStateSaverFactory.createPmStateSaver(tag))

    internal val allChildren = mutableListOf<PresentationModel>()
    private val attachedChildren = mutableListOf<PresentationModel>()

    init {
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
    fun <PM : PresentationModel> Child(args: PmArgs): PM {
        args.pmFactory = pmFactory
        args.pmStateSaverFactory = pmStateSaverFactory
        args.parent = this

        val childPm = pmFactory.createPm(args) as PM
        allChildren.add(childPm)
        return childPm
    }

    fun saveState() {
        stateHandler.saveState()
        allChildren.forEach { it.saveState() }
    }

    private fun removeChild(childPm: PresentationModel) {
        allChildren.remove(childPm)
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
                    scope.cancel()
                    parent?.removeChild(this)
                    pmStateSaverFactory.deletePmStateSaver(tag)
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

fun PresentationModel.attachToParent() {
    parent?.attachChild(this)
}

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
