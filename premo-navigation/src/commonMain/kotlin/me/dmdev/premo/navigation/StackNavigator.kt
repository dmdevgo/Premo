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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.dmdev.premo.ExperimentalPremoApi
import me.dmdev.premo.PmDescription
import me.dmdev.premo.PmLifecycle
import me.dmdev.premo.PmLifecycle.State.CREATED
import me.dmdev.premo.PmLifecycle.State.DESTROYED
import me.dmdev.premo.PmLifecycle.State.IN_FOREGROUND
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.getSaved
import me.dmdev.premo.setSaver

interface StackNavigator : StackNavigation {
    fun setBackStack(pmList: List<PresentationModel>)
}

fun StackNavigator.push(pm: PresentationModel) {
    setBackStack(backstack.plus(pm))
}

fun StackNavigator.pop(): Boolean {
    if (backstack.isEmpty()) return false
    setBackStack(backstack.dropLast(1))
    return true
}

fun PresentationModel.StackNavigator(
    initialDescription: PmDescription? = null,
    key: String = "stack_navigator"
): StackNavigator {
    val navigator = StackNavigatorImpl(lifecycle, scope)
    stateHandler.setSaver(key) {
        navigator.backstack.map { pm -> Pair(pm.description, pm.tag) }
    }
    val savedBackStack: List<PresentationModel> =
        stateHandler.getSaved<List<Pair<PmDescription, String>>>(key)
            ?.map { (description, tag) -> Child(description, tag) }
            ?: listOf()
    if (savedBackStack.isNotEmpty()) {
        navigator.setBackStack(savedBackStack)
    } else if (initialDescription != null) {
        navigator.push(Child(initialDescription))
    }
    return navigator
}

internal class StackNavigatorImpl(
    private val lifecycle: PmLifecycle,
    private val scope: CoroutineScope
) : StackNavigator {

    private val _backstackState = MutableStateFlow<List<PresentationModel>>(listOf())
    override val backstackState: StateFlow<List<PresentationModel>> = _backstackState

    private var backstack: List<PresentationModel>
        get() = backstackState.value
        private set(value) {
            _backstackState.value = value
        }

    override val currentTopState: StateFlow<PresentationModel?> =
        backstackState.map { it.lastOrNull() }.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    init {
        subscribeToLifecycle()
    }

    @ExperimentalPremoApi
    override val backstackChanges: Flow<BackstackChange> = flow {
        var oldPmStack: List<PresentationModel> = backstackState.value
        _backstackState.collect { newPmStack ->

            val oldTopPm = oldPmStack.lastOrNull()
            val newTopPm = newPmStack.lastOrNull()

            val pmStackChange = if (newTopPm != null && oldTopPm != null) {
                when {
                    oldTopPm === newTopPm -> {
                        BackstackChange.Set(newTopPm)
                    }
                    oldPmStack.any { it === newTopPm } -> {
                        BackstackChange.Pop(newTopPm, oldTopPm)
                    }
                    else -> {
                        BackstackChange.Push(newTopPm, oldTopPm)
                    }
                }
            } else if (newTopPm != null) {
                BackstackChange.Set(newTopPm)
            } else {
                BackstackChange.Empty
            }

            emit(pmStackChange)
            oldPmStack = newPmStack
        }
    }

    override fun setBackStack(pmList: List<PresentationModel>) {
        val iterator = pmList.listIterator()
        while (iterator.hasNext()) {
            val pm = iterator.next()
            if (iterator.hasNext()) {
                pm.lifecycle.moveTo(CREATED)
            } else {
                pm.lifecycle.moveTo(lifecycle.state)
            }
        }
        backstack.forEach { pm ->
            if (pmList.contains(pm).not()) {
                pm.lifecycle.moveTo(DESTROYED)
            }
        }
        backstack = pmList
    }

    private fun subscribeToLifecycle() {
        lifecycle.addObserver(object : PmLifecycle.Observer {
            override fun onLifecycleChange(lifecycle: PmLifecycle, event: PmLifecycle.Event) {
                when (lifecycle.state) {
                    CREATED,
                    DESTROYED -> {
                        backstack.forEach { pm ->
                            pm.lifecycle.moveTo(lifecycle.state)
                        }
                    }

                    IN_FOREGROUND -> {
                        currentTop?.lifecycle?.moveTo(lifecycle.state)
                    }
                }
            }
        })
    }
}