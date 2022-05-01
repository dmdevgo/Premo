/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020-2022 Dmitriy Gorbunov (dmitriy.goto@gmail.com)
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

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import me.dmdev.premo.PmLifecycle.State.CREATED
import me.dmdev.premo.PmLifecycle.State.DESTROYED
import me.dmdev.premo.PmLifecycle.State.IN_FOREGROUND

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
                description = TestPm.Description,
                factory = pmFactory,
                stateSaverFactory = TestStateSaverFactory(),
            )
        )
    }

    @Test
    fun testNoInteractions() {
        val pm = delegate.presentationModel
        assertNotNull(pm)
        assertEquals(pm, PmStore.get(TestPm.TAG))
    }

    @Test
    fun testOnCreate() {
        val pm = delegate.presentationModel
        delegate.onCreate()
        assertEquals(pm.lifecycle.state, CREATED)
        assertNotNull(PmStore.get(TestPm.TAG))
    }

    @Test
    fun testOnForeground() {
        val pm = delegate.presentationModel
        delegate.onCreate()
        delegate.onForeground()

        assertEquals(pm.lifecycle.state, IN_FOREGROUND)
        assertNotNull(PmStore.get(TestPm.TAG))
    }

    @Test
    fun testOnBackground() {
        val pm = delegate.presentationModel
        delegate.onCreate()
        delegate.onForeground()
        delegate.onBackground()

        assertEquals(pm.lifecycle.state, CREATED)
        assertNotNull(PmStore.get(TestPm.TAG))
    }

    @Test
    fun testOnDestroy() {
        val pm = delegate.presentationModel
        delegate.onCreate()
        delegate.onForeground()
        delegate.onBackground()
        delegate.onDestroy()

        assertEquals(pm.lifecycle.state, DESTROYED)
        assertNull(PmStore.get(TestPm.TAG))
    }
}