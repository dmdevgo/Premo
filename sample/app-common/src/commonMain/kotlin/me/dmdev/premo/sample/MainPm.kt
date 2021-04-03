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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.State
import me.dmdev.premo.navigation.NavigationMessage
import me.dmdev.premo.navigation.PmFactory
import me.dmdev.premo.navigation.PmStackChange

class MainPm(
    args: Args,
    pmFactory: PmFactory
) : PresentationModel(args) {

    @Serializable
    class Args : PresentationModel.Args()

    private val router = Router(SamplesPm.Args(), pmFactory)

    val currentPm = State(null) {
        router.pmStack.flow().map { it.lastOrNull()?.pm }
    }

    val pmStackChanges: Flow<PmStackChange> get() = router.pmStackChanges

    override fun handleNavigationMessage(message: NavigationMessage) {
        when (message) {
            CounterSampleMessage -> router.push(CounterPm.Args(10))
            CounterUdfSampleMessage -> router.push(CounterUdfPm.Args(10))
            CountdownSampleMessage -> router.push(CountdownPm.Args())
            DialogSampleMessage -> router.push(DialogPm.Args())
            MultistackSampleMessage -> router.push(BottomBarPm.Args())
            else -> super.handleNavigationMessage(message)
        }
    }
}