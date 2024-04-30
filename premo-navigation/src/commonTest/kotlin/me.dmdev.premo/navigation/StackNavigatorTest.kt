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

import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
import me.dmdev.premo.navigation.stack.DEFAULT_STACK_NAVIGATOR_BACKSTACK_STATE_KEY
import me.dmdev.premo.navigation.stack.StackNavigator
import me.dmdev.premo.navigation.stack.handleBack
import me.dmdev.premo.navigation.stack.pop
import me.dmdev.premo.navigation.stack.popToRoot
import me.dmdev.premo.navigation.stack.popUntil
import me.dmdev.premo.navigation.stack.push
import me.dmdev.premo.navigation.stack.replaceAll
import me.dmdev.premo.navigation.stack.replaceTop
import me.dmdev.premo.saver.NoPmStateSaverFactory
import me.dmdev.premo.saver.PmStateSaverFactory
import me.dmdev.premo.test.PmTestContext
import me.dmdev.premo.test.runPmTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StackNavigatorTest {

    @Test
    fun testEmptyNavigator() = runStackNavigationTest {
        assertNull(navigator.currentTop)
        assertTrue(navigator.backStack.isEmpty())
        assertTrue(navigator.backStackFlow.value.isEmpty())
    }

    @Test
    fun testInitialPm() = runStackNavigationTest {
        val navigator = pm.StackNavigator(
            initBackStack = {
                listOf(pm1, pm2, pm3)
            },
            key = "stack_navigator_2"
        )

        assertEquals(
            listOf(CREATED, CREATED, IN_FOREGROUND),
            listOf(pm1, pm2, pm3).map {
                it.lifecycle.state
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
    fun testPushOnePm() = runStackNavigationTest {
        navigator.push(pm1)

        assertEquals(pm1, navigator.currentTop)
        assertEquals(pm1.lifecycle.state, IN_FOREGROUND)
        assertEquals(listOf(pm1), navigator.backStack)
    }

    @Test
    fun testPushTwoPm() = runStackNavigationTest {
        navigator.push(pm1)
        navigator.push(pm2)

        assertEquals(pm2, navigator.currentTop)
        assertEquals(CREATED, pm1.lifecycle.state)
        assertEquals(IN_FOREGROUND, pm2.lifecycle.state)
        assertEquals(listOf(pm1, pm2), navigator.backStack)
    }

    @Test
    fun testPopEmpty() = runStackNavigationTest {
        navigator.pop()

        assertNull(navigator.currentTop)
        assertTrue(navigator.backStack.isEmpty())
        assertTrue(navigator.backStackFlow.value.isEmpty())
    }

    @Test
    fun testPushOnePmAndThenPop() = runStackNavigationTest {
        navigator.push(pm1)
        navigator.pop()

        assertNull(navigator.currentTop)
        assertEquals(DESTROYED, pm1.lifecycle.state)
        assertTrue(navigator.backStack.isEmpty())
        assertTrue(navigator.backStackFlow.value.isEmpty())
    }

    @Test
    fun testPopSecondPm() = runStackNavigationTest {
        navigator.push(pm1)
        navigator.push(pm2)
        navigator.pop()

        assertEquals(pm1, navigator.currentTop)
        assertEquals(IN_FOREGROUND, pm1.lifecycle.state)
        assertEquals(DESTROYED, pm2.lifecycle.state)
        assertEquals(listOf(pm1), navigator.backStack)
    }

    @Test
    fun testPopToRoot() = runStackNavigationTest {
        navigator.changeBackStack(listOf(pm1, pm2, pm3))

        assertTrue { navigator.popToRoot() }
        assertEquals(pm1, navigator.currentTop)
        assertEquals(IN_FOREGROUND, pm1.lifecycle.state)
        assertEquals(DESTROYED, pm2.lifecycle.state)
        assertEquals(DESTROYED, pm3.lifecycle.state)
        assertEquals(listOf(pm1), navigator.backStack)
    }

    @Test
    fun testPopToRootWhenBackstackIsEmpty() = runStackNavigationTest {
        assertFalse { navigator.popToRoot() }
        assertEquals(listOf(), navigator.backStack)
    }

    @Test
    fun testReplaceTop() = runStackNavigationTest {
        navigator.changeBackStack(listOf(pm1, pm2))
        navigator.replaceTop(pm3)

        assertEquals(CREATED, pm1.lifecycle.state)
        assertEquals(DESTROYED, pm2.lifecycle.state)
        assertEquals(IN_FOREGROUND, pm3.lifecycle.state)
        assertEquals(pm3, navigator.currentTop)
        assertEquals(listOf(pm1, pm3), navigator.backStack)
    }

    @Test
    fun testReplaceTopWhenBackstackIsEmpty() = runStackNavigationTest {
        navigator.replaceTop(pm3)

        assertEquals(pm3, navigator.currentTop)
        assertEquals(IN_FOREGROUND, pm3.lifecycle.state)
        assertEquals(listOf(pm3), navigator.backStack)
    }

    @Test
    fun testReplaceAll() = runStackNavigationTest {
        navigator.changeBackStack(listOf(pm1, pm2))
        navigator.replaceAll(pm3)

        assertEquals(DESTROYED, pm1.lifecycle.state)
        assertEquals(DESTROYED, pm2.lifecycle.state)
        assertEquals(IN_FOREGROUND, pm3.lifecycle.state)
        assertEquals(pm3, navigator.currentTop)
        assertEquals(listOf(pm3), navigator.backStack)
    }

    @Test
    fun testPopUntil() = runStackNavigationTest {
        navigator.changeBackStack(listOf(pm1, pm2, pm3))
        navigator.popUntil { it === pm1 }

        assertEquals(pm1, navigator.currentTop)
        assertEquals(IN_FOREGROUND, pm1.lifecycle.state)
        assertEquals(DESTROYED, pm2.lifecycle.state)
        assertEquals(DESTROYED, pm3.lifecycle.state)
        assertEquals(listOf(pm1), navigator.backStack)
    }

    @Test
    fun testSetBackstack() = runStackNavigationTest {
        val backstack = listOf(pm1, pm2, pm3)
        navigator.changeBackStack(backstack)

        assertEquals(CREATED, pm1.lifecycle.state)
        assertEquals(CREATED, pm2.lifecycle.state)
        assertEquals(IN_FOREGROUND, pm3.lifecycle.state)
        assertEquals(backstack, navigator.backStack)
    }

    @Test
    fun testHandleBackWhenTwoPmInBackstack() = runStackNavigationTest {
        navigator.push(pm1)
        navigator.push(pm2)

        assertTrue { navigator.handleBack() }
        assertEquals(pm1, navigator.currentTop)
        assertEquals(IN_FOREGROUND, pm1.lifecycle.state)
        assertEquals(DESTROYED, pm2.lifecycle.state)
    }

    @Test
    fun testHandleBackWhenOnePmInBackstack() = runStackNavigationTest {
        navigator.push(pm1)

        assertFalse { navigator.handleBack() }
        assertEquals(pm1, navigator.currentTop)
        assertEquals(IN_FOREGROUND, pm1.lifecycle.state)
    }

    @Test
    fun testHandleBackWhenBackstackIsEmpty() = runStackNavigationTest {
        assertFalse { navigator.handleBack() }
    }

    @Test
    fun testMoveTopPmToCreatedWhenLifecycleMoveToCreated() = runStackNavigationTest {
        navigator.push(pm1)
        navigator.push(pm2)
        onBackground()

        assertEquals(CREATED, pm1.lifecycle.state)
        assertEquals(CREATED, pm2.lifecycle.state)
    }

    @Test
    fun testMoveTopPmToInForegroundWhenLifecycleMoveToInForeground() = runStackNavigationTest {
        navigator.push(pm1)
        navigator.push(pm2)

        assertEquals(CREATED, pm1.lifecycle.state)
        assertEquals(IN_FOREGROUND, pm2.lifecycle.state)
    }

    @Test
    fun testMoveAllPmToDestroyedWhenLifecycleMoveToDestroyed() = runStackNavigationTest {
        navigator.push(pm1)
        navigator.push(pm2)
        onBackground()
        onDestroy()

        assertEquals(DESTROYED, pm1.lifecycle.state)
        assertEquals(DESTROYED, pm2.lifecycle.state)
    }

    @Test
    fun testBackStackChangesFlow() {
        // TODO
    }

    @Test
    fun testRestoreState() = runStackNavigationTest(
        pmStateSaverFactory = TestStateSaverFactory(
            initialState = mutableMapOf(
                ROOT_PM_ARGS.key to mutableMapOf(
                    DEFAULT_STACK_NAVIGATOR_BACKSTACK_STATE_KEY to listOf(
                        PM4_ARGS,
                        PM5_ARGS,
                        PM6_ARGS
                    )
                )
            )
        )
    ) {
        assertEquals(
            listOf(CREATED, CREATED, IN_FOREGROUND),
            navigator.backStack.map { it.lifecycle.state }
        )

        assertEquals(
            listOf(PM4_ARGS, PM5_ARGS, PM6_ARGS),
            navigator.backStack.map { it.pmArgs }
        )
    }

    @Test
    fun testSaveState() {
        val stateSaverFactory = TestStateSaverFactory()

        runStackNavigationTest(
            pmStateSaverFactory = stateSaverFactory
        ) {
            navigator.changeBackStack(listOf(pm1, pm2, pm3))
            onSave()

            assertEquals(
                mutableMapOf(
                    ROOT_PM_ARGS.key to mutableMapOf<String, Any>(
                        DEFAULT_STACK_NAVIGATOR_BACKSTACK_STATE_KEY to listOf(
                            PM1_ARGS,
                            PM2_ARGS,
                            PM3_ARGS
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

    private class StackNavigationTestContext(
        private val pmTestContext: PmTestContext<TestPm>,
        val testCoroutineScheduler: TestCoroutineScheduler
    ) : PmTestContext<TestPm> by pmTestContext {
        val pm1 = pm.Child<TestPm>(PM1_ARGS)
        val pm2 = pm.Child<TestPm>(PM2_ARGS)
        val pm3 = pm.Child<TestPm>(PM3_ARGS)
        val navigator: StackNavigator = pm.StackNavigator()
    }

    private fun runStackNavigationTest(
        pmStateSaverFactory: PmStateSaverFactory = NoPmStateSaverFactory,
        testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
        testBody: StackNavigationTestContext.() -> Unit
    ) = runTest(context = testDispatcher) {
        runPmTest(
            pmArgs = ROOT_PM_ARGS,
            pmFactory = TestPmFactory,
            pmStateSaverFactory = pmStateSaverFactory,
            testDispatcher = testDispatcher
        ) {
            val testContext = StackNavigationTestContext(this, testScheduler)
            testBody.invoke(testContext)
        }
    }
}
