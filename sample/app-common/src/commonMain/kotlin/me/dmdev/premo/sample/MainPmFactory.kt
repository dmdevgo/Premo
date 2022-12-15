/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020-2022 Dmitriy Gorbunov (dmitriy.goto@gmail.com)
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

import me.dmdev.premo.PmFactory
import me.dmdev.premo.PmParams
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.sample.bottom_navigation.BottomNavigationPm
import me.dmdev.premo.sample.bottom_navigation.TabItemPm
import me.dmdev.premo.sample.bottom_navigation.TabPm
import me.dmdev.premo.sample.dilaog_navigation.DialogNavigationPm
import me.dmdev.premo.sample.dilaog_navigation.SimpleDialogPm
import me.dmdev.premo.sample.stack_navigation.SimpleScreenPm
import me.dmdev.premo.sample.stack_navigation.StackNavigationPm

class MainPmFactory : PmFactory {
    override fun createPm(params: PmParams): PresentationModel {
        return when (val description = params.description) {
            is MainPm.Description -> MainPm(params)
            is SamplesPm.Description -> SamplesPm(params)
            is CounterPm.Description -> CounterPm(description.maxCount, params)
            is StackNavigationPm.Description -> StackNavigationPm(params)
            is SimpleScreenPm.Description -> SimpleScreenPm(description.number, params)
            is BottomNavigationPm.Description -> BottomNavigationPm(params)
            is TabPm.Description -> TabPm(description.tabTitle, params)
            is TabItemPm.Description -> TabItemPm(description.screenTitle, description.tabTitle, params)
            is DialogNavigationPm.Description -> DialogNavigationPm(params)
            is SimpleDialogPm.Description -> SimpleDialogPm(
                description.title,
                description.message,
                description.okButtonText,
                description.cancelButtonText,
                params
            )
            else -> throw IllegalArgumentException("Not handled instance creation for pm description $description")
        }
    }
}