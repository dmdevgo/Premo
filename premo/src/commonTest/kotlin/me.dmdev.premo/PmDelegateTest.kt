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

package me.dmdev.premo

import me.dmdev.premo.PmLifecycle.State.*
import me.dmdev.premo.internal.PmStore
import kotlin.test.*

class PmDelegateTest {

    private lateinit var pmFactory: TestPmFactory
    private lateinit var delegate: PmDelegate<TestPm>

    @BeforeTest
    fun setUp() {
        pmFactory = TestPmFactory()
        delegate = PmDelegate(
            PmParams(
                tag = TestPm.TAG,
                parent = null,
                state = mapOf(),
                factory = pmFactory,
                stateSaver = TestPmStateSaver(),
                description = TestPm.Description
            )
        )
    }

    @Test
    fun testNoInteractions() {
        val pm = delegate.presentationModel
        assertNotNull(pm)
        assertEquals(pm, PmStore.getPm(TestPm.TAG))
    }

    @Test
    fun testOnCreate() {
        val pm = delegate.presentationModel
        delegate.onCreate()
        assertEquals(pm.lifecycle.state, CREATED)
        assertNotNull(PmStore.getPm(TestPm.TAG))
    }

    @Test
    fun testOnForeground() {
        val pm = delegate.presentationModel
        delegate.onCreate()
        delegate.onForeground()

        assertEquals(pm.lifecycle.state, IN_FOREGROUND)
        assertNotNull(PmStore.getPm(TestPm.TAG))
    }

    @Test
    fun testOnBackground() {
        val pm = delegate.presentationModel
        delegate.onCreate()
        delegate.onForeground()
        delegate.onBackground()

        assertEquals(pm.lifecycle.state, CREATED)
        assertNotNull(PmStore.getPm(TestPm.TAG))
    }

    @Test
    fun testOnDestroy() {
        val pm = delegate.presentationModel
        delegate.onCreate()
        delegate.onForeground()
        delegate.onBackground()
        delegate.onDestroy()

        assertEquals(pm.lifecycle.state, DESTROYED)
        assertNull(PmStore.getPm(TestPm.TAG))
    }
}