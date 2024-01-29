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
import me.dmdev.premo.navigation.TestPm.Companion.PM1_ARGS
import me.dmdev.premo.navigation.TestPm.Companion.PM2_ARGS
import me.dmdev.premo.navigation.TestPm.Companion.PM3_ARGS
import me.dmdev.premo.navigation.TestPm.Companion.ROOT_PM_ARGS
import me.dmdev.premo.navigation.TestPm.ResultMessage
import me.dmdev.premo.navigation.dialog.DialogNavigator
import me.dmdev.premo.navigation.dialog.handleBack
import me.dmdev.premo.navigation.dialog.isShowing
import me.dmdev.premo.saver.NoPmStateSaverFactory
import me.dmdev.premo.saver.PmStateSaverFactory
import me.dmdev.premo.test.PmTestContext
import me.dmdev.premo.test.runPmTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DialogNavigatorTest {

    @Test
    fun testInitialState() = runDialogNavigationTest {
        assertNull(navigator.dialog)
        assertNull(navigator.dialogFlow.value)
        assertFalse { navigator.isShowing }
    }

    @Test
    fun testShowDialog() = runDialogNavigationTest {
        navigator.show(dialogPm)

        assertEquals(dialogPm, navigator.dialog)
        assertEquals(dialogPm, navigator.dialogFlow.value)
        assertEquals(IN_FOREGROUND, dialogPm.lifecycle.state)
        assertTrue { navigator.isShowing }
    }

    @Test
    fun testReplaceDialog() = runDialogNavigationTest {
        navigator.show(dialogPm)
        navigator.show(dialogPm2)

        assertEquals(DESTROYED, dialogPm.lifecycle.state)
        assertEquals(dialogPm2, navigator.dialog)
        assertEquals(dialogPm2, navigator.dialogFlow.value)
        assertEquals(IN_FOREGROUND, dialogPm2.lifecycle.state)
        assertTrue { navigator.isShowing }
    }

    @Test
    fun testDismissDialog() = runDialogNavigationTest {
        navigator.show(dialogPm)
        navigator.dismiss()

        assertEquals(DESTROYED, dialogPm.lifecycle.state)
        assertNull(navigator.dialog)
        assertNull(navigator.dialogFlow.value)
        assertFalse { navigator.isShowing }
    }

    @Test
    fun testHandleBack() = runDialogNavigationTest {
        navigator.show(dialogPm)

        assertTrue { navigator.handleBack() }

        assertEquals(DESTROYED, dialogPm.lifecycle.state)
        assertNull(navigator.dialog)
        assertNull(navigator.dialogFlow.value)
        assertFalse { navigator.isShowing }
    }

    @Test
    fun testHandleBackForEmptyNavigator() = runDialogNavigationTest {
        assertFalse { navigator.handleBack() }
    }

    @Test
    fun testResultDialog() = runDialogNavigationTest {
        var result: ResultMessage? = null
        val navigator: DialogNavigator<TestPm, ResultMessage> =
            pm.DialogNavigator(DIALOG_NAVIGATOR_KEY_2) { resultMessage ->
                result = resultMessage
            }

        navigator.show(dialogPm)
        dialogPm.sendResultMessage(ResultMessage.Ok)

        assertEquals(ResultMessage.Ok, result)
        assertNull(navigator.dialog)
        assertNull(navigator.dialogFlow.value)
        assertEquals(DESTROYED, dialogPm.lifecycle.state)
        assertFalse { navigator.isShowing }
    }

    @Test
    fun testRestoreState() = runDialogNavigationTest(
        pmStateSaverFactory = TestStateSaverFactory(
            initialState = mutableMapOf(
                ROOT_PM_ARGS.key to mutableMapOf(
                    DIALOG_NAVIGATOR_KEY to PM3_ARGS
                ),
                "${ROOT_PM_ARGS.key}/${PM3_ARGS.key}" to mutableMapOf()
            )
        )
    ) {
        assertEquals(PM3_ARGS, navigator.dialog?.pmArgs)
        assertEquals(PM3_ARGS, navigator.dialogFlow.value?.pmArgs)
        assertEquals(IN_FOREGROUND, navigator.dialog?.lifecycle?.state)
        assertTrue { navigator.isShowing }
    }

    @Test
    fun testSaveState() {
        val stateSaverFactory = TestStateSaverFactory()

        runDialogNavigationTest(
            pmStateSaverFactory = stateSaverFactory
        ) {
            navigator.show(dialogPm)
            onSave()

            assertEquals(
                mutableMapOf(
                    ROOT_PM_ARGS.key to mutableMapOf<String, Any>(
                        DIALOG_NAVIGATOR_KEY to PM1_ARGS
                    ),
                    "${ROOT_PM_ARGS.key}/${PM1_ARGS.key}" to mutableMapOf(),
                    "${ROOT_PM_ARGS.key}/${PM2_ARGS.key}" to mutableMapOf()
                ),
                stateSaverFactory.pmStates
            )
        }
    }

    companion object {
        private const val DIALOG_NAVIGATOR_KEY = "dialog"
        private const val DIALOG_NAVIGATOR_KEY_2 = "dialog_2"
    }

    private class DialogNavigationTestContext(
        private val pmTestContext: PmTestContext<TestPm>
    ) : PmTestContext<TestPm> by pmTestContext {
        val dialogPm = pm.Child<TestPm>(PM1_ARGS)
        val dialogPm2 = pm.Child<TestPm>(PM2_ARGS)
        val navigator = pm.DialogNavigator<TestPm, ResultMessage>(DIALOG_NAVIGATOR_KEY)
    }

    private fun runDialogNavigationTest(
        pmStateSaverFactory: PmStateSaverFactory = NoPmStateSaverFactory,
        testBody: DialogNavigationTestContext.() -> Unit
    ) = runTest {
        runPmTest(
            pmArgs = ROOT_PM_ARGS,
            pmFactory = TestPmFactory,
            pmStateSaverFactory = pmStateSaverFactory
        ) {
            val testContext = DialogNavigationTestContext(this)
            testBody.invoke(testContext)
        }
    }
}
