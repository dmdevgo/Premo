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

import me.dmdev.premo.PmParams
import me.dmdev.premo.PresentationModel

object Stubs {

    private val mainPmFactory = MainPmFactory()

    val samplesPm = createPm<SamplesPm>(SamplesPm.Description)
    val counterPm = createPm<CounterPm>(CounterPm.Description(10))
    val counterUdfPm = createPm<CounterUdfPm>(CounterUdfPm.Description(10))
    val countdownPm = createPm<CountdownPm>(CountdownPm.Description)
    val controlsPm = createPm<ControlsPm>(ControlsPm.Description)
    val dialogPm = createPm<DialogPm>(DialogPm.Description)
    val bottomBarPm = createPm<BottomBarPm>(BottomBarPm.Description)
    val tabPm = createPm<TabPm>(TabPm.Description("Tab #"))
    val tabItemPm = createPm<TabItemPm>(TabItemPm.Description("Screen #", "Tab #"))

    private fun <PM : PresentationModel> createPm(description: PresentationModel.Description): PM {
        val config = PmParams(
            tag = "",
            parent = null,
            state = null,
            factory = mainPmFactory,
            description = description,
            stateSaver = JsonStateSaver()
        )
        @Suppress("UNCHECKED_CAST")
        return mainPmFactory.createPm(config) as PM
    }
}