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

import kotlinx.coroutines.test.runTest
import me.dmdev.premo.PmLifecycle.State.DESTROYED
import me.dmdev.premo.PmLifecycle.State.IN_FOREGROUND
import me.dmdev.premo.navigation.TestPm.Companion.DETAIL1_PM_ARGS
import me.dmdev.premo.navigation.TestPm.Companion.DETAIL2_PM_ARGS
import me.dmdev.premo.navigation.TestPm.Companion.DETAIL_SAVED_PM_ARGS
import me.dmdev.premo.navigation.TestPm.Companion.MASTER_PM_ARGS
import me.dmdev.premo.navigation.TestPm.Companion.ROOT_PM_ARGS
import me.dmdev.premo.navigation.master.DEFAULT_MASTER_DETAIL_NAVIGATOR_DETAIL_STATE_KEY
import me.dmdev.premo.navigation.master.MasterDetailNavigator
import me.dmdev.premo.navigation.master.handleBack
import me.dmdev.premo.saver.NoPmStateSaverFactory
import me.dmdev.premo.saver.PmStateSaverFactory
import me.dmdev.premo.test.PmTestContext
import me.dmdev.premo.test.runPmTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MasterDetailNavigatorTest {

    @Test
    fun testInitialState() = runMasterDetailNavigationTest {
        assertEquals(masterPm, navigator.master)
        assertEquals(IN_FOREGROUND, navigator.master.lifecycle.state)
        assertNull(navigator.detail)
    }

    @Test
    fun testSetDetail() = runMasterDetailNavigationTest {
        navigator.changeDetail(detailPm1)
        assertEquals(detailPm1, navigator.detail)
        assertEquals(IN_FOREGROUND, detailPm1.lifecycle.state)
        assertEquals(IN_FOREGROUND, navigator.master.lifecycle.state)
    }

    @Test
    fun testResetDetail() = runMasterDetailNavigationTest {
        navigator.changeDetail(detailPm1)
        navigator.changeDetail(detailPm2)

        assertEquals(detailPm2, navigator.detail)
        assertEquals(DESTROYED, detailPm1.lifecycle.state)
        assertEquals(IN_FOREGROUND, detailPm2.lifecycle.state)
        assertEquals(IN_FOREGROUND, navigator.master.lifecycle.state)
    }

    @Test
    fun testResetNullDetail() = runMasterDetailNavigationTest {
        navigator.changeDetail(detailPm1)
        navigator.changeDetail(null)

        assertEquals(DESTROYED, detailPm1.lifecycle.state)
        assertEquals(null, navigator.detail)
        assertEquals(IN_FOREGROUND, navigator.master.lifecycle.state)
    }

    @Test
    fun testHandleBackWhenDetailIsNull() = runMasterDetailNavigationTest {
        assertFalse { navigator.handleBack() }
        assertEquals(null, navigator.detail)
        assertEquals(IN_FOREGROUND, navigator.master.lifecycle.state)
    }

    @Test
    fun testHandleBackWhenDetailIsNotNull() = runMasterDetailNavigationTest {
        navigator.changeDetail(detailPm1)

        assertTrue { navigator.handleBack() }
        assertEquals(DESTROYED, detailPm1.lifecycle.state)
        assertEquals(null, navigator.detail)
        assertEquals(IN_FOREGROUND, navigator.master.lifecycle.state)
    }

    @Test
    fun testRestoreState() = runMasterDetailNavigationTest(
        pmStateSaverFactory = TestStateSaverFactory(
            initialState = mutableMapOf(
                ROOT_PM_ARGS.key to mutableMapOf(
                    DEFAULT_MASTER_DETAIL_NAVIGATOR_DETAIL_STATE_KEY to DETAIL_SAVED_PM_ARGS
                )
            )
        )
    ) {
        assertEquals(DETAIL_SAVED_PM_ARGS, navigator.detail?.pmArgs)
        assertEquals(IN_FOREGROUND, navigator.master.lifecycle.state)
        assertEquals(IN_FOREGROUND, navigator.detail?.lifecycle?.state)
    }

    @Test
    fun testSaveState() {
        val stateSaverFactory = TestStateSaverFactory()

        runMasterDetailNavigationTest(
            pmStateSaverFactory = stateSaverFactory
        ) {
            navigator.changeDetail(detailPm1)
            onSave()

            assertEquals(
                mutableMapOf(
                    ROOT_PM_ARGS.key to mutableMapOf<String, Any>(
                        DEFAULT_MASTER_DETAIL_NAVIGATOR_DETAIL_STATE_KEY to DETAIL1_PM_ARGS
                    ),
                    "${ROOT_PM_ARGS.key}/${MASTER_PM_ARGS.key}" to mutableMapOf(),
                    "${ROOT_PM_ARGS.key}/${DETAIL1_PM_ARGS.key}" to mutableMapOf(),
                    "${ROOT_PM_ARGS.key}/${DETAIL2_PM_ARGS.key}" to mutableMapOf()
                ),
                stateSaverFactory.pmStates
            )
        }
    }

    private class MasterDetailNavigationTestContext(
        private val pmTestContext: PmTestContext<TestPm>
    ) : PmTestContext<TestPm> by pmTestContext {
        val masterPm = pm.Child<TestPm>(MASTER_PM_ARGS)
        val detailPm1 = pm.Child<TestPm>(DETAIL1_PM_ARGS)
        val detailPm2 = pm.Child<TestPm>(DETAIL2_PM_ARGS)
        val navigator = pm.MasterDetailNavigator<TestPm, TestPm>(masterPm)
    }

    private fun runMasterDetailNavigationTest(
        pmStateSaverFactory: PmStateSaverFactory = NoPmStateSaverFactory,
        testBody: MasterDetailNavigationTestContext.() -> Unit
    ) = runTest {
        runPmTest(
            pmArgs = ROOT_PM_ARGS,
            pmFactory = TestPmFactory,
            pmStateSaverFactory = pmStateSaverFactory
        ) {
            val testContext = MasterDetailNavigationTestContext(this)
            testBody.invoke(testContext)
        }
    }
}
