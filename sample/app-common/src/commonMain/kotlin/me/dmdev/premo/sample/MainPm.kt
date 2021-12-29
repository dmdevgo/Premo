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

import kotlinx.serialization.Serializable
import me.dmdev.premo.*
import me.dmdev.premo.navigation.BackMessage
import me.dmdev.premo.navigation.MasterDetailNavigation
import me.dmdev.premo.navigation.SystemBackMessage
import me.dmdev.premo.navigation.handleBack
import me.dmdev.premo.sample.bottom_navigation.BottomNavigationPm
import me.dmdev.premo.sample.stack_navigation.StackNavigationPm


class MainPm(params: PmParams) : PresentationModel(params) {

    @Serializable
    object Description : PmDescription

    val navigation = MasterDetailNavigation<SamplesPm, PresentationModel>(
        masterPmDescription = SamplesPm.Description
    ) { navigator ->
        onMessage<CounterSampleMessage> {
            navigator.setDetail(Child(CounterPm.Description(10)))
        }
        onMessage<StackNavigationSampleMessage> {
            navigator.setDetail(Child(StackNavigationPm.Description))
        }
        onMessage<BottomNavigationSampleMessage> {
            navigator.setDetail(Child(BottomNavigationPm.Description))
        }
        onMessage<BackMessage> {
            navigator.handleBack()
        }
        handle<SystemBackMessage> {
            navigator.handleBack()
        }
    }
}