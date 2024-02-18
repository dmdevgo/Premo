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

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import me.dmdev.premo.annotation.ExperimentalPremoApi
import me.dmdev.premo.saver.NoPmStateSaverFactory
import me.dmdev.premo.saver.PmStateSaverFactory

@OptIn(ExperimentalCoroutinesApi::class)
@ExperimentalPremoApi
suspend fun <PM : PresentationModel> runPmTest(
    pmArgs: PmArgs,
    pmFactory: PmFactory,
    pmStateSaverFactory: PmStateSaverFactory = NoPmStateSaverFactory,
    testDispatcher: CoroutineDispatcher = UnconfinedTestDispatcher(),
    initialPmLifecycleState: PmLifecycle.State = PmLifecycle.State.IN_FOREGROUND,
    testBody: suspend PmTestContext<PM>.() -> Unit
) {
    Dispatchers.setMain(testDispatcher)

    val pmTestContext = PmTestContextImpl<PM>(
        pmArgs = pmArgs,
        pmFactory = pmFactory,
        pmStateSaverFactory = pmStateSaverFactory,
        testDispatcher = testDispatcher
    )

    try {
        when (initialPmLifecycleState) {
            PmLifecycle.State.CREATED -> {
                pmTestContext.onCreate()
            }

            PmLifecycle.State.IN_FOREGROUND -> {
                pmTestContext.onCreate()
                pmTestContext.onForeground()
            }

            else -> throw IllegalArgumentException("Initial PM lifecycle state should be CREATED or IN FOREGROUND")
        }

        testBody.invoke(pmTestContext)
    } finally {
        try {
            pmTestContext.onBackground()
            pmTestContext.onDestroy()
        } finally {
            Dispatchers.resetMain()
        }
    }
}
