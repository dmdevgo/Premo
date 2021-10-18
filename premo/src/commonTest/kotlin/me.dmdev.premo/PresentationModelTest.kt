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

import kotlinx.coroutines.isActive
import me.dmdev.premo.PmLifecycle.State.*
import kotlin.test.*

class PresentationModelTest {

    private lateinit var pm: PresentationModel

    @BeforeTest
    fun setUp() {
        pm = TestPm()
    }

    @Test
    fun testInitialState() {
        assertTrue(pm.scope.isActive)
        assertNull(pm.inForegroundScope)
    }

    @Test
    fun testInForegroundState() {
        pm.lifecycle.moveTo(IN_FOREGROUND)

        assertTrue(pm.scope.isActive)
        assertTrue(pm.inForegroundScope?.isActive ?: false)
    }

    @Test
    fun testMoveToBackgroundState() {
        pm.lifecycle.moveTo(IN_FOREGROUND)
        pm.lifecycle.moveTo(CREATED)

        assertTrue(pm.scope.isActive)
        assertNull(pm.inForegroundScope)
    }

    @Test
    fun testMoveToDestroyedState() {
        pm.lifecycle.moveTo(DESTROYED)

        assertFalse(pm.scope.isActive)
        assertNull(pm.inForegroundScope)
    }

    @Test
    fun testNotAttachedChildrenLifecycle() {
        val pm1 = pm.Child(TestPm.Description)
        val pm2 = pm.Child(TestPm.Description)

        pm.lifecycle.moveTo(IN_FOREGROUND)

        assertEquals(pm1.lifecycle.state, CREATED)
        assertEquals(pm2.lifecycle.state, CREATED)
    }

    @Test
    fun testAttachedChildrenLifecycle() {
        val pm1 = pm.AttachedChild(TestPm.Description, "pm1")
        val pm2 = pm.AttachedChild(TestPm.Description, "pm2")

        pm.lifecycle.moveTo(IN_FOREGROUND)

        assertEquals(pm1.lifecycle.state, IN_FOREGROUND)
        assertEquals(pm2.lifecycle.state, IN_FOREGROUND)
    }
}