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

import me.dmdev.premo.PmLifecycle.Event.*
import me.dmdev.premo.PmLifecycle.State.*
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
    }

    @Test
    fun testMoveToCreated() {
        lifecycle.moveTo(CREATED)
        assertEquals(listOf(), observer.states)
        assertEquals(listOf(), observer.events)
    }

    @Test
    fun testMoveToInForeground() {
        lifecycle.moveTo(IN_FOREGROUND)
        assertEquals(listOf(IN_FOREGROUND), observer.states)
        assertEquals(listOf(ON_FOREGROUND), observer.events)
    }

    @Test
    fun testMoveToCreatedFromInForeground() {
        lifecycle.moveTo(IN_FOREGROUND)
        lifecycle.moveTo(CREATED)
        assertEquals(listOf(IN_FOREGROUND, CREATED), observer.states)
        assertEquals(listOf(ON_FOREGROUND, ON_BACKGROUND), observer.events)
    }

    @Test
    fun testMoveToDestroyedFromCreated() {
        lifecycle.moveTo(DESTROYED)
        assertEquals(listOf(DESTROYED), observer.states)
        assertEquals(listOf(ON_DESTROY), observer.events)
    }

    @Test
    fun testMoveToDestroyedFromInForeground() {
        lifecycle.moveTo(IN_FOREGROUND)
        lifecycle.moveTo(DESTROYED)
        assertEquals(listOf(IN_FOREGROUND, CREATED, DESTROYED), observer.states)
        assertEquals(listOf(ON_FOREGROUND, ON_BACKGROUND, ON_DESTROY), observer.events)
    }

    @Test
    fun testMultipleSubscribers() {
        val observer1 = PmLifecycleTestObserver()
        val observer2 = PmLifecycleTestObserver()
        lifecycle.addObserver(observer1)
        lifecycle.addObserver(observer2)
        lifecycle.moveTo(IN_FOREGROUND)
        assertEquals(listOf(IN_FOREGROUND), observer1.states)
        assertEquals(listOf(IN_FOREGROUND), observer2.states)
        assertEquals(listOf(ON_FOREGROUND), observer1.events)
        assertEquals(listOf(ON_FOREGROUND), observer2.events)
    }

    @Test
    fun testRemoveLifecycleObserver() {
        lifecycle.removeObserver(observer)
        lifecycle.moveTo(IN_FOREGROUND)
        assertEquals(listOf(), observer.states)
        assertEquals(listOf(), observer.events)
    }
}

