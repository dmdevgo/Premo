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

package me.dmdev.premo.sample

import me.dmdev.premo.PmState
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.Saveable
import me.dmdev.premo.navigation.PmFactory

class MainPmFactory : PmFactory {
    override fun createPm(description: Saveable, pmState: PmState?): PresentationModel {
        return when (description) {
            is SamplesPm.Description -> SamplesPm(pmState)
            is CounterPm.Description -> CounterPm(description.maxCount, pmState)
            is CounterUdfPm.Description -> CounterUdfPm(description.maxCount, pmState)
            is CountdownPm.Description -> CountdownPm(pmState)
            is DialogPm.Description -> DialogPm(pmState)
            is BottomBarPm.Description -> BottomBarPm(pmFactory = this, pmState = pmState)
            is TabPm.Description -> TabPm(pmFactory = this, description.tabTitle, pmState)
            is TabItemPm.Description -> TabItemPm(description.screenTitle, description.tabTitle, pmState)
            else -> throw IllegalStateException("Not handled instance creation for pm description $description")
        }
    }
}