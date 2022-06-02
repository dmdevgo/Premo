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

package me.dmdev.premo.navigation

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import me.dmdev.premo.PmLifecycle
import me.dmdev.premo.PmLifecycle.State.CREATED
import me.dmdev.premo.PmLifecycle.State.DESTROYED
import me.dmdev.premo.PmLifecycle.State.IN_FOREGROUND

class StackNavigatorTest {

    private lateinit var lifecycle: PmLifecycle
    private lateinit var navigator: StackNavigator
    private lateinit var pm1: TestPm
    private lateinit var pm2: TestPm
    private lateinit var pm3: TestPm

    @BeforeTest
    fun setUp() {
        lifecycle = PmLifecycle()
        lifecycle.moveTo(IN_FOREGROUND)
        pm1 = TestPm()
        pm2 = TestPm()
        pm3 = TestPm()
        navigator = StackNavigatorImpl(
            lifecycle = lifecycle,
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        )
    }

    @Test
    fun testEmptyNavigator() {
        assertNull(navigator.currentTop)
        assertTrue(navigator.backstack.isEmpty())
        assertTrue(navigator.backstackState.value.isEmpty())
    }

    @Test
    fun testPushOnePm() {
        navigator.push(pm1)

        assertEquals(pm1, navigator.currentTop)
        assertEquals(pm1.lifecycle.state, IN_FOREGROUND)
        assertEquals(listOf(pm1), navigator.backstack)
    }

    @Test
    fun testPushTwoPm() {
        navigator.push(pm1)
        navigator.push(pm2)

        assertEquals(pm2,  navigator.currentTop)
        assertEquals(pm1.lifecycle.state, CREATED)
        assertEquals(pm2.lifecycle.state, IN_FOREGROUND)
        assertEquals(listOf(pm1, pm2), navigator.backstack)
    }

    @Test
    fun testPopEmpty() {
        navigator.pop()

        assertNull(navigator.currentTop)
        assertTrue(navigator.backstack.isEmpty())
        assertTrue(navigator.backstackState.value.isEmpty())
    }

    @Test
    fun testPushOnePmAndThenPop() {
        navigator.push(pm1)
        navigator.pop()

        assertNull(navigator.currentTop)
        assertEquals(pm1.lifecycle.state, DESTROYED)
        assertTrue(navigator.backstack.isEmpty())
        assertTrue(navigator.backstackState.value.isEmpty())
    }

    @Test
    fun testPopSecondPm() {
        navigator.push(pm1)
        navigator.push(pm2)
        navigator.pop()

        assertEquals(pm1,  navigator.currentTop)
        assertEquals(pm1.lifecycle.state, IN_FOREGROUND)
        assertEquals(pm2.lifecycle.state, DESTROYED)
        assertEquals(listOf(pm1), navigator.backstack)
    }

    @Test
    fun testSetBackstack() {
        val backstack = listOf(pm1, pm2, pm3)
        navigator.setBackStack(backstack)
        assertEquals(pm1.lifecycle.state, CREATED)
        assertEquals(pm2.lifecycle.state, CREATED)
        assertEquals(pm3.lifecycle.state, IN_FOREGROUND)
        assertEquals(backstack, navigator.backstack)
    }

    @Test
    fun testHandleBackWhenTwoPmInBackstack() {
        navigator.push(pm1)
        navigator.push(pm2)

        assertTrue(navigator.handleBack())
        assertEquals(pm1, navigator.currentTop)
        assertEquals(pm1.lifecycle.state, IN_FOREGROUND)
        assertEquals(pm2.lifecycle.state, DESTROYED)
    }

    @Test
    fun testHandleBackWhenOnePmInBackstack() {
        navigator.push(pm1)

        assertFalse(navigator.handleBack())
        assertEquals(pm1, navigator.currentTop)
        assertEquals(pm1.lifecycle.state, IN_FOREGROUND)

    }

    @Test
    fun testHandleBackWhenBackstackIsEmpty() {
        assertFalse(navigator.handleBack())
    }

    @Test
    fun testMoveTopPmToCreatedWhenLifecycleMoveToCreated() {
        navigator.push(pm1)
        navigator.push(pm2)
        lifecycle.moveTo(CREATED)

        assertEquals(pm1.lifecycle.state, CREATED)
        assertEquals(pm2.lifecycle.state, CREATED)
    }

    @Test
    fun testMoveTopPmToInForegroundWhenLifecycleMoveToInForeground() {
        navigator.push(pm1)
        navigator.push(pm2)
        lifecycle.moveTo(CREATED)
        lifecycle.moveTo(IN_FOREGROUND)

        assertEquals(pm1.lifecycle.state, CREATED)
        assertEquals(pm2.lifecycle.state, IN_FOREGROUND)
    }

    @Test
    fun testMoveAllPmToDestroyedWhenLifecycleMoveToDestroyed() {
        navigator.push(pm1)
        navigator.push(pm2)
        lifecycle.moveTo(DESTROYED)

        assertEquals(pm1.lifecycle.state, DESTROYED)
        assertEquals(pm2.lifecycle.state, DESTROYED)
    }

}