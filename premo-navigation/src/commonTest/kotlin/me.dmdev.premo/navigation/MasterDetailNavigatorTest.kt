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

package me.dmdev.premo.navigation

import me.dmdev.premo.PmLifecycle.State.DESTROYED
import me.dmdev.premo.PmLifecycle.State.IN_FOREGROUND
import kotlin.test.*

class MasterDetailNavigatorTest {

    private lateinit var navigator: MasterDetailNavigator<TestPm, TestPm>
    private lateinit var parentPm: TestPm
    private lateinit var masterPm: TestPm

    @BeforeTest
    fun setUp() {
        parentPm = TestPm()
        parentPm.lifecycle.moveTo(IN_FOREGROUND)
        masterPm = parentPm.Child(TestPm.Description)
        navigator = MasterDetailNavigatorImpl(masterPm)
    }

    @Test
    fun testInitialState() {
        assertNull(navigator.detailPm)
        assertEquals(masterPm.lifecycle.state, IN_FOREGROUND)
    }

    @Test
    fun testSetDetail() {
        val detailPm = parentPm.Child<TestPm>(TestPm.Description)
        navigator.setDetail(detailPm)
        assertEquals(detailPm, navigator.detailPm)
        assertEquals(detailPm.lifecycle.state, IN_FOREGROUND)
        assertEquals(masterPm.lifecycle.state, IN_FOREGROUND)
    }

    @Test
    fun testResetDetail() {
        val detailPm1 = parentPm.Child<TestPm>(TestPm.Description)
        val detailPm2 = parentPm.Child<TestPm>(TestPm.Description)
        navigator.setDetail(detailPm1)
        navigator.setDetail(detailPm2)

        assertEquals(detailPm2, navigator.detailPm)
        assertEquals(detailPm1.lifecycle.state, DESTROYED)
        assertEquals(detailPm2.lifecycle.state, IN_FOREGROUND)
        assertEquals(masterPm.lifecycle.state, IN_FOREGROUND)
    }

    @Test
    fun testResetNullDetail() {
        val detailPm1 = parentPm.Child<TestPm>(TestPm.Description)
        navigator.setDetail(detailPm1)
        navigator.setDetail(null)

        assertEquals(detailPm1.lifecycle.state, DESTROYED)
        assertEquals(navigator.detailPm, null)
        assertEquals(masterPm.lifecycle.state, IN_FOREGROUND)
    }

    @Test
    fun testHandleBackWhenDetailIsNull() {
        val handled = navigator.handleSystemBack()

        assertFalse(handled)
        assertEquals(navigator.detailPm, null)
        assertEquals(masterPm.lifecycle.state, IN_FOREGROUND)
    }

    @Test
    fun testHandleBackWhenDetailIsNotNull() {
        val detailPm = parentPm.Child<TestPm>(TestPm.Description)
        navigator.setDetail(detailPm)
        val handled = navigator.handleSystemBack()

        assertTrue(handled)
        assertEquals(detailPm.lifecycle.state, DESTROYED)
        assertEquals(navigator.detailPm, null)
        assertEquals(masterPm.lifecycle.state, IN_FOREGROUND)
    }
}