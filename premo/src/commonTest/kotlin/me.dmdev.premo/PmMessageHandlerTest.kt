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

import me.dmdev.premo.saver.NoPmStateSaverFactory
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PmMessageHandlerTest {

    private lateinit var parentHandler: PmMessageHandler
    private lateinit var handler: PmMessageHandler

    private object TestPmMessage : PmMessage

    @BeforeTest
    fun setUp() {
        val parentPm = TestPm()
        val childPm = TestPm(
            TestPm.Args().apply {
                parent = parentPm
                overridePmFactory(TestPmFactory())
                overridePmStateSaverFactory(NoPmStateSaverFactory)
            }
        )
        parentHandler = parentPm.messageHandler
        handler = PmMessageHandler(childPm)
    }

    @Test
    fun testEmptyHandler() {
        assertFalse(handler.handle(TestPmMessage))
    }

    @Test
    fun testHandleMessage() {
        handler.addHandler { message -> message is TestPmMessage }
        assertTrue(handler.handle(TestPmMessage))
    }

    @Test
    fun testSendMessage() {
        var handled = false
        parentHandler.addHandler { message ->
            if (message is TestPmMessage) handled = true
            handled
        }
        handler.send(TestPmMessage)
        assertTrue(handled)
    }

    @Test
    fun testOnMessage() {
        var handled = false
        handler.onMessage<TestPmMessage> { handled = true }
        handler.handle(TestPmMessage)
        assertTrue(handled)
    }

    @Test
    fun testRemoveHandler() {
        val messageHandler: (message: PmMessage) -> Boolean = { true }
        handler.addHandler(messageHandler)
        handler.removeHandler(messageHandler)
        assertFalse(handler.handle(TestPmMessage))
    }
}
