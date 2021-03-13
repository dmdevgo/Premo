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

import me.dmdev.premo.LifecycleState
import me.dmdev.premo.PresentationModel
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PresentationModelLifecycleStateTest {

    private lateinit var pm: TestPm

    @BeforeTest
    fun setUp() {
        pm = TestPm()
    }

    @Test
    fun `When create PM lifecycle - state should be INITIALIZED`() {
        assertEquals(LifecycleState.INITIALIZED, pm.lifecycleState.value)
    }

    @Test
    fun `When move lifecycle of initialized PM to CREATED - lifecycle state should be CREATED`() {
        pm.moveLifecycleTo(LifecycleState.CREATED)

        assertEquals(LifecycleState.CREATED, pm.lifecycleState.value)
    }

    @Test
    fun `When move lifecycle of initialized PM to IN_FOREGROUND - lifecycle state should be IN_FOREGROUND`() {
        pm.moveLifecycleTo(LifecycleState.IN_FOREGROUND)

        assertEquals(LifecycleState.IN_FOREGROUND, pm.lifecycleState.value)
    }

    @Test
    fun `When move lifecycle of initialized PM to DESTROYED - lifecycle state should be DESTROYED`() {
        pm.moveLifecycleTo(LifecycleState.DESTROYED)

        assertEquals(LifecycleState.DESTROYED, pm.lifecycleState.value)
    }
}

class TestPm: PresentationModel()