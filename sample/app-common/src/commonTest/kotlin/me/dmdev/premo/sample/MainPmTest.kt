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

import kotlinx.coroutines.test.runTest
import me.dmdev.premo.annotation.ExperimentalPremoApi
import me.dmdev.premo.test.navigation.assertDetailNotNull
import me.dmdev.premo.test.navigation.assertDetailNull
import me.dmdev.premo.test.navigation.onDetail
import me.dmdev.premo.test.runPmTest
import kotlin.test.Test

@OptIn(ExperimentalPremoApi::class)
class MainPmTest {

    @Test
    fun testOpenCounterSample() = runTest {
        runPmTest<MainPm>(
            pmArgs = MainPm.Args,
            pmFactory = MainPmFactory()
        ) {
            pm.assertDetailNull()
            pm.masterDetailNavigation.master.counterSample()
            pm.assertDetailNotNull()
            pm.masterDetailNavigation.onDetail<CounterPm> {}
        }
    }
}
