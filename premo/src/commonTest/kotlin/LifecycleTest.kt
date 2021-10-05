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

import me.dmdev.premo.lifecycle.Lifecycle
import me.dmdev.premo.lifecycle.LifecycleEvent
import me.dmdev.premo.lifecycle.LifecycleEvent.*
import me.dmdev.premo.lifecycle.LifecycleObserver
import me.dmdev.premo.lifecycle.LifecycleState
import me.dmdev.premo.lifecycle.LifecycleState.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LifecycleTest {

    private var lifecycle = Lifecycle()
    private var observer = LifecycleTestObserver()

    @BeforeTest
    fun setUp() {
        lifecycle = Lifecycle()
        observer = LifecycleTestObserver()
        lifecycle.addObserver(observer)
    }

    @Test
    fun testInitialState() {
        assertEquals(lifecycle.state, INITIALIZED)
    }

    @Test
    fun testMoveToInitialized() {
        lifecycle.moveTo(INITIALIZED)
        assertEquals(listOf(), observer.states)
        assertEquals(listOf(), observer.events)
    }

    @Test
    fun testMoveToCreated() {
        lifecycle.moveTo(CREATED)
        assertEquals(listOf(CREATED), observer.states)
        assertEquals(listOf(ON_CREATE), observer.events)
    }

    @Test
    fun testMoveToInForeground() {
        lifecycle.moveTo(IN_FOREGROUND)
        assertEquals(listOf(CREATED, IN_FOREGROUND), observer.states)
        assertEquals(listOf(ON_CREATE, ON_FOREGROUND), observer.events)
    }

    @Test
    fun testMoveToCreatedFromInForeground() {
        lifecycle.moveTo(IN_FOREGROUND)
        lifecycle.moveTo(CREATED)
        assertEquals(listOf(CREATED, IN_FOREGROUND, CREATED), observer.states)
        assertEquals(listOf(ON_CREATE, ON_FOREGROUND, ON_BACKGROUND), observer.events)
    }

    @Test
    fun testMoveToDestroyedFromInitialized() {
        lifecycle.moveTo(DESTROYED)
        assertEquals(listOf(DESTROYED), observer.states)
        assertEquals(listOf(ON_DESTROY), observer.events)
    }

    @Test
    fun testMoveToDestroyedFromCreated() {
        lifecycle.moveTo(CREATED)
        lifecycle.moveTo(DESTROYED)
        assertEquals(listOf(CREATED, DESTROYED), observer.states)
        assertEquals(listOf(ON_CREATE, ON_DESTROY), observer.events)
    }

    @Test
    fun testMoveToDestroyedFromInForeground() {
        lifecycle.moveTo(IN_FOREGROUND)
        lifecycle.moveTo(DESTROYED)
        assertEquals(listOf(CREATED, IN_FOREGROUND, CREATED, DESTROYED), observer.states)
        assertEquals(listOf(ON_CREATE, ON_FOREGROUND, ON_BACKGROUND, ON_DESTROY), observer.events)
    }

    @Test
    fun testMultipleSubscribers() {
        val observer1 = LifecycleTestObserver()
        val observer2 = LifecycleTestObserver()
        lifecycle.addObserver(observer1)
        lifecycle.addObserver(observer2)
        lifecycle.moveTo(CREATED)
        assertEquals(listOf(CREATED), observer1.states)
        assertEquals(listOf(CREATED), observer2.states)
        assertEquals(listOf(ON_CREATE), observer1.events)
        assertEquals(listOf(ON_CREATE), observer2.events)
    }

    @Test
    fun testRemoveLifecycleObserver() {
        lifecycle.removeObserver(observer)
        lifecycle.moveTo(IN_FOREGROUND)
        assertEquals(listOf(), observer.states)
        assertEquals(listOf(), observer.events)
    }
}

class LifecycleTestObserver : LifecycleObserver {

    val states = mutableListOf<LifecycleState>()
    val events = mutableListOf<LifecycleEvent>()

    override fun onLifecycleChange(lifecycle: Lifecycle, event: LifecycleEvent) {
        states.add(lifecycle.state)
        events.add(event)
    }
}