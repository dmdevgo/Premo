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

package me.dmdev.premo.sample

import kotlinx.serialization.json.Json
import me.dmdev.premo.PmDescription
import me.dmdev.premo.PmParams
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.sample.bottomnavigation.BottomNavigationPm
import me.dmdev.premo.sample.bottomnavigation.TabItemPm
import me.dmdev.premo.sample.bottomnavigation.TabPm
import me.dmdev.premo.sample.dilaognavigation.DialogNavigationPm
import me.dmdev.premo.sample.dilaognavigation.SimpleDialogPm
import me.dmdev.premo.sample.stacknavigation.SimpleScreenPm
import me.dmdev.premo.sample.stacknavigation.StackNavigationPm
import me.dmdev.premo.saver.JsonPmStateSaver

object Stubs {

    private val mainPmFactory = MainPmFactory()

    val mainPm = createPm<MainPm>(MainPm.Description)
    val samplesPm = createPm<SamplesPm>(SamplesPm.Description)
    val counterPm = createPm<CounterPm>(CounterPm.Description(10))
    val stackNavigationPm = createPm<StackNavigationPm>(StackNavigationPm.Description)
    val simplePm = createPm<SimpleScreenPm>(SimpleScreenPm.Description(1))
    val bottomBarPm = createPm<BottomNavigationPm>(BottomNavigationPm.Description)
    val tabPm = createPm<TabPm>(TabPm.Description("Tab #"))
    val tabItemPm = createPm<TabItemPm>(TabItemPm.Description("Screen #", "Tab #"))
    val dialogNavigationPm = createPm<DialogNavigationPm>(DialogNavigationPm.Description)
    val simpleDialogPm = createPm<SimpleDialogPm>(
        SimpleDialogPm.Description("Title", "Text", "Ок", "Cancel")
    )

    private fun <PM : PresentationModel> createPm(description: PmDescription): PM {
        val config = PmParams(
            tag = "",
            parent = null,
            description = description,
            factory = mainPmFactory,
            stateSaverFactory = { JsonPmStateSaver(Json, mutableMapOf()) }
        )
        @Suppress("UNCHECKED_CAST")
        return mainPmFactory.createPm(config) as PM
    }
}
