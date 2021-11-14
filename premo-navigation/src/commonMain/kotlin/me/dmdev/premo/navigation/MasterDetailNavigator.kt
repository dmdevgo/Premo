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
import me.dmdev.premo.*


interface MasterDetailNavigator<M, D> : MasterDetailNavigation<M, D>
        where M : PresentationModel,
              D : PresentationModel {

    fun setDetail(detailPm: D?)
}

@Suppress("FunctionName")
fun <M : PresentationModel, D : PresentationModel> PresentationModel.MasterDetailNavigator(
    masterPmDescription: PmDescription,
    key: String = "master_detail_navigator"
): MasterDetailNavigator<M, D> {
    val masterKey = "${key}_master_pm"
    val navigator = MasterDetailNavigatorImpl<M, D>(
        Child(masterPmDescription, masterKey)
    )
    val detailKey = "${key}_detail_pm"
    val savedDetail = stateHandler.getSaved<Pair<PmDescription, String>>(detailKey)
    if (savedDetail != null) {
        navigator.setDetail(Child(savedDetail.first, savedDetail.second))
    }
    stateHandler.setSaver(detailKey) {
        navigator.detailPm?.let { detailPm ->
            Pair(detailPm.description, detailPm.tag)
        }
    }
    return navigator
}

internal class MasterDetailNavigatorImpl<M, D>(
    override val masterPm: M
) : MasterDetailNavigator<M, D>
        where M : PresentationModel,
              D : PresentationModel {

    init {
        masterPm.attachToParent()
    }

    private val _detailPmState: MutableStateFlow<D?> = MutableStateFlow(null)
    override val detailPmState: StateFlow<D?> = _detailPmState.asStateFlow()

    override val detailPm: D? get() = detailPmState.value

    override fun setDetail(detailPm: D?) {
        this.detailPm?.detachFromParent()
        _detailPmState.value = detailPm
        detailPm?.attachToParent()
    }
}