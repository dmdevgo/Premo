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

import me.dmdev.premo.*
import me.dmdev.premo.save.StateSaver
import kotlin.reflect.KType
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
        assertEquals(PmLifecycle.State.INITIALIZED, pm.lifecycle.state)
    }

    @Test
    fun `When move lifecycle of initialized PM to CREATED - lifecycle state should be CREATED`() {
        pm.lifecycle.moveTo(PmLifecycle.State.CREATED)

        assertEquals(PmLifecycle.State.CREATED, pm.lifecycle.state)
    }

    @Test
    fun `When move lifecycle of initialized PM to IN_FOREGROUND - lifecycle state should be IN_FOREGROUND`() {
        pm.lifecycle.moveTo(PmLifecycle.State.IN_FOREGROUND)

        assertEquals(PmLifecycle.State.IN_FOREGROUND, pm.lifecycle.state)
    }

    @Test
    fun `When move lifecycle of initialized PM to DESTROYED - lifecycle state should be DESTROYED`() {
        pm.lifecycle.moveTo(PmLifecycle.State.DESTROYED)

        assertEquals(PmLifecycle.State.DESTROYED, pm.lifecycle.state)
    }
}

class TestPm: PresentationModel(
    PmParams(
        tag = "",
        parent = null,
        state = null,
        factory = object : PmFactory {
            override fun createPm(params: PmParams): PresentationModel {
                TODO("Not yet implemented")
            }
        },
        description = object : PmDescription {},
        stateSaver = object : StateSaver {
            override fun <T> saveState(kType: KType, value: T): String {
                TODO("Not yet implemented")
            }

            override fun <T> restoreState(kType: KType, json: String): T {
                TODO("Not yet implemented")
            }

        }
    )
)