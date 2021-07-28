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

import me.dmdev.premo.PmLifecycle.State.*
import me.dmdev.premo.internal.PmStore

/**
 *  Common delegate serves for forwarding the lifecycle[LifecycleEvent] directly into the [PresentationModel][PresentationModel].
 *  Can be used to implement your delegate.
 *
 *  @see PmActivityDelegat
 */
class CommonDelegate<PM : PresentationModel>(
    val pmTag: String,
    val pmProvider: () -> PM
) {

    @Suppress("UNCHECKED_CAST")
    val presentationModel: PM = PmStore.getPm(pmTag) as? PM ?: pmProvider().also { pm ->
        PmStore.putPm(pmTag, pm)
    }

    fun onCreate() {
        presentationModel.lifecycle.moveTo(CREATED)
    }

    fun onForeground() {
        presentationModel.lifecycle.moveTo(IN_FOREGROUND)
    }

    fun onBackground() {
        presentationModel.lifecycle.moveTo(CREATED)
    }

    fun onDestroy() {
        presentationModel.lifecycle.moveTo(DESTROYED)
        PmStore.removePm(pmTag)
    }
}