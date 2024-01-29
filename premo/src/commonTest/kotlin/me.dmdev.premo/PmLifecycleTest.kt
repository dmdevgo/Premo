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

import me.dmdev.premo.PmLifecycle.State.CREATED
import me.dmdev.premo.PmLifecycle.State.DESTROYED
import me.dmdev.premo.PmLifecycle.State.IN_FOREGROUND
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PmLifecycleTest {

    private lateinit var lifecycle: PmLifecycle
    private lateinit var observer: TestPmLifecycleObserver

    @BeforeTest
    fun setUp() {
        lifecycle = PmLifecycle()
        observer = TestPmLifecycleObserver()
        lifecycle.addObserver(observer)
    }

    @Test
    fun testInitialState() {
        assertEquals(CREATED, lifecycle.state)
        assertTrue { lifecycle.isCreated }
        assertFalse { lifecycle.isInForeground }
        assertFalse { lifecycle.isDestroyed }
        assertEquals(listOf(CREATED to CREATED), observer.states)
    }

    @Test
    fun testMoveToCreated() {
        lifecycle.moveTo(CREATED)

        assertEquals(CREATED, lifecycle.state)
        assertTrue { lifecycle.isCreated }
        assertFalse { lifecycle.isInForeground }
        assertFalse { lifecycle.isDestroyed }
        assertEquals(listOf(CREATED to CREATED), observer.states)
    }

    @Test
    fun testMoveToInForeground() {
        lifecycle.moveTo(IN_FOREGROUND)

        assertEquals(IN_FOREGROUND, lifecycle.state)
        assertFalse { lifecycle.isCreated }
        assertTrue { lifecycle.isInForeground }
        assertFalse { lifecycle.isDestroyed }
        assertEquals(
            listOf(
                CREATED to CREATED,
                CREATED to IN_FOREGROUND
            ),
            observer.states
        )
    }

    @Test
    fun testMoveToCreatedFromInForeground() {
        lifecycle.moveTo(IN_FOREGROUND)
        lifecycle.moveTo(CREATED)

        assertEquals(CREATED, lifecycle.state)
        assertTrue { lifecycle.isCreated }
        assertFalse { lifecycle.isInForeground }
        assertFalse { lifecycle.isDestroyed }
        assertEquals(
            listOf(
                CREATED to CREATED,
                CREATED to IN_FOREGROUND,
                IN_FOREGROUND to CREATED
            ),
            observer.states
        )
    }

    @Test
    fun testMoveToDestroyedFromCreated() {
        lifecycle.moveTo(DESTROYED)

        assertEquals(DESTROYED, lifecycle.state)
        assertFalse { lifecycle.isCreated }
        assertFalse { lifecycle.isInForeground }
        assertTrue { lifecycle.isDestroyed }
        assertEquals(
            listOf(
                CREATED to CREATED,
                CREATED to DESTROYED
            ),
            observer.states
        )
    }

    @Test
    fun testMoveToDestroyedFromInForeground() {
        lifecycle.moveTo(IN_FOREGROUND)
        lifecycle.moveTo(DESTROYED)

        assertEquals(DESTROYED, lifecycle.state)
        assertFalse { lifecycle.isCreated }
        assertFalse { lifecycle.isInForeground }
        assertTrue { lifecycle.isDestroyed }
        assertEquals(
            listOf(
                CREATED to CREATED,
                CREATED to IN_FOREGROUND,
                IN_FOREGROUND to CREATED,
                CREATED to DESTROYED
            ),
            observer.states
        )
    }

    @Test
    fun testMultipleSubscribers() {
        val observer1 = TestPmLifecycleObserver()
        val observer2 = TestPmLifecycleObserver()
        lifecycle.addObserver(observer1)
        lifecycle.addObserver(observer2)
        lifecycle.moveTo(IN_FOREGROUND)
        assertEquals(
            listOf(
                CREATED to CREATED,
                CREATED to IN_FOREGROUND
            ),
            observer1.states
        )
        assertEquals(
            listOf(
                CREATED to CREATED,
                CREATED to IN_FOREGROUND
            ),
            observer2.states
        )
    }

    @Test
    fun testRemoveLifecycleObserver() {
        lifecycle.removeObserver(observer)
        lifecycle.moveTo(IN_FOREGROUND)
        assertEquals(listOf(CREATED to CREATED), observer.states)
    }

    @Test
    fun testDoOnCreate() {
        TestCallback().apply {
            assertNotCalled()
            lifecycle.doOnCreate(::call)
            assertCalledOnce()
            lifecycle.moveTo(IN_FOREGROUND)
            lifecycle.moveTo(CREATED)
            lifecycle.moveTo(DESTROYED)
            assertCalledOnce()
        }
    }

    @Test
    fun testDoOnDestroy() {
        TestCallback().apply {
            lifecycle.doOnDestroy(::call)
            lifecycle.moveTo(IN_FOREGROUND)
            lifecycle.moveTo(CREATED)
            assertNotCalled()
            lifecycle.moveTo(DESTROYED)
            assertCalledOnce()
        }
    }

    @Test
    fun testDoOnForeground() {
        TestCallback().apply {
            assertNotCalled()
            lifecycle.doOnForeground(::call)
            lifecycle.moveTo(IN_FOREGROUND)
            assertCalledOnce()
            lifecycle.moveTo(CREATED)
            assertCalledOnce()
            lifecycle.moveTo(IN_FOREGROUND)
            assertCalledTwice()
            lifecycle.moveTo(DESTROYED)
            assertCalledTwice()
        }
    }

    @Test
    fun testDoOnBackground() {
        TestCallback().apply {
            lifecycle.doOnBackground(::call)
            lifecycle.moveTo(IN_FOREGROUND)
            assertNotCalled()
            lifecycle.moveTo(CREATED)
            assertCalledOnce()
            lifecycle.moveTo(IN_FOREGROUND)
            assertCalledOnce()
            lifecycle.moveTo(CREATED)
            assertCalledTwice()
            lifecycle.moveTo(DESTROYED)
            assertCalledTwice()
        }
    }
}
