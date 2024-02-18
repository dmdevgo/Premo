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

import kotlinx.coroutines.test.runTest
import me.dmdev.premo.PmLifecycle.State.DESTROYED
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PmMessageHandlerTest {

    @Test
    fun testEmptyHandler() = runPmMessageHandlerTest {
        assertFalse { pm.messageHandler.handle(TestPmMessage()) }
    }

    @Test
    fun testAddHandler() = runPmMessageHandlerTest {
        pm.messageHandler.addHandler(handlerTrue::handle)

        assertTrue { pm.messageHandler.handle(TestPmMessage()) }
        handlerTrue.assertCalledOnce()
    }

    @Test
    fun testRemoveHandler() = runPmMessageHandlerTest {
        val handler = handlerTrue::handle
        pm.messageHandler.addHandler(handler)
        pm.messageHandler.removeHandler(handler)

        assertFalse { pm.messageHandler.handle(TestPmMessage()) }
        handlerTrue.assertNotCalled()
    }

    @Test
    fun testOnMessage() = runPmMessageHandlerTest {
        pm.messageHandler.onMessage<TestPmMessage> { message ->
            handlerTrue.handle(message)
        }

        assertTrue { pm.messageHandler.handle(TestPmMessage()) }
        handlerTrue.assertCalledOnce()
    }

    @Test
    fun testHandleTrue() = runPmMessageHandlerTest {
        pm.messageHandler.handle<TestPmMessage> { message ->
            handlerTrue.handle(message)
        }

        assertTrue { pm.messageHandler.handle(TestPmMessage()) }
        handlerTrue.assertCalledOnce()
    }

    @Test
    fun testHandleFalse() = runPmMessageHandlerTest {
        pm.messageHandler.handle<TestPmMessage> { message ->
            handlerFalse.handle(message)
        }

        assertFalse { pm.messageHandler.handle(TestPmMessage()) }
        handlerFalse.assertCalledOnce()
    }

    @Test
    fun testFindRoot() = runPmMessageHandlerTest {
        assertEquals(root, root.messageHandler.findRootPm())
        assertEquals(root, child11.messageHandler.findRootPm())
        assertEquals(root, child12.messageHandler.findRootPm())
        assertEquals(root, parent1.messageHandler.findRootPm())
        assertEquals(root, child21.messageHandler.findRootPm())
        assertEquals(root, child22.messageHandler.findRootPm())
        assertEquals(root, parent2.messageHandler.findRootPm())
    }

    @Test
    fun testSendMessageHandleBySenderPm() = runPmMessageHandlerTest {
        child11.messageHandler.addHandler(handlerTrue::handle)
        assertTrue { child11.messageHandler.send(TestPmMessage()) }

        child11Handler.assertCalledOnce()
        parent1Handler.assertNotCalled()
        rootHandler.assertNotCalled()

        child12Handler.assertNotCalled()
        parent2Handler.assertNotCalled()
        child21Handler.assertNotCalled()
        child22Handler.assertNotCalled()
    }

    @Test
    fun testSendMessageHandleByParentPm() = runPmMessageHandlerTest {
        parent1.messageHandler.addHandler(handlerTrue::handle)
        assertTrue { child11.messageHandler.send(TestPmMessage()) }

        child11Handler.assertCalledOnce()
        parent1Handler.assertCalledOnce()
        rootHandler.assertNotCalled()

        child12Handler.assertNotCalled()
        parent2Handler.assertNotCalled()
        child21Handler.assertNotCalled()
        child22Handler.assertNotCalled()
    }

    @Test
    fun testSendMessageHandleByRootPm() = runPmMessageHandlerTest {
        root.messageHandler.addHandler(handlerTrue::handle)
        assertTrue { child11.messageHandler.send(TestPmMessage()) }

        child11Handler.assertCalledOnce()
        parent1Handler.assertCalledOnce()
        rootHandler.assertCalledOnce()

        child12Handler.assertNotCalled()
        parent2Handler.assertNotCalled()
        child21Handler.assertNotCalled()
        child22Handler.assertNotCalled()
    }

    @Test
    fun testSendMessageNotHandledByAnyPm() = runPmMessageHandlerTest {
        assertFalse { child11.messageHandler.send(TestPmMessage()) }

        child11Handler.assertCalledOnce()
        parent1Handler.assertCalledOnce()
        rootHandler.assertCalledOnce()

        child12Handler.assertNotCalled()
        parent2Handler.assertNotCalled()
        child21Handler.assertNotCalled()
        child22Handler.assertNotCalled()
    }

    @Test
    fun testSendMessageFromDestroyedPm() = runPmMessageHandlerTest {
        child11.lifecycle.moveTo(DESTROYED)
        assertFalse { child11.messageHandler.send(TestPmMessage()) }

        child11Handler.assertNotCalled()
        parent1Handler.assertNotCalled()
        rootHandler.assertNotCalled()
        child12Handler.assertNotCalled()
        parent2Handler.assertNotCalled()
        child21Handler.assertNotCalled()
        child22Handler.assertNotCalled()
    }

    @Test
    fun testSendTargetMessage() = runPmMessageHandlerTest {
        child22.messageHandler.addHandler(handlerTrue::handle)

        assertTrue { child11.messageHandler.sendToTarget(TestPmMessage(), child22.tag) }

        child11Handler.assertNotCalled()
        parent1Handler.assertNotCalled()
        rootHandler.assertNotCalled()
        child12Handler.assertNotCalled()
        parent2Handler.assertNotCalled()
        child21Handler.assertNotCalled()
        child22Handler.assertCalledOnce() // Target
    }

    @Test
    fun testSendTargetMessageToUnknownPm() = runPmMessageHandlerTest {
        assertFalse { child11.messageHandler.sendToTarget(TestPmMessage(), "foo") }

        child11Handler.assertNotCalled()
        parent1Handler.assertNotCalled()
        rootHandler.assertNotCalled()
        child12Handler.assertNotCalled()
        parent2Handler.assertNotCalled()
        child21Handler.assertNotCalled()
        child22Handler.assertNotCalled()
    }

    @Test
    fun testSendTargetMessageFromDestroyedPm() = runPmMessageHandlerTest {
        child11.lifecycle.moveTo(DESTROYED)

        assertFalse { child11.messageHandler.sendToTarget(TestPmMessage(), child22.tag) }

        child11Handler.assertNotCalled()
        parent1Handler.assertNotCalled()
        rootHandler.assertNotCalled()
        child12Handler.assertNotCalled()
        parent2Handler.assertNotCalled()
        child21Handler.assertNotCalled()
        child22Handler.assertNotCalled()
    }

    @Test
    fun testSendToChildren() {
        // TODO
    }

    private class TestPmMessage : PmMessage

    private class TestMessageHandler(
        private val handleResult: Boolean
    ) : TestCallback() {
        fun handle(message: PmMessage): Boolean {
            call()
            return handleResult
        }
    }

    private class PmMessageHandlerTestContext(
        private val pmTestContext: PmTestContext<TestPm>
    ) : PmTestContext<TestPm> by pmTestContext {

        val handlerTrue = TestMessageHandler(true)
        val handlerFalse = TestMessageHandler(false)

        val root = pm
        val rootHandler = TestMessageHandler(false)

        val parent1 = root.Child<TestPm>(TestPm.Args("parent1"))
        val parent1Handler = TestMessageHandler(false)

        val child11 = parent1.Child<TestPm>(TestPm.Args("child11"))
        val child11Handler = TestMessageHandler(false)

        val child12 = parent1.Child<TestPm>(TestPm.Args("child12"))
        val child12Handler = TestMessageHandler(false)

        val parent2 = root.Child<TestPm>(TestPm.Args("parent2"))
        val parent2Handler = TestMessageHandler(false)

        val child21 = parent2.Child<TestPm>(TestPm.Args("child21"))
        val child21Handler = TestMessageHandler(false)

        val child22 = parent2.Child<TestPm>(TestPm.Args("child22"))
        val child22Handler = TestMessageHandler(false)

        init {
            root.messageHandler.addHandler(rootHandler::handle)
            parent1.messageHandler.addHandler(parent1Handler::handle)
            child11.messageHandler.addHandler(child11Handler::handle)
            child12.messageHandler.addHandler(child12Handler::handle)
            parent2.messageHandler.addHandler(parent2Handler::handle)
            child21.messageHandler.addHandler(child21Handler::handle)
            child22.messageHandler.addHandler(child22Handler::handle)
        }
    }

    private fun runPmMessageHandlerTest(
        testBody: PmMessageHandlerTestContext.() -> Unit
    ) = runTest {
        runPmTest(
            pmArgs = TestPm.Args("root"),
            pmFactory = TestPmFactory,
            pmStateSaverFactory = TestPmStateSaverFactory,
            initialPmLifecycleState = PmLifecycle.State.CREATED
        ) {
            val testContext = PmMessageHandlerTestContext(this)
            testBody.invoke(testContext)
        }
    }
}
