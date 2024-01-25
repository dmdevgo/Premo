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

package me.dmdev.premo.sample

import me.dmdev.premo.PmArgs
import me.dmdev.premo.PmFactory
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.sample.bottomnavigation.BottomNavigationPm
import me.dmdev.premo.sample.bottomnavigation.TabItemPm
import me.dmdev.premo.sample.bottomnavigation.TabPm
import me.dmdev.premo.sample.dialognavigation.DialogNavigationPm
import me.dmdev.premo.sample.dialognavigation.SimpleDialogPm
import me.dmdev.premo.sample.stacknavigation.SimpleScreenPm
import me.dmdev.premo.sample.stacknavigation.StackNavigationPm

class MainPmFactory : PmFactory {
    override fun createPresentationModel(args: PmArgs): PresentationModel {
        return when (args) {
            is MainPm.Args -> MainPm(args)
            is SamplesPm.Args -> SamplesPm(args)
            is CounterPm.Args -> CounterPm(args)
            is StackNavigationPm.Args -> StackNavigationPm(args)
            is SimpleScreenPm.Args -> SimpleScreenPm(args)
            is BottomNavigationPm.Args -> BottomNavigationPm(args)
            is TabPm.Args -> TabPm(args)
            is TabItemPm.Args -> TabItemPm(args)
            is DialogNavigationPm.Args -> DialogNavigationPm(args)
            is SimpleDialogPm.Args -> SimpleDialogPm(args)
            else -> throw IllegalArgumentException("Not handled instance creation for pm args $args")
        }
    }
}
