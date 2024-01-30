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

package me.dmdev.premo

import kotlinx.coroutines.test.runTest
import me.dmdev.premo.saver.JsonStateSaver
import me.dmdev.premo.test.runPmTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PmStateSaverTest {

    @Test
    fun testPmStateRestoring() = runTest {
        val jsonStateSever = JsonStateSaver(Serializer.json)
        runPmTest<TestPm>(
            pmArgs = TestPm.Args("root"),
            pmFactory = TestPmFactory(),
            pmStateSaverFactory = jsonStateSever
        ) {
            val rootPm = pm

            for (i in 1..5) {
                rootPm.Child<TestPm>(TestPm.Args("child$i")).apply {
                    for (k in 1..5) {
                        Child<TestPm>(TestPm.Args("child$i$k"))
                    }
                }
            }

            onSave()
            val savedState = jsonStateSever.save()

            val newJsonStateSever = JsonStateSaver(Serializer.json)
            newJsonStateSever.restore(savedState)

            runPmTest(
                pmArgs = TestPm.Args("root"),
                pmFactory = TestPmFactory(),
                pmStateSaverFactory = newJsonStateSever
            ) {
                val restoredRootPm = pm

                assertEquals(rootPm, restoredRootPm)
            }
        }
    }
}
