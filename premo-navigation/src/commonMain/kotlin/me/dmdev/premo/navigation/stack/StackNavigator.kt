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

package me.dmdev.premo.navigation.stack

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.dmdev.premo.PmLifecycle.State.CREATED
import me.dmdev.premo.PmLifecycle.State.DESTROYED
import me.dmdev.premo.PmLifecycle.State.IN_FOREGROUND
import me.dmdev.premo.PmMessageHandler
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.SaveableFlow
import me.dmdev.premo.annotation.ExperimentalPremoApi
import me.dmdev.premo.handle
import me.dmdev.premo.navigation.BackMessage

interface StackNavigator : StackNavigation {
    fun changeBackStack(pms: List<PresentationModel>)
}

fun StackNavigator.push(pm: PresentationModel) {
    changeBackStack(backStack.plus(pm))
}

fun StackNavigator.pop(): Boolean {
    if (backStack.isEmpty()) return false
    changeBackStack(backStack.dropLast(1))
    return true
}

fun StackNavigator.popToRoot(): Boolean {
    if (backStack.isEmpty()) return false
    changeBackStack(backStack.subList(0, 1))
    return true
}

fun StackNavigator.popUntil(predicate: (PresentationModel) -> Boolean) {
    changeBackStack(backStack.takeWhile(predicate))
}

fun StackNavigator.replaceTop(pm: PresentationModel) {
    changeBackStack(backStack.dropLast(1).plus(pm))
}

fun StackNavigator.replaceAll(pm: PresentationModel) {
    changeBackStack(listOf(pm))
}

fun StackNavigator.handleBack(): Boolean {
    if (backStack.size > 1) {
        pop()
        return true
    }
    return false
}

fun PresentationModel.StackNavigator(
    initBackStack: () -> List<PresentationModel> = { listOf() },
    key: String = DEFAULT_STACK_NAVIGATOR_KEY,
    backHandler: (navigator: StackNavigator) -> Boolean = DEFAULT_STACK_NAVIGATOR_BACK_HANDLER,
    initHandlers: PmMessageHandler.(navigator: StackNavigator) -> Unit = {}
): StackNavigator {
    val navigator = StackNavigatorImpl(
        hostPm = this,
        initBackStack = initBackStack,
        key = key
    )
    messageHandler.handle<BackMessage> { backHandler.invoke(navigator) }
    messageHandler.initHandlers(navigator)
    return navigator
}

internal const val DEFAULT_STACK_NAVIGATOR_KEY = "stack_navigator"
internal const val DEFAULT_STACK_NAVIGATOR_BACKSTACK_STATE_KEY =
    "${DEFAULT_STACK_NAVIGATOR_KEY}_backstack"
internal val DEFAULT_STACK_NAVIGATOR_BACK_HANDLER: (StackNavigator) -> Boolean = { it.handleBack() }

internal class StackNavigatorImpl(
    private val hostPm: PresentationModel,
    initBackStack: () -> List<PresentationModel> = { listOf() },
    key: String
) : StackNavigator {

    private val _backStackFlow: MutableStateFlow<List<PresentationModel>> =
        hostPm.stateHandler.SaveableFlow(
            key = "${key}_backstack",
            initialValueProvider = { initBackStack() },
            saveTypeMapper = { backStack -> backStack.map { it.pmArgs } },
            restoreTypeMapper = { backStack -> backStack.map { hostPm.Child(it) } }
        )

    override val backStackFlow: StateFlow<List<PresentationModel>> = _backStackFlow

    override var backStack: List<PresentationModel>
        get() = backStackFlow.value
        private set(value) {
            _backStackFlow.value = value
        }

    override val currentTopFlow: StateFlow<PresentationModel?> =
        backStackFlow
            .map { it.lastOrNull() }
            .stateIn(
                scope = hostPm.scope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = null
            )

    init {
        subscribeToLifecycle()
    }

    @ExperimentalPremoApi
    override val backStackChangesFlow: Flow<BackStackChange> = flow {
        var oldPmStack: List<PresentationModel> = backStackFlow.value
        backStackFlow.collect { newPmStack ->

            val oldTopPm = oldPmStack.lastOrNull()
            val newTopPm = newPmStack.lastOrNull()
            val removedPms = oldPmStack.filter { newPmStack.contains(it).not() }

            val pmStackChange = if (newTopPm != null && oldTopPm != null) {
                when {
                    oldTopPm === newTopPm -> {
                        BackStackChange.Set(newTopPm, removedPms)
                    }

                    oldPmStack.any { it === newTopPm } -> {
                        BackStackChange.Pop(newTopPm, oldTopPm, removedPms)
                    }

                    else -> {
                        BackStackChange.Push(newTopPm, oldTopPm, removedPms)
                    }
                }
            } else if (newTopPm != null) {
                BackStackChange.Set(newTopPm, removedPms)
            } else {
                BackStackChange.Nothing
            }

            emit(pmStackChange)
            oldPmStack = newPmStack
        }
    }

    override fun changeBackStack(pms: List<PresentationModel>) {
        val iterator = pms.listIterator()
        while (iterator.hasNext()) {
            val pm = iterator.next()
            if (iterator.hasNext()) {
                pm.lifecycle.moveTo(CREATED)
            } else {
                pm.lifecycle.moveTo(hostPm.lifecycle.state)
            }
        }
        backStack.forEach { pm ->
            if (pms.contains(pm).not()) {
                pm.lifecycle.moveTo(DESTROYED)
            }
        }
        backStack = pms
    }

    private fun subscribeToLifecycle() {
        hostPm.lifecycle.addObserver { _, newState ->
            when (newState) {
                CREATED,
                DESTROYED -> {
                    backStack.forEach { pm ->
                        pm.lifecycle.moveTo(newState)
                    }
                }

                IN_FOREGROUND -> {
                    currentTop?.lifecycle?.moveTo(newState)
                }
            }
        }
    }
}
