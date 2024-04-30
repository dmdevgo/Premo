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
import me.dmdev.premo.PmLifecycle.State.CREATED
import me.dmdev.premo.PmLifecycle.State.DESTROYED
import me.dmdev.premo.PmLifecycle.State.IN_FOREGROUND
import me.dmdev.premo.navigation.TestPm.Companion.PM1_ARGS
import me.dmdev.premo.navigation.TestPm.Companion.PM2_ARGS
import me.dmdev.premo.navigation.TestPm.Companion.PM3_ARGS
import me.dmdev.premo.navigation.TestPm.Companion.PM4_ARGS
import me.dmdev.premo.navigation.TestPm.Companion.PM5_ARGS
import me.dmdev.premo.navigation.TestPm.Companion.PM6_ARGS
import me.dmdev.premo.navigation.TestPm.Companion.ROOT_PM_ARGS
import me.dmdev.premo.navigation.set.DEFAULT_SET_NAVIGATOR_STATE_KEY
import me.dmdev.premo.navigation.set.SetNavigator
import me.dmdev.premo.navigation.set.handleBack
import me.dmdev.premo.saver.NoPmStateSaverFactory
import me.dmdev.premo.saver.PmStateSaverFactory
import me.dmdev.premo.test.PmTestContext
import me.dmdev.premo.test.runPmTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SetNavigatorTest {

    @Test
    fun testInitialState() = runSetNavigationTest {
        assertEquals(listOf(pm1, pm2, pm3), navigator.values)
        assertEquals(pm1, navigator.current)
        assertEquals(IN_FOREGROUND, pm1.lifecycle.state)
        assertEquals(CREATED, pm2.lifecycle.state)
        assertEquals(CREATED, pm3.lifecycle.state)
    }

    @Test
    fun testSetCurrent() = runSetNavigationTest {
        navigator.changeCurrent(navigator.values.indexOf(pm2))

        assertEquals(pm2, navigator.current)
        assertEquals(CREATED, pm1.lifecycle.state)
        assertEquals(IN_FOREGROUND, pm2.lifecycle.state)
        assertEquals(CREATED, pm3.lifecycle.state)
    }

    @Test
    fun testMoveParentLifecycleToCreated() = runSetNavigationTest {
        onBackground()

        assertEquals(CREATED, pm1.lifecycle.state)
        assertEquals(CREATED, pm2.lifecycle.state)
        assertEquals(CREATED, pm3.lifecycle.state)
    }

    @Test
    fun testMoveParentLifecycleToCreatedAndThenInForeground() = runSetNavigationTest {
        onBackground()
        onForeground()

        assertEquals(IN_FOREGROUND, pm1.lifecycle.state)
        assertEquals(CREATED, pm2.lifecycle.state)
        assertEquals(CREATED, pm3.lifecycle.state)
    }

    @Test
    fun testEmptySetNavigator() = runSetNavigationTest {
        val navigator = pm.SetNavigator(
            initValues = { listOf() },
            key = "set_navigator_2"
        )

        assertEquals(listOf(), navigator.values)
        assertNull(navigator.current)
    }

    @Test
    fun testSetValuesToEmptyNavigator() = runSetNavigationTest {
        val navigator = pm.SetNavigator(
            initValues = { listOf() },
            key = "set_navigator_2"
        )

        navigator.changeValues(listOf(pm1, pm2, pm3))

        assertEquals(listOf(pm1, pm2, pm3), navigator.values)
        assertEquals(navigator.current, pm1)
        assertEquals(IN_FOREGROUND, pm1.lifecycle.state)
        assertEquals(CREATED, pm2.lifecycle.state)
        assertEquals(CREATED, pm3.lifecycle.state)
    }

    @Test
    fun testReplaceValues() = runSetNavigationTest {
        val pm4 = pm.Child<TestPm>(PM4_ARGS)
        val pm5 = pm.Child<TestPm>(PM5_ARGS)
        val pm6 = pm.Child<TestPm>(PM6_ARGS)
        navigator.changeValues(listOf(pm4, pm5, pm6))

        assertEquals(listOf(pm4, pm5, pm6), navigator.values)
        assertEquals(navigator.current, pm4)
        assertEquals(DESTROYED, pm1.lifecycle.state)
        assertEquals(DESTROYED, pm2.lifecycle.state)
        assertEquals(DESTROYED, pm3.lifecycle.state)
        assertEquals(IN_FOREGROUND, pm4.lifecycle.state)
        assertEquals(CREATED, pm5.lifecycle.state)
        assertEquals(CREATED, pm6.lifecycle.state)
    }

    @Test
    fun testPartlyReplaceValues() = runSetNavigationTest {
        val pm4 = pm.Child<TestPm>(PM4_ARGS)
        val pm5 = pm.Child<TestPm>(PM5_ARGS)
        navigator.changeValues(listOf(pm2, pm4, pm5))

        assertEquals(listOf(pm2, pm4, pm5), navigator.values)
        assertEquals(navigator.current, pm2)
        assertEquals(DESTROYED, pm1.lifecycle.state)
        assertEquals(IN_FOREGROUND, pm2.lifecycle.state)
        assertEquals(DESTROYED, pm3.lifecycle.state)
        assertEquals(CREATED, pm4.lifecycle.state)
        assertEquals(CREATED, pm5.lifecycle.state)
    }

    @Test
    fun testHandleBackWhenCurrentIndexIsZero() = runSetNavigationTest {
        navigator.changeCurrent(0)
        assertFalse { navigator.handleBack() }
    }

    @Test
    fun testHandleBackWhenCurrentIndexIsNotZero() = runSetNavigationTest {
        navigator.changeCurrent(pm3)
        assertEquals(pm3, navigator.current)
        assertTrue { navigator.handleBack() }
        assertEquals(pm1, navigator.current)
    }

    @Test
    fun testRestoreState() = runSetNavigationTest(
        pmStateSaverFactory = TestStateSaverFactory(
            initialState = mutableMapOf(
                ROOT_PM_ARGS.key to mutableMapOf(
                    DEFAULT_SET_NAVIGATOR_STATE_KEY to Pair(
                        listOf(
                            PM4_ARGS,
                            PM5_ARGS,
                            PM6_ARGS
                        ),
                        0
                    )
                )
            )
        )
    ) {
        assertEquals(
            listOf(IN_FOREGROUND, CREATED, CREATED),
            navigator.values.map { it.lifecycle.state }
        )

        assertEquals(
            listOf(PM4_ARGS, PM5_ARGS, PM6_ARGS),
            navigator.values.map { it.pmArgs }
        )
    }

    @Test
    fun testSaveState() {
        val stateSaverFactory = TestStateSaverFactory()

        runSetNavigationTest(
            pmStateSaverFactory = stateSaverFactory
        ) {
            val index = 1
            navigator.changeCurrent(index)
            onSave()

            assertEquals(
                mutableMapOf(
                    ROOT_PM_ARGS.key to mutableMapOf<String, Any>(
                        DEFAULT_SET_NAVIGATOR_STATE_KEY to Pair(
                            listOf(
                                PM1_ARGS,
                                PM2_ARGS,
                                PM3_ARGS
                            ),
                            index
                        )
                    ),
                    "${ROOT_PM_ARGS.key}/${PM1_ARGS.key}" to mutableMapOf(),
                    "${ROOT_PM_ARGS.key}/${PM2_ARGS.key}" to mutableMapOf(),
                    "${ROOT_PM_ARGS.key}/${PM3_ARGS.key}" to mutableMapOf()
                ),
                stateSaverFactory.pmStates
            )
        }
    }

    private class SetNavigationTestContext(
        private val pmTestContext: PmTestContext<TestPm>
    ) : PmTestContext<TestPm> by pmTestContext {
        val pm1 = pm.Child<TestPm>(PM1_ARGS)
        val pm2 = pm.Child<TestPm>(PM2_ARGS)
        val pm3 = pm.Child<TestPm>(PM3_ARGS)
        val navigator = pm.SetNavigator(initValues = { listOf(pm1, pm2, pm3) })
    }

    private fun runSetNavigationTest(
        pmStateSaverFactory: PmStateSaverFactory = NoPmStateSaverFactory,
        testBody: SetNavigationTestContext.() -> Unit
    ) = runTest {
        runPmTest(
            pmArgs = ROOT_PM_ARGS,
            pmFactory = TestPmFactory,
            pmStateSaverFactory = pmStateSaverFactory
        ) {
            val testContext = SetNavigationTestContext(this)
            testBody.invoke(testContext)
        }
    }
}
