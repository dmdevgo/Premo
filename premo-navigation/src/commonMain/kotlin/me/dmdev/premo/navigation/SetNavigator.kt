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

package me.dmdev.premo.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.dmdev.premo.PmDescription
import me.dmdev.premo.PmLifecycle
import me.dmdev.premo.PmLifecycle.State.CREATED
import me.dmdev.premo.PmLifecycle.State.DESTROYED
import me.dmdev.premo.PmLifecycle.State.IN_FOREGROUND
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.getSaved
import me.dmdev.premo.handle
import me.dmdev.premo.setSaver

interface SetNavigator : SetNavigation {
    fun changeCurrent(index: Int)
    fun changeCurrent(pm: PresentationModel)
    fun setValues(pmList: List<PresentationModel>)
}

fun PresentationModel.SetNavigator(
    vararg pmDescriptions: PmDescription,
    key: String = "set_navigator",
    onChangeCurrent: (index: Int, navigator: SetNavigator) -> Unit = { index, navigator ->
        navigator.changeCurrent(index)
    }
): SetNavigator {
    val indexKey = "${key}_index"

    val savedValues: List<PresentationModel> =
        stateHandler.getSaved<List<PmDescription>>(key)?.map { Child(it) } ?: listOf()

    val navigator = if (savedValues.isNotEmpty()) {
        SetNavigatorImpl(lifecycle, savedValues, onChangeCurrent)
    } else {
        val pms = pmDescriptions.map { Child<PresentationModel>(it) }
        SetNavigatorImpl(lifecycle, pms.toList(), onChangeCurrent)
    }

    val savedIndex: Int? = stateHandler.getSaved(indexKey)
    if (savedIndex != null && savedIndex >= 0) {
        navigator.changeCurrent(savedIndex)
    }
    stateHandler.setSaver(indexKey) {
        navigator.values.indexOf(navigator.current)
    }
    stateHandler.setSaver(key) {
        navigator.values.map { it.description }
    }
    messageHandler.handle<BackMessage> { navigator.handleBack() }
    return navigator
}

class SetNavigatorImpl(
    private val lifecycle: PmLifecycle,
    values: List<PresentationModel>,
    private val onChangeCurrent: (index: Int, navigator: SetNavigator) -> Unit
) : SetNavigator {

    private val _valuesFlow = MutableStateFlow(values)
    override val valuesFlow: StateFlow<List<PresentationModel>> = _valuesFlow.asStateFlow()
    override val values: List<PresentationModel> get() = valuesFlow.value

    private val _currentFlow = MutableStateFlow(values.firstOrNull())
    override val currentFlow = _currentFlow.asStateFlow()

    override val current: PresentationModel? get() = currentFlow.value

    init {
        subscribeToLifecycle()
    }

    override fun changeCurrent(index: Int) {
        val pm = values[index]
        if (pm === _currentFlow.value) return
        _currentFlow.value?.lifecycle?.moveTo(CREATED)
        pm.lifecycle.moveTo(lifecycle.state)
        _currentFlow.value = pm
    }

    override fun changeCurrent(pm: PresentationModel) {
        changeCurrent(values.indexOf(pm))
    }

    override fun setValues(pmList: List<PresentationModel>) {
        val iterator = pmList.listIterator()
        while (iterator.hasNext()) {
            val pm = iterator.next()
            pm.lifecycle.moveTo(CREATED)
        }
        values.forEach { pm ->
            if (pmList.contains(pm).not()) {
                pm.lifecycle.moveTo(DESTROYED)
            }
        }
        _currentFlow.value = if (pmList.contains(current)) current else pmList.first()
        current?.lifecycle?.moveTo(lifecycle.state)
        _valuesFlow.value = pmList
    }

    override fun onChangeCurrent(index: Int) {
        onChangeCurrent(index, this)
    }

    override fun onChangeCurrent(pm: PresentationModel) {
        onChangeCurrent(values.indexOf(pm), this)
    }

    private fun subscribeToLifecycle() {
        current?.lifecycle?.moveTo(lifecycle.state)
        lifecycle.addObserver { lifecycle, event ->
            when (lifecycle.state) {
                CREATED,
                DESTROYED -> {
                    values.forEach { pm ->
                        pm.lifecycle.moveTo(lifecycle.state)
                    }
                }
                IN_FOREGROUND -> {
                    current?.lifecycle?.moveTo(lifecycle.state)
                }
            }
        }
    }
}
