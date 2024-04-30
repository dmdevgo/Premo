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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.SaveableFlow
import me.dmdev.premo.attachToParent
import me.dmdev.premo.destroy
import me.dmdev.premo.detachFromParent
import me.dmdev.premo.handle
import me.dmdev.premo.navigation.BackMessage

interface SetNavigator : SetNavigation {
    fun changeCurrent(index: Int)
    fun changeCurrent(pm: PresentationModel)
    fun changeValues(values: List<PresentationModel>, currentIndex: Int = 0)
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
    backHandler: (navigator: SetNavigator) -> Boolean = { it.handleBack() },
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
internal const val DEFAULT_SET_NAVIGATOR_STATE_KEY = "${DEFAULT_SET_NAVIGATOR_KEY}_state"
internal val DEFAULT_SET_NAVIGATOR_ON_CHANGE_CURRENT: (index: Int, navigator: SetNavigator) -> Unit =
    { index, navigator -> navigator.changeCurrent(index) }

internal class SetNavigatorImpl(
    private val hostPm: PresentationModel,
    initValues: () -> List<PresentationModel>,
    key: String,
    private val onChangeCurrent: (index: Int, navigator: SetNavigator) -> Unit
) : SetNavigator {

    private val state: MutableStateFlow<Pair<List<PresentationModel>, PresentationModel?>> =
        hostPm.SaveableFlow(
            key = "${key}_state",
            initialValueProvider = {
                val values = initValues()
                val current = values.firstOrNull()
                current?.attachToParent()
                Pair(values, current)
            },
            saveTypeMapper = { (values, pm) ->
                Pair(values.map { it.pmArgs }, values.indexOf(pm))
            },
            restoreTypeMapper = { (pmArgs, index) ->
                val values = pmArgs.map { hostPm.Child<PresentationModel>(it) }
                val current = if (index >= 0) values[index] else null
                current?.attachToParent()
                Pair(values, current)
            }
        )

    override val valuesFlow: StateFlow<List<PresentationModel>> =
        state.map { it.first }.stateIn(
            scope = hostPm.scope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = state.value.first
        )

    override val values: List<PresentationModel>
        get() = state.value.first

    override val currentFlow: StateFlow<PresentationModel?> =
        state.map { it.second }.stateIn(
            scope = hostPm.scope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = state.value.second
        )

    override val current: PresentationModel?
        get() = state.value.second

    override fun changeCurrent(index: Int) {
        val pm = values[index]
        if (pm === current) return

        current?.detachFromParent()
        pm.attachToParent()
        state.value = state.value.copy(second = pm)
    }

    override fun changeCurrent(pm: PresentationModel) {
        changeCurrent(values.indexOf(pm))
    }

    override fun changeValues(values: List<PresentationModel>, currentIndex: Int) {
        val current = if (values.isNotEmpty()) values[currentIndex] else null

        for (pm in values.iterator()) {
            if (pm != current) {
                pm.detachFromParent()
            } else {
                pm.attachToParent()
            }
        }

        this.values.forEach { pm ->
            if (values.contains(pm).not()) {
                pm.destroy()
            }
        }

        state.value = Pair(values, current)
    }

    override fun onChangeCurrent(index: Int) {
        onChangeCurrent(index, this)
    }

    override fun onChangeCurrent(pm: PresentationModel) {
        onChangeCurrent(values.indexOf(pm), this)
    }
}
