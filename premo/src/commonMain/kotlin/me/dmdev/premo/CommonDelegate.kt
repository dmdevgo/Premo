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

package me.dmdev.premo

import me.dmdev.premo.LifecycleEvent.*

/**
 *  Common delegate serves for forwarding the lifecycle[LifecycleEvent] directly into the [PresentationModel][PresentationModel].
 *  Can be used to implement your delegate for the View[PmView].
 *
 *  @see PmActivityDelegate
 *  @see PmFragmentDelegate
 *  @see PmControllerDelegate
 */
class CommonDelegate<PM, V>(
    private val pmView: PmView<PM>
)
        where PM : PresentationModel,
              V : PmView<PM> {

    private lateinit var pmTag: String

    val presentationModel: PM by lazy(LazyThreadSafetyMode.NONE) {
        @Suppress("UNCHECKED_CAST")
        PmStore.getPm(pmTag) { pmView.providePresentationModel() } as PM
    }

    fun onCreate(pmTag: String) {
        this.pmTag = pmTag
        if (presentationModel.lifecycleState.value == LifecycleState.INITIALIZED) {
            presentationModel.lifecycleEvent.tryEmit(ON_CREATE)
        }
    }

    fun onForeground() {
        presentationModel.lifecycleEvent.tryEmit(ON_FOREGROUND)
        pmView.onBindPresentationModel(presentationModel)
    }

    fun onBackground() {
        presentationModel.lifecycleEvent.tryEmit(ON_BACKGROUND)
        pmView.onUnbindPresentationModel()
    }

    fun onDestroy() {
        PmStore.removePm(pmTag)
        presentationModel.lifecycleEvent.tryEmit(ON_DESTROY)
    }
}