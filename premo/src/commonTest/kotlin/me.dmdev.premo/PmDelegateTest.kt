/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020-2026 Dmitriy Gorbunov (dmitriy.goto@gmail.com)
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
import me.dmdev.premo.PmLifecycle.State.ACTIVE
import me.dmdev.premo.PmLifecycle.State.CREATED
import me.dmdev.premo.PmLifecycle.State.DESTROYED
import me.dmdev.premo.PmLifecycle.State.INITIALIZED
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PmDelegateTest {

    @Test
    fun testNoInteractions() = runPmTest {
        assertNotNull(pm)
        assertEquals(pm, PmStore[pm.tag])
    }

    @Test
    fun testOnCreate() = runPmTest {
        pmDelegate.onCreate()
        assertEquals(CREATED, pm.lifecycle.state)
        assertNotNull(PmStore[pm.tag])
    }

    @Test
    fun testOnActive() = runPmTest {
        pmDelegate.onCreate()
        pmDelegate.onActive()

        assertEquals(ACTIVE, pm.lifecycle.state)
        assertNotNull(PmStore[pm.tag])
    }

    @Test
    fun testOnInactive() = runPmTest {
        pmDelegate.onCreate()
        pmDelegate.onActive()
        pmDelegate.onInactive()

        assertEquals(CREATED, pm.lifecycle.state)
        assertNotNull(PmStore[pm.tag])
    }

    @Test
    fun testOnDestroy() = runPmTest {
        pmDelegate.onCreate()
        pmDelegate.onActive()
        pmDelegate.onInactive()
        pmDelegate.onDestroy()

        assertEquals(DESTROYED, pm.lifecycle.state)
        assertNull(PmStore[pm.tag])
    }

    private fun runPmTest(
        testBody: suspend PmTestContext<TestPm>.() -> Unit
    ) = runTest {
        runPmTest(
            pmArgs = TestPm.Args(ROOT_KEY),
            pmFactory = TestPmFactory,
            pmStateSaverFactory = TestPmStateSaverFactory,
            initialPmLifecycleState = INITIALIZED,
            testBody = testBody
        )
    }

    companion object {
        private const val ROOT_KEY = "root"
    }
}
