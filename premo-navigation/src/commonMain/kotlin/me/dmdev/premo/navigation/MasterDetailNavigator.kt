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
import me.dmdev.premo.PmMessageHandler
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.attachToParent
import me.dmdev.premo.detachFromParent
import me.dmdev.premo.getSaved
import me.dmdev.premo.handle
import me.dmdev.premo.setSaver

interface MasterDetailNavigator<M, D> : MasterDetailNavigation<M, D>
        where M : PresentationModel,
              D : PresentationModel {

    fun changeDetail(detail: D?)
}

@Suppress("FunctionName")
fun <M : PresentationModel, D : PresentationModel> PresentationModel.MasterDetailNavigator(
    masterPmDescription: PmDescription,
    key: String = "master_detail_navigator",
    initHandlers: PmMessageHandler.(navigator: MasterDetailNavigator<M, D>) -> Unit = {}
): MasterDetailNavigator<M, D> {
    val navigator = MasterDetailNavigatorImpl<M, D>(
        Child(masterPmDescription)
    )
    val detailKey = "${key}_detail_pm"
    val savedDetail = stateHandler.getSaved<PmDescription?>(detailKey)
    if (savedDetail != null) {
        navigator.changeDetail(Child(savedDetail))
    }
    stateHandler.setSaver(detailKey) {
        navigator.detail?.description
    }
    messageHandler.initHandlers(navigator)
    messageHandler.handle<BackMessage> {
        navigator.handleBack()
    }
    messageHandler.initHandlers(navigator)
    return navigator
}

internal class MasterDetailNavigatorImpl<M, D>(
    override val master: M
) : MasterDetailNavigator<M, D>
        where M : PresentationModel,
              D : PresentationModel {

    init {
        master.attachToParent()
    }

    private val _detailFlow: MutableStateFlow<D?> = MutableStateFlow(null)
    override val detailFlow: StateFlow<D?> = _detailFlow.asStateFlow()

    override val detail: D? get() = detailFlow.value

    override fun changeDetail(detail: D?) {
        this.detail?.detachFromParent()
        _detailFlow.value = detail
        detail?.attachToParent()
    }
}
