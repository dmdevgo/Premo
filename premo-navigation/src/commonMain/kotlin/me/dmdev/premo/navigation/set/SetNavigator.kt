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

package me.dmdev.premo.navigation.set

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.dmdev.premo.PmLifecycle.State.CREATED
import me.dmdev.premo.PmLifecycle.State.DESTROYED
import me.dmdev.premo.PmLifecycle.State.IN_FOREGROUND
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.SaveableFlow
import me.dmdev.premo.handle
import me.dmdev.premo.navigation.BackMessage

interface SetNavigator : SetNavigation {
    fun changeCurrent(index: Int)
    fun changeCurrent(pm: PresentationModel)
    fun changeValues(values: List<PresentationModel>)
}

fun SetNavigator.handleBack(): Boolean {
    return if (values.indexOf(current) > 0) {
        changeCurrent(0)
        true
    } else {
        false
    }
}

fun PresentationModel.SetNavigator(
    initValues: () -> List<PresentationModel>,
    key: String = DEFAULT_SET_NAVIGATOR_KEY,
    backHandler: (SetNavigator) -> Boolean = DEFAULT_SET_NAVIGATOR_BACK_HANDLER,
    onChangeCurrent: (index: Int, navigator: SetNavigator) -> Unit = DEFAULT_SET_NAVIGATOR_ON_CHANGE_CURRENT
): SetNavigator {
    val navigator = SetNavigatorImpl(
        hostPm = this,
        initValues = initValues,
        key = key,
        onChangeCurrent = onChangeCurrent
    )
    messageHandler.handle<BackMessage> { backHandler.invoke(navigator) }
    return navigator
}

internal const val DEFAULT_SET_NAVIGATOR_KEY = "set_navigator"
internal const val DEFAULT_SET_NAVIGATOR_STATE_VALUES_KEY = "${DEFAULT_SET_NAVIGATOR_KEY}_values"
internal const val DEFAULT_SET_NAVIGATOR_STATE_CURRENT_INDEX_KEY =
    "${DEFAULT_SET_NAVIGATOR_KEY}_current_index"
internal val DEFAULT_SET_NAVIGATOR_BACK_HANDLER: (SetNavigator) -> Boolean = { it.handleBack() }
internal val DEFAULT_SET_NAVIGATOR_ON_CHANGE_CURRENT: (index: Int, navigator: SetNavigator) -> Unit =
    { index, navigator ->
        navigator.changeCurrent(index)
    }

internal class SetNavigatorImpl(
    private val hostPm: PresentationModel,
    initValues: () -> List<PresentationModel>,
    key: String,
    private val onChangeCurrent: (index: Int, navigator: SetNavigator) -> Unit
) : SetNavigator {

    private val _valuesFlow: MutableStateFlow<List<PresentationModel>> = hostPm.SaveableFlow(
        key = "${key}_values",
        initialValueProvider = { initValues() },
        saveTypeMapper = { values -> values.map { it.pmArgs } },
        restoreTypeMapper = { pmArgs -> pmArgs.map { hostPm.Child(it) } }
    )
    override val valuesFlow: StateFlow<List<PresentationModel>> = _valuesFlow.asStateFlow()

    private val _currentFlow: MutableStateFlow<PresentationModel?> = hostPm.SaveableFlow(
        key = "${key}_current_index",
        initialValueProvider = { values.firstOrNull() },
        saveTypeMapper = { values.indexOf(it) },
        restoreTypeMapper = { values[it] }
    )
    override val currentFlow: StateFlow<PresentationModel?> = _currentFlow.asStateFlow()

    init {
        subscribeToLifecycle()
    }

    override fun changeCurrent(index: Int) {
        val pm = values[index]
        if (pm === _currentFlow.value) return
        _currentFlow.value?.lifecycle?.moveTo(CREATED)
        pm.lifecycle.moveTo(hostPm.lifecycle.state)
        _currentFlow.value = pm
    }

    override fun changeCurrent(pm: PresentationModel) {
        changeCurrent(values.indexOf(pm))
    }

    override fun changeValues(values: List<PresentationModel>) {
        val iterator = values.listIterator()
        while (iterator.hasNext()) {
            val pm = iterator.next()
            pm.lifecycle.moveTo(CREATED)
        }
        this.values.forEach { pm ->
            if (values.contains(pm).not()) {
                pm.lifecycle.moveTo(DESTROYED)
            }
        }
        _currentFlow.value = if (values.contains(current)) current else values.first()
        current?.lifecycle?.moveTo(hostPm.lifecycle.state)
        _valuesFlow.value = values
    }

    override fun onChangeCurrent(index: Int) {
        onChangeCurrent(index, this)
    }

    override fun onChangeCurrent(pm: PresentationModel) {
        onChangeCurrent(values.indexOf(pm), this)
    }

    private fun subscribeToLifecycle() {
        current?.lifecycle?.moveTo(hostPm.lifecycle.state)
        hostPm.lifecycle.addObserver { _, newState ->
            when (newState) {
                CREATED,
                DESTROYED -> {
                    values.forEach { pm ->
                        pm.lifecycle.moveTo(newState)
                    }
                }

                IN_FOREGROUND -> {
                    current?.lifecycle?.moveTo(newState)
                }
            }
        }
    }
}
