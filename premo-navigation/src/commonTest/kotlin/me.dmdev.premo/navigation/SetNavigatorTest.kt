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

import me.dmdev.premo.PmLifecycle.State.CREATED
import me.dmdev.premo.PmLifecycle.State.DESTROYED
import me.dmdev.premo.PmLifecycle.State.IN_FOREGROUND
import me.dmdev.premo.childrenOf
import me.dmdev.premo.navigation.TestPm.Companion.PM1_ARGS
import me.dmdev.premo.navigation.TestPm.Companion.PM2_ARGS
import me.dmdev.premo.navigation.TestPm.Companion.PM3_ARGS
import me.dmdev.premo.navigation.TestPm.Companion.PM4_ARGS
import me.dmdev.premo.navigation.TestPm.Companion.PM5_ARGS
import me.dmdev.premo.navigation.TestPm.Companion.PM6_ARGS
import me.dmdev.premo.navigation.set.DEFAULT_SET_NAVIGATOR_STATE_CURRENT_INDEX_KEY
import me.dmdev.premo.navigation.set.DEFAULT_SET_NAVIGATOR_STATE_VALUES_KEY
import me.dmdev.premo.navigation.set.SetNavigator
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SetNavigatorTest {

    private lateinit var parentPm: TestPm
    private lateinit var navigator: SetNavigator
    private lateinit var pm1: TestPm
    private lateinit var pm2: TestPm
    private lateinit var pm3: TestPm
    private lateinit var pm4: TestPm
    private lateinit var pm5: TestPm
    private lateinit var pm6: TestPm

    @BeforeTest
    fun setUp() {
        parentPm = TestPm.buildRootPm()
        parentPm.lifecycle.moveTo(IN_FOREGROUND)
        pm1 = parentPm.Child(PM1_ARGS)
        pm2 = parentPm.Child(PM2_ARGS)
        pm3 = parentPm.Child(PM3_ARGS)
        pm4 = parentPm.Child(PM4_ARGS)
        pm5 = parentPm.Child(PM5_ARGS)
        pm6 = parentPm.Child(PM6_ARGS)
        navigator = parentPm.SetNavigator(
            initValues = { listOf() }
        )
        navigator.changeValues(listOf(pm1, pm2, pm3))
    }

    @Test
    fun testInitialState() {
        assertEquals(navigator.values, listOf(pm1, pm2, pm3))
        assertEquals(pm1, navigator.current)
        assertEquals(pm1.lifecycle.state, IN_FOREGROUND)
        assertEquals(pm2.lifecycle.state, CREATED)
        assertEquals(pm3.lifecycle.state, CREATED)
    }

    @Test
    fun testSetCurrent() {
        navigator.changeCurrent(navigator.values.indexOf(pm2))

        assertEquals(pm2, navigator.current)
        assertEquals(pm1.lifecycle.state, CREATED)
        assertEquals(pm2.lifecycle.state, IN_FOREGROUND)
        assertEquals(pm3.lifecycle.state, CREATED)
    }

    @Test
    fun testMoveParentLifecycleToCreated() {
        parentPm.lifecycle.moveTo(CREATED)

        assertEquals(pm1.lifecycle.state, CREATED)
        assertEquals(pm2.lifecycle.state, CREATED)
        assertEquals(pm3.lifecycle.state, CREATED)
    }

    @Test
    fun testMoveParentLifecycleToCreatedAndThenInForeground() {
        parentPm.lifecycle.moveTo(CREATED)
        parentPm.lifecycle.moveTo(IN_FOREGROUND)

        assertEquals(pm1.lifecycle.state, IN_FOREGROUND)
        assertEquals(pm2.lifecycle.state, CREATED)
        assertEquals(pm3.lifecycle.state, CREATED)
    }

    @Test
    fun testEmptySetNavigator() {
        val navigator = parentPm.SetNavigator(
            initValues = { listOf() }
        )

        assertEquals(listOf(), navigator.values)
        assertEquals(null, navigator.current)
    }

    @Test
    fun testSetValuesToEmptyNavigator() {
        val navigator = parentPm.SetNavigator(
            initValues = { listOf() }
        )

        navigator.changeValues(listOf(pm1, pm2, pm3))

        assertEquals(listOf(pm1, pm2, pm3), navigator.values)
        assertEquals(navigator.current, pm1)
        assertEquals(IN_FOREGROUND, pm1.lifecycle.state)
        assertEquals(CREATED, pm2.lifecycle.state)
        assertEquals(CREATED, pm3.lifecycle.state)
    }

    @Test
    fun testReplaceValues() {
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
    fun testPartlyReplaceValues() {
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
    fun testRestoreState() {
        val stateSaverFactory = TestStateSaverFactory(
            initialState = mutableMapOf(
                TestPm.ROOT_PM_KEY to mutableMapOf(
                    DEFAULT_SET_NAVIGATOR_STATE_VALUES_KEY to listOf(
                        PM1_ARGS,
                        PM2_ARGS,
                        PM3_ARGS
                    ),
                    DEFAULT_SET_NAVIGATOR_STATE_CURRENT_INDEX_KEY to 0
                )
            )
        )

        val parentPm = TestPm.buildRootPm(stateSaverFactory)
        parentPm.lifecycle.moveTo(IN_FOREGROUND)
        val navigator = parentPm.SetNavigator(
            initValues = { listOf() }
        )

        assertEquals(
            listOf(IN_FOREGROUND, CREATED, CREATED),
            navigator.values.map { it.lifecycle.state }
        )

        assertEquals(
            listOf(PM1_ARGS, PM2_ARGS, PM3_ARGS),
            navigator.values.map { it.pmArgs }
        )
    }

    @Test
    fun testSaveState() {
        val stateSaverFactory = TestStateSaverFactory()

        val parentPm = TestPm.buildRootPm(stateSaverFactory)
        parentPm.lifecycle.moveTo(IN_FOREGROUND)
        val navigator = parentPm.SetNavigator(
            initValues = {
                parentPm.childrenOf(
                    PM1_ARGS,
                    PM2_ARGS,
                    PM3_ARGS
                )
            }
        )
        navigator.changeCurrent(1)

        parentPm.stateHandler.saveState()

        assertEquals(
            mutableMapOf(
                TestPm.ROOT_PM_KEY to mutableMapOf(
                    DEFAULT_SET_NAVIGATOR_STATE_VALUES_KEY to listOf(
                        PM1_ARGS,
                        PM2_ARGS,
                        PM3_ARGS
                    ),
                    DEFAULT_SET_NAVIGATOR_STATE_CURRENT_INDEX_KEY to 1
                ),
                "${TestPm.ROOT_PM_KEY}/${PM1_ARGS.key}" to mutableMapOf(),
                "${TestPm.ROOT_PM_KEY}/${PM2_ARGS.key}" to mutableMapOf(),
                "${TestPm.ROOT_PM_KEY}/${PM3_ARGS.key}" to mutableMapOf()
            ),
            stateSaverFactory.pmStates
        )
    }
}
