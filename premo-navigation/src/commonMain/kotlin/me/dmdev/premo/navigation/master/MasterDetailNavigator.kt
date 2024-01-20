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

package me.dmdev.premo.navigation.master

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.dmdev.premo.AttachedChild
import me.dmdev.premo.PmArgs
import me.dmdev.premo.PmMessageHandler
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.SaveableFlow
import me.dmdev.premo.attachToParent
import me.dmdev.premo.detachFromParent
import me.dmdev.premo.handle
import me.dmdev.premo.navigation.BackMessage
import kotlin.reflect.typeOf

interface MasterDetailNavigator<M, D> : MasterDetailNavigation<M, D>
        where M : PresentationModel,
              D : PresentationModel {

    fun changeDetail(detail: D?)
}

fun MasterDetailNavigator<*, *>.handleBack(): Boolean {
    if (detail != null) {
        changeDetail(null)
        return true
    }
    return false
}

fun <M : PresentationModel, D : PresentationModel> PresentationModel.MasterDetailNavigator(
    masterPm: M,
    key: String = DEFAULT_MASTER_DETAIL_NAVIGATOR_KEY,
    backHandler: (MasterDetailNavigator<M, D>) -> Boolean = DEFAULT_MASTER_DETAIL_NAVIGATOR_BACK_HANDLER,
    initHandlers: PmMessageHandler.(navigator: MasterDetailNavigator<M, D>) -> Unit = {}
): MasterDetailNavigator<M, D> {
    val navigator = MasterDetailNavigatorImpl<M, D>(
        hostPm = this,
        masterPm = masterPm,
        key = key
    )
    messageHandler.handle<BackMessage> { backHandler.invoke(navigator) }
    messageHandler.initHandlers(navigator)
    return navigator
}

internal const val DEFAULT_MASTER_DETAIL_NAVIGATOR_KEY = "master_detail_navigator"
internal const val DEFAULT_MASTER_DETAIL_NAVIGATOR_DETAIL_STATE_KEY =
    "${DEFAULT_MASTER_DETAIL_NAVIGATOR_KEY}_detail_pm"
internal val DEFAULT_MASTER_DETAIL_NAVIGATOR_BACK_HANDLER: (MasterDetailNavigator<*, *>) -> Boolean =
    { it.handleBack() }

internal class MasterDetailNavigatorImpl<M, D>(
    private val hostPm: PresentationModel,
    masterPm: M,
    key: String
) : MasterDetailNavigator<M, D>
        where M : PresentationModel,
              D : PresentationModel {

    override val master: M = masterPm

    init {
        masterPm.attachToParent()
    }

    private val _detailFlow: MutableStateFlow<D?> = hostPm.SaveableFlow(
        key = "${key}_detail_pm",
        initialValueProvider = { null },
        saveType = typeOf<PmArgs>(),
        saveTypeMapper = { it?.pmArgs },
        restoreTypeMapper = { it?.let { hostPm.AttachedChild(it) as D } }
    )
    override val detailFlow: StateFlow<D?> = _detailFlow.asStateFlow()

    override fun changeDetail(detail: D?) {
        this.detail?.detachFromParent()
        _detailFlow.value = detail
        detail?.attachToParent()
    }
}
