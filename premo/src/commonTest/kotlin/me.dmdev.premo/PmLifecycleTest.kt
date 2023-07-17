/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020-2023 Dmitriy Gorbunov (dmitriy.goto@gmail.com)
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

class PmLifecycleTest {

    private lateinit var lifecycle: PmLifecycle
    private lateinit var observer: PmLifecycleTestObserver

    @BeforeTest
    fun setUp() {
        lifecycle = PmLifecycle()
        observer = PmLifecycleTestObserver()
        lifecycle.addObserver(observer)
    }

    @Test
    fun testInitialState() {
        assertEquals(lifecycle.state, CREATED)
        assertEquals(listOf(CREATED), observer.states)
    }

    @Test
    fun testMoveToCreated() {
        lifecycle.moveTo(CREATED)
        assertEquals(listOf(CREATED), observer.states)
    }

    @Test
    fun testMoveToInForeground() {
        lifecycle.moveTo(IN_FOREGROUND)
        assertEquals(listOf(CREATED, IN_FOREGROUND), observer.states)
    }

    @Test
    fun testMoveToCreatedFromInForeground() {
        lifecycle.moveTo(IN_FOREGROUND)
        lifecycle.moveTo(CREATED)
        assertEquals(listOf(CREATED, IN_FOREGROUND, CREATED), observer.states)
    }

    @Test
    fun testMoveToDestroyedFromCreated() {
        lifecycle.moveTo(DESTROYED)
        assertEquals(listOf(CREATED, DESTROYED), observer.states)
    }

    @Test
    fun testMoveToDestroyedFromInForeground() {
        lifecycle.moveTo(IN_FOREGROUND)
        lifecycle.moveTo(DESTROYED)
        assertEquals(listOf(CREATED, IN_FOREGROUND, CREATED, DESTROYED), observer.states)
    }

    @Test
    fun testMultipleSubscribers() {
        val observer1 = PmLifecycleTestObserver()
        val observer2 = PmLifecycleTestObserver()
        lifecycle.addObserver(observer1)
        lifecycle.addObserver(observer2)
        lifecycle.moveTo(IN_FOREGROUND)
        assertEquals(listOf(CREATED, IN_FOREGROUND), observer1.states)
        assertEquals(listOf(CREATED, IN_FOREGROUND), observer2.states)
    }

    @Test
    fun testRemoveLifecycleObserver() {
        lifecycle.removeObserver(observer)
        lifecycle.moveTo(IN_FOREGROUND)
        assertEquals(listOf(CREATED), observer.states)
    }
}
