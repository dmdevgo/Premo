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

import me.dmdev.premo.PmConfig
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.Saveable
import me.dmdev.premo.navigation.PmFactory

class MainPmFactory : PmFactory {
    override fun createPm(description: Saveable, config: PmConfig): PresentationModel {
        return when (description) {
            is SamplesPm.Description -> SamplesPm(config)
            is CounterPm.Description -> CounterPm(description.maxCount, config)
            is CounterUdfPm.Description -> CounterUdfPm(description.maxCount, config)
            is CountdownPm.Description -> CountdownPm(config)
            is DialogPm.Description -> DialogPm(config)
            is BottomBarPm.Description -> BottomBarPm(config)
            is TabPm.Description -> TabPm(description.tabTitle, config)
            is TabItemPm.Description -> TabItemPm(description.screenTitle, description.tabTitle, config)
            else -> throw IllegalStateException("Not handled instance creation for pm description $description")
        }
    }
}