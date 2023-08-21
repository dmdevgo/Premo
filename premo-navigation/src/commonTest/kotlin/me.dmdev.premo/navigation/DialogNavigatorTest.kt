/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020-2023 Dmitriy Gorbunov (dmitriy.goto@gmail.com)
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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import me.dmdev.premo.PmLifecycle.State.DESTROYED
import me.dmdev.premo.PmLifecycle.State.IN_FOREGROUND
import me.dmdev.premo.navigation.TestPm.ResultMessage
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class DialogNavigatorTest {

    companion object {
        private const val DIALOG_KEY = "dialog"
    }

    private lateinit var parentPm: TestPm
    private lateinit var dialogPm: TestPm
    private lateinit var dialogPm2: TestPm
    private lateinit var navigator: DialogNavigator<TestPm, ResultMessage>

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        parentPm = TestPm.buildRootPm()
        parentPm.lifecycle.moveTo(IN_FOREGROUND)
        dialogPm = parentPm.Child(TestPm.PM1_DESCRIPTION)
        dialogPm2 = parentPm.Child(TestPm.PM2_DESCRIPTION)
        navigator = parentPm.DialogNavigator(DIALOG_KEY)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState() {
        assertEquals(null, navigator.dialog)
        assertEquals(null, navigator.dialogFlow.value)
    }

    @Test
    fun testShowDialog() {
        navigator.show(dialogPm)
        assertEquals(dialogPm, navigator.dialog)
        assertEquals(dialogPm, navigator.dialogFlow.value)
        assertEquals(IN_FOREGROUND, dialogPm.lifecycle.state)
    }

    @Test
    fun testReplaceDialog() {
        navigator.show(dialogPm)
        navigator.show(dialogPm2)

        assertEquals(DESTROYED, dialogPm.lifecycle.state)
        assertEquals(dialogPm2, navigator.dialog)
        assertEquals(dialogPm2, navigator.dialogFlow.value)
        assertEquals(IN_FOREGROUND, dialogPm2.lifecycle.state)
    }

    @Test
    fun testDismissDialog() {
        navigator.show(dialogPm)
        navigator.dismiss()

        assertEquals(DESTROYED, dialogPm.lifecycle.state)
        assertEquals(null, navigator.dialog)
        assertEquals(null, navigator.dialogFlow.value)
    }

    @Test
    fun testResultDialog() {
        var result: ResultMessage? = null
        val navigator: DialogNavigator<TestPm, ResultMessage> =
            parentPm.DialogNavigator(DIALOG_KEY) { resultMessage ->
                result = resultMessage
            }

        navigator.show(dialogPm)
        dialogPm.sendResultMessage(ResultMessage.Ok)

        assertEquals(ResultMessage.Ok, result)
        assertEquals(null, navigator.dialog)
        assertEquals(null, navigator.dialogFlow.value)
        assertEquals(DESTROYED, dialogPm.lifecycle.state)
    }

    @Test
    fun testRestoreState() {
        val stateSaverFactory = TestStateSaverFactory(
            initialState = mutableMapOf(
                TestPm.ROOT_PM_KEY to mutableMapOf(
                    DIALOG_KEY to TestPm.PM1_DESCRIPTION
                ),
                "${TestPm.ROOT_PM_KEY}/${TestPm.PM1_DESCRIPTION.key}" to mutableMapOf()
            )
        )

        val parentPm = TestPm.buildRootPm(stateSaverFactory)
        parentPm.lifecycle.moveTo(IN_FOREGROUND)
        val navigator = parentPm.DialogNavigator<TestPm, ResultMessage>(DIALOG_KEY)

        assertEquals(TestPm.PM1_DESCRIPTION, navigator.dialog?.description)
        assertEquals(TestPm.PM1_DESCRIPTION, navigator.dialogFlow.value?.description)
        assertEquals(IN_FOREGROUND, navigator.dialog?.lifecycle?.state)
    }

    @Test
    fun testSaveState() {
        val stateSaverFactory = TestStateSaverFactory()

        val parentPm = TestPm.buildRootPm(stateSaverFactory)
        val dialogPm = parentPm.Child<TestPm>(TestPm.PM1_DESCRIPTION)
        val navigator = parentPm.DialogNavigator<TestPm, ResultMessage>(DIALOG_KEY)

        parentPm.lifecycle.moveTo(IN_FOREGROUND)
        navigator.show(dialogPm)
        parentPm.stateHandler.saveState()

        assertEquals(
            mutableMapOf(
                TestPm.ROOT_PM_KEY to mutableMapOf<String, Any>(
                    DIALOG_KEY to TestPm.PM1_DESCRIPTION
                ),
                "${TestPm.ROOT_PM_KEY}/${TestPm.PM1_DESCRIPTION.key}" to mutableMapOf()
            ),
            stateSaverFactory.pmStates
        )
    }
}
