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

package me.dmdev.premo.navigation

import me.dmdev.premo.PmLifecycle.State.DESTROYED
import me.dmdev.premo.PmLifecycle.State.IN_FOREGROUND
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MasterDetailNavigatorTest {

    private lateinit var navigator: MasterDetailNavigator<TestPm, TestPm>
    private lateinit var parentPm: TestPm
    private lateinit var masterPm: TestPm
    private lateinit var detailPm1: TestPm
    private lateinit var detailPm2: TestPm

    @BeforeTest
    fun setUp() {
        parentPm = TestPm()
        parentPm.lifecycle.moveTo(IN_FOREGROUND)
        masterPm = parentPm.Child(TestPm.Description())
        detailPm1 = parentPm.Child(TestPm.Description())
        detailPm2 = parentPm.Child(TestPm.Description())
        navigator = MasterDetailNavigatorImpl(masterPm)
    }

    @Test
    fun testInitialState() {
        assertNull(navigator.detail)
        assertEquals(masterPm.lifecycle.state, IN_FOREGROUND)
    }

    @Test
    fun testSetDetail() {
        navigator.changeDetail(detailPm1)
        assertEquals(detailPm1, navigator.detail)
        assertEquals(detailPm1.lifecycle.state, IN_FOREGROUND)
        assertEquals(masterPm.lifecycle.state, IN_FOREGROUND)
    }

    @Test
    fun testResetDetail() {
        navigator.changeDetail(detailPm1)
        navigator.changeDetail(detailPm2)

        assertEquals(detailPm2, navigator.detail)
        assertEquals(detailPm1.lifecycle.state, DESTROYED)
        assertEquals(detailPm2.lifecycle.state, IN_FOREGROUND)
        assertEquals(masterPm.lifecycle.state, IN_FOREGROUND)
    }

    @Test
    fun testResetNullDetail() {
        navigator.changeDetail(detailPm1)
        navigator.changeDetail(null)

        assertEquals(detailPm1.lifecycle.state, DESTROYED)
        assertEquals(navigator.detail, null)
        assertEquals(masterPm.lifecycle.state, IN_FOREGROUND)
    }

    @Test
    fun testHandleBackWhenDetailIsNull() {
        val handled = navigator.handleBack()

        assertFalse(handled)
        assertEquals(navigator.detail, null)
        assertEquals(masterPm.lifecycle.state, IN_FOREGROUND)
    }

    @Test
    fun testHandleBackWhenDetailIsNotNull() {
        navigator.changeDetail(detailPm1)
        val handled = navigator.handleBack()

        assertTrue(handled)
        assertEquals(detailPm1.lifecycle.state, DESTROYED)
        assertEquals(navigator.detail, null)
        assertEquals(masterPm.lifecycle.state, IN_FOREGROUND)
    }
}
