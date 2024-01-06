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

    companion object {
        private val MASTER_PM_ARGS = TestPm.Args("master_pm")
        private val DETAIL_PM1_ARGS = TestPm.Args("detail_pm1")
        private val DETAIL_PM2_ARGS = TestPm.Args("detail_pm2")
    }

    private lateinit var navigator: MasterDetailNavigator<TestPm, TestPm>
    private lateinit var parentPm: TestPm
    private lateinit var detailPm1: TestPm
    private lateinit var detailPm2: TestPm

    @BeforeTest
    fun setUp() {
        parentPm = TestPm.buildRootPm()
        parentPm.lifecycle.moveTo(IN_FOREGROUND)
        detailPm1 = parentPm.Child(DETAIL_PM1_ARGS)
        detailPm2 = parentPm.Child(DETAIL_PM2_ARGS)
        navigator = parentPm.MasterDetailNavigator(
            masterPm = parentPm.Child(MASTER_PM_ARGS)
        )
    }

    @Test
    fun testInitialState() {
        assertEquals(MASTER_PM_ARGS, navigator.master.pmArgs)
        assertEquals(IN_FOREGROUND, navigator.master.lifecycle.state)
        assertNull(navigator.detail)
    }

    @Test
    fun testSetDetail() {
        navigator.changeDetail(detailPm1)
        assertEquals(detailPm1, navigator.detail)
        assertEquals(IN_FOREGROUND, detailPm1.lifecycle.state)
        assertEquals(IN_FOREGROUND, navigator.master.lifecycle.state)
    }

    @Test
    fun testResetDetail() {
        navigator.changeDetail(detailPm1)
        navigator.changeDetail(detailPm2)

        assertEquals(detailPm2, navigator.detail)
        assertEquals(DESTROYED, detailPm1.lifecycle.state)
        assertEquals(IN_FOREGROUND, detailPm2.lifecycle.state)
        assertEquals(IN_FOREGROUND, navigator.master.lifecycle.state)
    }

    @Test
    fun testResetNullDetail() {
        navigator.changeDetail(detailPm1)
        navigator.changeDetail(null)

        assertEquals(DESTROYED, detailPm1.lifecycle.state)
        assertEquals(null, navigator.detail)
        assertEquals(IN_FOREGROUND, navigator.master.lifecycle.state)
    }

    @Test
    fun testHandleBackWhenDetailIsNull() {
        val handled = navigator.handleBack()

        assertFalse(handled)
        assertEquals(null, navigator.detail)
        assertEquals(IN_FOREGROUND, navigator.master.lifecycle.state)
    }

    @Test
    fun testHandleBackWhenDetailIsNotNull() {
        navigator.changeDetail(detailPm1)
        val handled = navigator.handleBack()

        assertTrue(handled)
        assertEquals(DESTROYED, detailPm1.lifecycle.state)
        assertEquals(null, navigator.detail)
        assertEquals(IN_FOREGROUND, navigator.master.lifecycle.state)
    }

    @Test
    fun testRestoreState() {
        val masterPmArgs = TestPm.Args("master_pm")
        val detailPmArgs = TestPm.Args("detail_pm")

        val stateSaverFactory = TestStateSaverFactory(
            initialState = mutableMapOf(
                TestPm.ROOT_PM_KEY to mutableMapOf(
                    DEFAULT_MASTER_DETAIL_NAVIGATOR_DETAIL_STATE_KEY to detailPmArgs
                )
            )
        )

        val parentPm = TestPm.buildRootPm(stateSaverFactory)
        parentPm.lifecycle.moveTo(IN_FOREGROUND)
        val navigator: MasterDetailNavigator<TestPm, TestPm> = parentPm.MasterDetailNavigator(
            masterPm = parentPm.Child(masterPmArgs)
        )

        assertEquals(masterPmArgs, navigator.master.pmArgs)
        assertEquals(detailPmArgs, navigator.detail?.pmArgs)
        assertEquals(IN_FOREGROUND, navigator.master.lifecycle.state)
        assertEquals(IN_FOREGROUND, navigator.detail?.lifecycle?.state)
    }

    @Test
    fun testSaveState() {
        val masterPmArgs = TestPm.Args("master_pm")
        val detailPmArgs = TestPm.Args("detail_pm")

        val stateSaverFactory = TestStateSaverFactory()

        val parentPm = TestPm.buildRootPm(stateSaverFactory)
        parentPm.lifecycle.moveTo(IN_FOREGROUND)
        val navigator: MasterDetailNavigator<TestPm, TestPm> = parentPm.MasterDetailNavigator(
            masterPm = parentPm.Child(masterPmArgs)
        )
        navigator.changeDetail(parentPm.Child(detailPmArgs))

        parentPm.stateHandler.saveState()

        assertEquals(
            mutableMapOf(
                TestPm.ROOT_PM_KEY to mutableMapOf<String, Any>(
                    DEFAULT_MASTER_DETAIL_NAVIGATOR_DETAIL_STATE_KEY to detailPmArgs
                ),
                "${TestPm.ROOT_PM_KEY}/${masterPmArgs.key}" to mutableMapOf(),
                "${TestPm.ROOT_PM_KEY}/${detailPmArgs.key}" to mutableMapOf()
            ),
            stateSaverFactory.pmStates
        )
    }
}
