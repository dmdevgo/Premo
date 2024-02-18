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

package me.dmdev.premo

import kotlinx.coroutines.isActive
import kotlinx.coroutines.test.runTest
import me.dmdev.premo.PmLifecycle.State.CREATED
import me.dmdev.premo.PmLifecycle.State.DESTROYED
import me.dmdev.premo.PmLifecycle.State.IN_FOREGROUND
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PresentationModelTest {

    companion object {
        private const val ROOT_KEY = "root"
        private const val CHILD_KEY_1 = "child1"
        private const val CHILD_KEY_2 = "child2"
        private const val CHILD_KEY_3 = "child3"
    }

    @Test
    fun testInitialArgs() = runPmTest {
        assertNull(pm.parent)
        assertEquals(ROOT_KEY, pm.tag)
        assertEquals(TestPmFactory, pm.pmFactory)
        assertEquals(TestPmStateSaverFactory, pm.pmStateSaverFactory)
        assertEquals(listOf(), pm.children)
        assertEquals(listOf(), pm.attachedChildren)
    }

    @Test
    fun testOnCreated() = runPmTest {
        assertEquals(CREATED, pm.lifecycle.state)
        assertTrue(pm.scope.isActive)
        assertNull(pm.inForegroundScope)
    }

    @Test
    fun testOnForeground() = runPmTest {
        onForeground()

        assertEquals(IN_FOREGROUND, pm.lifecycle.state)
        assertTrue(pm.scope.isActive)
        assertNotNull(pm.inForegroundScope)
    }

    @Test
    fun testOnBackground() = runPmTest {
        onForeground()
        val inForegroundScope = pm.inForegroundScope
        onBackground()

        assertEquals(CREATED, pm.lifecycle.state)
        assertTrue(pm.scope.isActive)
        assertNotNull(inForegroundScope)
        assertFalse(inForegroundScope.isActive)
        assertNull(pm.inForegroundScope)
    }

    @Test
    fun testOnDestroy() = runPmTest {
        onForeground()
        onBackground()
        onDestroy()

        assertEquals(DESTROYED, pm.lifecycle.state)
        assertFalse(pm.scope.isActive)
        assertNull(pm.inForegroundScope)
    }

    @Test
    fun testCreateChild() = runPmTest {
        val childPm = pm.Child<TestPm>(TestPm.Args(CHILD_KEY_1))

        assertEquals(pm, childPm.parent)
        assertEquals("$ROOT_KEY/$CHILD_KEY_1", childPm.tag)
        assertEquals(pm.pmFactory, childPm.pmFactory)
        assertEquals(pm.pmStateSaverFactory, childPm.pmStateSaverFactory)
        assertEquals(CREATED, childPm.lifecycle.state)
    }

    @Test
    fun testCreateMultipleChildren() = runPmTest {
        val childPm1 = pm.Child<TestPm>(TestPm.Args(CHILD_KEY_1))
        assertEquals(listOf(childPm1), pm.children)
        assertEquals(listOf(), pm.attachedChildren)

        val childPm2 = pm.Child<TestPm>(TestPm.Args(CHILD_KEY_2))
        assertEquals(listOf(childPm1, childPm2), pm.children)
        assertEquals(listOf(), pm.attachedChildren)

        val childPm3 = pm.Child<TestPm>(TestPm.Args(CHILD_KEY_3))
        assertEquals(listOf(childPm1, childPm2, childPm3), pm.children)
        assertEquals(listOf(), pm.attachedChildren)
    }

    @Test
    fun testDestroyChildren() = runPmTest {
        val childPm1 = pm.Child<TestPm>(TestPm.Args(CHILD_KEY_1))
        val childPm2 = pm.Child<TestPm>(TestPm.Args(CHILD_KEY_2))
        val childPm3 = pm.Child<TestPm>(TestPm.Args(CHILD_KEY_3))

        childPm1.lifecycle.moveTo(DESTROYED)

        assertNull(childPm1.parent)
        assertEquals(listOf(childPm2, childPm3), pm.children)

        childPm2.lifecycle.moveTo(DESTROYED)

        assertNull(childPm2.parent)
        assertEquals(listOf(childPm3), pm.children)

        childPm3.lifecycle.moveTo(DESTROYED)

        assertNull(childPm3.parent)
        assertEquals(listOf(), pm.children)
    }

    @Test
    fun testCreateChildForDestroyedParent() = runPmTest {
        onDestroy()
        assertFailsWith<IllegalArgumentException> {
            pm.Child<TestPm>(TestPm.Args(CHILD_KEY_1))
        }
    }

    @Test
    fun testChildNotAttachedToParentLifecycle() = runPmTest {
        val childPm = pm.Child<TestPm>(TestPm.Args(CHILD_KEY_1))
        assertEquals(CREATED, childPm.lifecycle.state)
        onForeground()
        assertEquals(CREATED, childPm.lifecycle.state)
    }

    @Test
    fun testDestroyChildrenWhenTheirParentIsDestroyed() = runPmTest {
        val childPm1 = pm.Child<TestPm>(TestPm.Args(CHILD_KEY_1))
        val childPm2 = pm.Child<TestPm>(TestPm.Args(CHILD_KEY_2))

        pm.lifecycle.moveTo(DESTROYED)
        assertEquals(DESTROYED, childPm1.lifecycle.state)
        assertEquals(DESTROYED, childPm2.lifecycle.state)

        assertEquals(listOf(), pm.children)
    }

    @Test
    fun testAttachChildToParentLifecycle() = runPmTest {
        val childPm = pm.Child<TestPm>(TestPm.Args(CHILD_KEY_1))
        pm.attachChild(childPm)

        assertEquals(CREATED, childPm.lifecycle.state)
        assertEquals(listOf(childPm), pm.children)
        assertEquals(listOf(childPm), pm.attachedChildren)

        onForeground()
        assertEquals(IN_FOREGROUND, childPm.lifecycle.state)

        onBackground()
        assertEquals(CREATED, childPm.lifecycle.state)

        onDestroy()
        assertEquals(DESTROYED, childPm.lifecycle.state)

        assertEquals(listOf(), pm.children)
        assertEquals(listOf(), pm.attachedChildren)
    }

    @Test
    fun testAttachMultipleChildren() = runPmTest {
        val childPm1 = pm.Child<TestPm>(TestPm.Args(CHILD_KEY_1))
        pm.attachChild(childPm1)
        assertEquals(listOf(childPm1), pm.children)
        assertEquals(listOf(childPm1), pm.attachedChildren)

        val childPm2 = pm.Child<TestPm>(TestPm.Args(CHILD_KEY_2))
        pm.attachChild(childPm2)
        assertEquals(listOf(childPm1, childPm2), pm.children)
        assertEquals(listOf(childPm1, childPm2), pm.attachedChildren)

        val childPm3 = pm.Child<TestPm>(TestPm.Args(CHILD_KEY_3))
        pm.attachChild(childPm3)
        assertEquals(listOf(childPm1, childPm2, childPm3), pm.children)
        assertEquals(listOf(childPm1, childPm2, childPm3), pm.attachedChildren)
    }

    @Test
    fun testAttachDestroyedChild() = runPmTest {
        val childPm = pm.Child<TestPm>(TestPm.Args(CHILD_KEY_1))
        childPm.lifecycle.moveTo(DESTROYED)

        assertFailsWith<IllegalArgumentException> {
            pm.attachChild(childPm)
        }
    }

    @Test
    fun testAttachAlreadyAttachedChild() = runPmTest {
        val childPm = pm.Child<TestPm>(TestPm.Args(CHILD_KEY_1))
        pm.attachChild(childPm)

        assertFailsWith<IllegalArgumentException> {
            pm.attachChild(childPm)
        }
    }

    @Test
    fun testAttachChildToNotItsParent() = runPmTest {
        val childPm = pm.Child<TestPm>(TestPm.Args(CHILD_KEY_1))
        val childOfChild = childPm.Child<TestPm>(TestPm.Args(CHILD_KEY_2))

        assertFailsWith<IllegalArgumentException> {
            pm.attachChild(childOfChild)
        }
    }

    @Test
    fun testDetachChild() = runPmTest {
        val childPm = pm.Child<TestPm>(TestPm.Args(CHILD_KEY_1))
        assertEquals(listOf(childPm), pm.children)
        assertEquals(listOf(), pm.attachedChildren)

        pm.attachChild(childPm)
        assertEquals(listOf(childPm), pm.attachedChildren)
        assertEquals(CREATED, childPm.lifecycle.state)

        pm.detachChild(childPm)
        assertEquals(DESTROYED, childPm.lifecycle.state)

        assertEquals(listOf(), pm.children)
        assertEquals(listOf(), pm.attachedChildren)
    }

    private fun runPmTest(
        testBody: suspend PmTestContext<TestPm>.() -> Unit
    ) = runTest {
        runPmTest(
            pmArgs = TestPm.Args(ROOT_KEY),
            pmFactory = TestPmFactory,
            pmStateSaverFactory = TestPmStateSaverFactory,
            initialPmLifecycleState = CREATED,
            testBody = testBody
        )
    }
}
