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
import me.dmdev.premo.navigation.stack.DEFAULT_STACK_NAVIGATOR_BACKSTACK_STATE_KEY
import me.dmdev.premo.navigation.stack.StackNavigator
import me.dmdev.premo.navigation.stack.handleBack
import me.dmdev.premo.navigation.stack.pop
import me.dmdev.premo.navigation.stack.popToRoot
import me.dmdev.premo.navigation.stack.popUntil
import me.dmdev.premo.navigation.stack.push
import me.dmdev.premo.navigation.stack.replaceAll
import me.dmdev.premo.navigation.stack.replaceTop
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StackNavigatorTest {

    private lateinit var parentPm: TestPm
    private lateinit var navigator: StackNavigator
    private lateinit var pm1: TestPm
    private lateinit var pm2: TestPm
    private lateinit var pm3: TestPm

    @BeforeTest
    fun setUp() {
        parentPm = TestPm.buildRootPm()
        parentPm.lifecycle.moveTo(IN_FOREGROUND)
        pm1 = parentPm.Child(PM1_ARGS)
        pm2 = parentPm.Child(PM2_ARGS)
        pm3 = parentPm.Child(PM3_ARGS)
        navigator = parentPm.StackNavigator()
    }

    @Test
    fun testEmptyNavigator() {
        assertNull(navigator.currentTop)
        assertTrue(navigator.backStack.isEmpty())
        assertTrue(navigator.backStackFlow.value.isEmpty())
    }

    @Test
    fun testInitialPm() {
        val navigator = parentPm.StackNavigator(
            initBackStack = {
                parentPm.childrenOf(
                    PM1_ARGS,
                    PM2_ARGS,
                    PM3_ARGS
                )
            }
        )
        assertEquals(
            listOf(CREATED, CREATED, IN_FOREGROUND),
            navigator.backStack.map { it.lifecycle.state }
        )

        assertEquals(
            listOf(PM1_ARGS, PM2_ARGS, PM3_ARGS),
            navigator.backStack.map { it.pmArgs }
        )
    }

    @Test
    fun testPushOnePm() {
        navigator.push(pm1)

        assertEquals(pm1, navigator.currentTop)
        assertEquals(pm1.lifecycle.state, IN_FOREGROUND)
        assertEquals(listOf(pm1), navigator.backStack)
    }

    @Test
    fun testPushTwoPm() {
        navigator.push(pm1)
        navigator.push(pm2)

        assertEquals(pm2, navigator.currentTop)
        assertEquals(pm1.lifecycle.state, CREATED)
        assertEquals(pm2.lifecycle.state, IN_FOREGROUND)
        assertEquals(listOf(pm1, pm2), navigator.backStack)
    }

    @Test
    fun testPopEmpty() {
        navigator.pop()

        assertNull(navigator.currentTop)
        assertTrue(navigator.backStack.isEmpty())
        assertTrue(navigator.backStackFlow.value.isEmpty())
    }

    @Test
    fun testPushOnePmAndThenPop() {
        navigator.push(pm1)
        navigator.pop()

        assertNull(navigator.currentTop)
        assertEquals(pm1.lifecycle.state, DESTROYED)
        assertTrue(navigator.backStack.isEmpty())
        assertTrue(navigator.backStackFlow.value.isEmpty())
    }

    @Test
    fun testPopSecondPm() {
        navigator.push(pm1)
        navigator.push(pm2)
        navigator.pop()

        assertEquals(pm1, navigator.currentTop)
        assertEquals(pm1.lifecycle.state, IN_FOREGROUND)
        assertEquals(pm2.lifecycle.state, DESTROYED)
        assertEquals(listOf(pm1), navigator.backStack)
    }

    @Test
    fun testPopToRoot() {
        navigator.changeBackStack(listOf(pm1, pm2, pm3))
        val popped = navigator.popToRoot()

        assertEquals(popped, true)
        assertEquals(pm1, navigator.currentTop)
        assertEquals(pm1.lifecycle.state, IN_FOREGROUND)
        assertEquals(pm2.lifecycle.state, DESTROYED)
        assertEquals(pm3.lifecycle.state, DESTROYED)
        assertEquals(listOf(pm1), navigator.backStack)
    }

    @Test
    fun testPopToRootWhenBackstackIsEmpty() {
        val popped = navigator.popToRoot()

        assertEquals(popped, false)
        assertEquals(listOf(), navigator.backStack)
    }

    @Test
    fun testReplaceTop() {
        navigator.changeBackStack(listOf(pm1, pm2))
        navigator.replaceTop(pm3)

        assertEquals(pm1.lifecycle.state, CREATED)
        assertEquals(pm2.lifecycle.state, DESTROYED)
        assertEquals(pm3.lifecycle.state, IN_FOREGROUND)
        assertEquals(pm3, navigator.currentTop)
        assertEquals(listOf(pm1, pm3), navigator.backStack)
    }

    @Test
    fun testReplaceTopWhenBackstackIsEmpty() {
        navigator.replaceTop(pm3)

        assertEquals(pm3, navigator.currentTop)
        assertEquals(pm3.lifecycle.state, IN_FOREGROUND)
        assertEquals(listOf(pm3), navigator.backStack)
    }

    @Test
    fun testReplaceAll() {
        navigator.changeBackStack(listOf(pm1, pm2))
        navigator.replaceAll(pm3)

        assertEquals(pm1.lifecycle.state, DESTROYED)
        assertEquals(pm2.lifecycle.state, DESTROYED)
        assertEquals(pm3.lifecycle.state, IN_FOREGROUND)
        assertEquals(pm3, navigator.currentTop)
        assertEquals(listOf(pm3), navigator.backStack)
    }

    @Test
    fun testPopUntil() {
        println(listOf(pm1, pm2, pm3))
        navigator.changeBackStack(listOf(pm1, pm2, pm3))
        navigator.popUntil { it === pm1 }

        assertEquals(pm1, navigator.currentTop)
        assertEquals(pm1.lifecycle.state, IN_FOREGROUND)
        assertEquals(pm2.lifecycle.state, DESTROYED)
        assertEquals(pm3.lifecycle.state, DESTROYED)
        assertEquals(listOf(pm1), navigator.backStack)
    }

    @Test
    fun testSetBackstack() {
        val backstack = listOf(pm1, pm2, pm3)
        navigator.changeBackStack(backstack)

        assertEquals(pm1.lifecycle.state, CREATED)
        assertEquals(pm2.lifecycle.state, CREATED)
        assertEquals(pm3.lifecycle.state, IN_FOREGROUND)
        assertEquals(backstack, navigator.backStack)
    }

    @Test
    fun testHandleBackWhenTwoPmInBackstack() {
        navigator.push(pm1)
        navigator.push(pm2)

        assertTrue(navigator.handleBack())
        assertEquals(pm1, navigator.currentTop)
        assertEquals(pm1.lifecycle.state, IN_FOREGROUND)
        assertEquals(pm2.lifecycle.state, DESTROYED)
    }

    @Test
    fun testHandleBackWhenOnePmInBackstack() {
        navigator.push(pm1)

        assertFalse(navigator.handleBack())
        assertEquals(pm1, navigator.currentTop)
        assertEquals(pm1.lifecycle.state, IN_FOREGROUND)
    }

    @Test
    fun testHandleBackWhenBackstackIsEmpty() {
        assertFalse(navigator.handleBack())
    }

    @Test
    fun testMoveTopPmToCreatedWhenLifecycleMoveToCreated() {
        navigator.push(pm1)
        navigator.push(pm2)
        parentPm.lifecycle.moveTo(CREATED)

        assertEquals(pm1.lifecycle.state, CREATED)
        assertEquals(pm2.lifecycle.state, CREATED)
    }

    @Test
    fun testMoveTopPmToInForegroundWhenLifecycleMoveToInForeground() {
        navigator.push(pm1)
        navigator.push(pm2)
        parentPm.lifecycle.moveTo(CREATED)
        parentPm.lifecycle.moveTo(IN_FOREGROUND)

        assertEquals(pm1.lifecycle.state, CREATED)
        assertEquals(pm2.lifecycle.state, IN_FOREGROUND)
    }

    @Test
    fun testMoveAllPmToDestroyedWhenLifecycleMoveToDestroyed() {
        navigator.push(pm1)
        navigator.push(pm2)
        parentPm.lifecycle.moveTo(DESTROYED)

        assertEquals(pm1.lifecycle.state, DESTROYED)
        assertEquals(pm2.lifecycle.state, DESTROYED)
    }

    @Test
    fun testRestoreState() {
        val stateSaverFactory = TestStateSaverFactory(
            initialState = mutableMapOf(
                TestPm.ROOT_PM_KEY to mutableMapOf(
                    DEFAULT_STACK_NAVIGATOR_BACKSTACK_STATE_KEY to listOf(
                        PM1_ARGS,
                        PM2_ARGS,
                        PM3_ARGS
                    )
                )
            )
        )

        val parentPm = TestPm.buildRootPm(stateSaverFactory)
        parentPm.lifecycle.moveTo(IN_FOREGROUND)
        val navigator = parentPm.StackNavigator()

        assertEquals(
            listOf(CREATED, CREATED, IN_FOREGROUND),
            navigator.backStack.map { it.lifecycle.state }
        )

        assertEquals(
            listOf(PM1_ARGS, PM2_ARGS, PM3_ARGS),
            navigator.backStack.map { it.pmArgs }
        )
    }

    @Test
    fun testSaveState() {
        val stateSaverFactory = TestStateSaverFactory()

        val parentPm = TestPm.buildRootPm(stateSaverFactory)
        parentPm.lifecycle.moveTo(IN_FOREGROUND)
        val navigator = parentPm.StackNavigator(
            initBackStack = {
                parentPm.childrenOf(
                    PM1_ARGS,
                    PM2_ARGS,
                    PM3_ARGS
                )
            }
        )

        parentPm.stateHandler.saveState()

        assertEquals(
            mutableMapOf(
                TestPm.ROOT_PM_KEY to mutableMapOf<String, Any>(
                    DEFAULT_STACK_NAVIGATOR_BACKSTACK_STATE_KEY to listOf(
                        PM1_ARGS,
                        PM2_ARGS,
                        PM3_ARGS
                    )
                ),
                "${TestPm.ROOT_PM_KEY}/${PM1_ARGS.key}" to mutableMapOf(),
                "${TestPm.ROOT_PM_KEY}/${PM2_ARGS.key}" to mutableMapOf(),
                "${TestPm.ROOT_PM_KEY}/${PM3_ARGS.key}" to mutableMapOf()
            ),
            stateSaverFactory.pmStates
        )
    }
}
