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

package me.dmdev.premo.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.dmdev.premo.PmLifecycle
import me.dmdev.premo.PmLifecycle.State.*
import me.dmdev.premo.PresentationModel

interface SetNavigation {
    val values: List<PresentationModel>
    val current: PresentationModel
    val currentState: StateFlow<PresentationModel>
}

interface SetNavigator : SetNavigation {
    fun setCurrent(pm: PresentationModel)
}

@Suppress("FunctionName")
fun PresentationModel.SetNavigator(
    vararg pms: PresentationModel,
    key: String = "set_navigator"
): SetNavigator {
    val indexKey = "${key}_index"
    val navigator = SetNavigatorImpl(lifecycle, pms.toList())
    val savedIndex: Int? = stateHandler.getSaved(indexKey)
    if (savedIndex != null && savedIndex >= 0) {
        navigator.setCurrent(navigator.values[savedIndex])
    }
    stateHandler.setSaver(indexKey) {
        navigator.values.indexOf(navigator.current)
    }
    return navigator
}

class SetNavigatorImpl(
    private val lifecycle: PmLifecycle,
    override val values: List<PresentationModel>,
) : SetNavigator {

    private val _currentState = MutableStateFlow(values.first())
    override val currentState = _currentState.asStateFlow()
    override val current = currentState.value

    init {
        subscribeToLifecycle()
    }

    override fun setCurrent(pm: PresentationModel) {
        _currentState.value.lifecycle.moveTo(CREATED)
        pm.lifecycle.moveTo(lifecycle.state)
        _currentState.value = pm
    }

    private fun subscribeToLifecycle() {
        current.lifecycle.moveTo(lifecycle.state)
        lifecycle.addObserver(object : PmLifecycle.Observer {
            override fun onLifecycleChange(lifecycle: PmLifecycle, event: PmLifecycle.Event) {
                when (lifecycle.state) {
                    CREATED,
                    DESTROYED -> {
                        values.forEach { pm ->
                            pm.lifecycle.moveTo(lifecycle.state)
                        }
                    }
                    IN_FOREGROUND -> {
                        current.lifecycle.moveTo(lifecycle.state)
                    }
                }
            }
        })
    }
}