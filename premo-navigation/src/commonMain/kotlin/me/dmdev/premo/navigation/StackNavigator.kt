/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020-2022 Dmitriy Gorbunov (dmitriy.goto@gmail.com)
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

package me.dmdev.premo.navigation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import me.dmdev.premo.*
import me.dmdev.premo.PmLifecycle.State.*

interface StackNavigator : StackNavigation {
    fun changeBackStack(pmList: List<PresentationModel>)
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

fun PresentationModel.StackNavigator(
    initialDescription: PmDescription? = null,
    key: String = "stack_navigator",
    initHandlers: PmMessageHandler.(navigator: StackNavigator) -> Unit = {}
): StackNavigator {
    val navigator = StackNavigatorImpl(lifecycle, scope)
    val savedBackStack: List<PresentationModel> =
        stateHandler.getSaved<List<Pair<PmDescription, String>>>(key)
            ?.map { (description, tag) -> Child(description, tag) }
            ?: listOf()
    if (savedBackStack.isNotEmpty()) {
        navigator.changeBackStack(savedBackStack)
    } else if (initialDescription != null) {
        navigator.push(Child(initialDescription))
    }
    stateHandler.setSaver(key) {
        navigator.backStack.map { pm -> Pair(pm.description, pm.tag) }
    }
    messageHandler.handle<BackMessage> { navigator.handleBack() }
    messageHandler.initHandlers(navigator)
    return navigator
}

internal class StackNavigatorImpl(
    private val lifecycle: PmLifecycle,
    private val scope: CoroutineScope
) : StackNavigator {

    private val _backStackFlow = MutableStateFlow<List<PresentationModel>>(listOf())
    override val backStackFlow: StateFlow<List<PresentationModel>> = _backStackFlow

    override var backStack: List<PresentationModel>
        get() = backStackFlow.value
        private set(value) {
            _backStackFlow.value = value
        }

    override val currentTop: PresentationModel?
        get() = backStack.lastOrNull()

    override val currentTopFlow: StateFlow<PresentationModel?> =
        backStackFlow
            .map { it.lastOrNull() }
            .stateIn(
                scope = scope,
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

            val pmStackChange = if (newTopPm != null && oldTopPm != null) {
                when {
                    oldTopPm === newTopPm -> {
                        BackStackChange.Set(newTopPm)
                    }
                    oldPmStack.any { it === newTopPm } -> {
                        BackStackChange.Pop(newTopPm, oldTopPm)
                    }
                    else -> {
                        BackStackChange.Push(newTopPm, oldTopPm)
                    }
                }
            } else if (newTopPm != null) {
                BackStackChange.Set(newTopPm)
            } else {
                BackStackChange.Nothing
            }

            emit(pmStackChange)
            oldPmStack = newPmStack
        }
    }

    override fun changeBackStack(pmList: List<PresentationModel>) {
        val iterator = pmList.listIterator()
        while (iterator.hasNext()) {
            val pm = iterator.next()
            if (iterator.hasNext()) {
                pm.lifecycle.moveTo(CREATED)
            } else {
                pm.lifecycle.moveTo(lifecycle.state)
            }
        }
        backStack.forEach { pm ->
            if (pmList.contains(pm).not()) {
                pm.lifecycle.moveTo(DESTROYED)
            }
        }
        backStack = pmList
    }

    private fun subscribeToLifecycle() {
        lifecycle.addObserver { lifecycle, event ->
            when (lifecycle.state) {
                CREATED,
                DESTROYED -> {
                    backStack.forEach { pm ->
                        pm.lifecycle.moveTo(lifecycle.state)
                    }
                }

                IN_FOREGROUND -> {
                    currentTop?.lifecycle?.moveTo(lifecycle.state)
                }
            }
        }
    }
}