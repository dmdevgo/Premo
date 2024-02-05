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
import me.dmdev.premo.navigation.TestPm.Companion.PM1_ARGS
import me.dmdev.premo.navigation.TestPm.Companion.PM2_ARGS
import me.dmdev.premo.navigation.TestPm.Companion.PM3_ARGS
import me.dmdev.premo.navigation.TestPm.Companion.PM4_ARGS
import me.dmdev.premo.navigation.TestPm.Companion.PM5_ARGS
import me.dmdev.premo.navigation.TestPm.Companion.PM6_ARGS
import me.dmdev.premo.navigation.TestPm.Companion.ROOT_PM_ARGS
import me.dmdev.premo.navigation.dialog.DEFAULT_DIALOG_GROUP_NAVIGATION_KEY
import me.dmdev.premo.navigation.dialog.DialogGroupNavigator
import me.dmdev.premo.navigation.dialog.DialogNavigator
import me.dmdev.premo.navigation.dialog.handleBack
import me.dmdev.premo.saver.NoPmStateSaverFactory
import me.dmdev.premo.saver.PmStateSaverFactory
import me.dmdev.premo.test.PmTestContext
import me.dmdev.premo.test.runPmTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DialogGroupNavigatorTest {

    @Test
    fun testInitialState() = runDialogGroupNavigationTest {
        val expectedDialogs = listOf<TestPm>()
        assertEquals(expectedDialogs, dialogGroupNavigator.dialogs)
        assertEquals(expectedDialogs, dialogGroupNavigator.dialogsFlow.value)
    }

    @Test
    fun testShowDialogs() = runDialogGroupNavigationTest {
        navigator1.show(dialogPm1)
        navigator2.show(dialogPm2)
        navigator3.show(dialogPm3)

        val expectedDialogs = listOf(dialogPm1, dialogPm2, dialogPm3)
        assertEquals(expectedDialogs, dialogGroupNavigator.dialogs)
        assertEquals(expectedDialogs, dialogGroupNavigator.dialogsFlow.value)
        assertEquals(dialogPm3, dialogGroupNavigator.dialog)
        assertEquals(dialogPm3, dialogGroupNavigator.dialogFlow.value)
    }

    @Test
    fun testDismissDialog() = runDialogGroupNavigationTest {
        navigator1.show(dialogPm1)
        navigator2.show(dialogPm2)
        navigator3.show(dialogPm3)
        navigator2.dismiss()

        val expectedDialogs = listOf(dialogPm1, dialogPm3)
        assertEquals(expectedDialogs, dialogGroupNavigator.dialogs)
        assertEquals(expectedDialogs, dialogGroupNavigator.dialogsFlow.value)
        assertEquals(dialogPm3, dialogGroupNavigator.dialog)
        assertEquals(dialogPm3, dialogGroupNavigator.dialogFlow.value)
    }

    @Test
    fun testDismissAllDialogs() = runDialogGroupNavigationTest {
        navigator1.show(dialogPm1)
        navigator2.show(dialogPm2)
        navigator3.show(dialogPm3)
        dialogGroupNavigator.dismissAll()

        assertEquals(listOf(), dialogGroupNavigator.dialogs)
        assertEquals(listOf(), dialogGroupNavigator.dialogsFlow.value)
        assertNull(dialogGroupNavigator.dialog)
        assertNull(dialogGroupNavigator.dialogFlow.value)
    }

    @Test
    fun testHandleBack() = runDialogGroupNavigationTest {
        navigator1.show(dialogPm1)
        navigator2.show(dialogPm2)
        navigator3.show(dialogPm3)

        assertTrue { dialogGroupNavigator.handleBack() }

        val expectedDialogs = listOf(dialogPm1, dialogPm2)
        assertNull(navigator3.dialog)
        assertEquals(expectedDialogs, dialogGroupNavigator.dialogs)
        assertEquals(expectedDialogs, dialogGroupNavigator.dialogsFlow.value)
        assertEquals(dialogPm2, dialogGroupNavigator.dialog)
        assertEquals(dialogPm2, dialogGroupNavigator.dialogFlow.value)
    }

    @Test
    fun testHandleBackForEmptyNavigator() = runDialogGroupNavigationTest {
        assertFalse { dialogGroupNavigator.handleBack() }
    }

    @Test
    fun testRestoreState() = runDialogGroupNavigationTest(
        pmStateSaverFactory = TestStateSaverFactory(
            initialState = mutableMapOf(
                ROOT_PM_ARGS.key to mutableMapOf(
                    DIALOG_KEY1 to PM4_ARGS,
                    DIALOG_KEY2 to PM5_ARGS,
                    DIALOG_KEY3 to PM6_ARGS,
                    DEFAULT_DIALOG_GROUP_NAVIGATION_KEY to listOf(
                        PM4_ARGS,
                        PM5_ARGS,
                        PM6_ARGS
                    )
                )
            )
        )
    ) {
        val expectedDialogs = listOf(PM4_ARGS, PM5_ARGS, PM6_ARGS)

        assertEquals(expectedDialogs, dialogGroupNavigator.dialogs.map { it.pmArgs })
        assertEquals(expectedDialogs, dialogGroupNavigator.dialogsFlow.value.map { it.pmArgs })
    }

    @Test
    fun testSaveState() {
        val stateSaverFactory = TestStateSaverFactory()

        runDialogGroupNavigationTest(
            pmStateSaverFactory = stateSaverFactory
        ) {
            navigator1.show(dialogPm1)
            navigator3.show(dialogPm3)
            navigator2.show(dialogPm2)
            onSave()

            assertEquals(
                mutableMapOf(
                    ROOT_PM_ARGS.key to mutableMapOf(
                        DIALOG_KEY1 to PM1_ARGS,
                        DIALOG_KEY2 to PM2_ARGS,
                        DIALOG_KEY3 to PM3_ARGS,
                        DEFAULT_DIALOG_GROUP_NAVIGATION_KEY to listOf(
                            PM1_ARGS,
                            PM3_ARGS,
                            PM2_ARGS
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

    companion object {
        private const val DIALOG_KEY1 = "dialog1"
        private const val DIALOG_KEY2 = "dialog2"
        private const val DIALOG_KEY3 = "dialog3"
    }

    private class DialogGroupNavigationTestContext(
        private val pmTestContext: PmTestContext<TestPm>
    ) : PmTestContext<TestPm> by pmTestContext {
        val dialogPm1 = pm.Child<TestPm>(PM1_ARGS)
        val dialogPm2 = pm.Child<TestPm>(PM2_ARGS)
        val dialogPm3 = pm.Child<TestPm>(PM3_ARGS)
        val navigator1 = pm.DialogNavigator<TestPm, TestPm.ResultMessage>(DIALOG_KEY1)
        val navigator2 = pm.DialogNavigator<TestPm, TestPm.ResultMessage>(DIALOG_KEY2)
        val navigator3 = pm.DialogNavigator<TestPm, TestPm.ResultMessage>(DIALOG_KEY3)
        val dialogGroupNavigator =
            pm.DialogGroupNavigator(listOf(navigator1, navigator2, navigator3))
    }

    private fun runDialogGroupNavigationTest(
        pmStateSaverFactory: PmStateSaverFactory = NoPmStateSaverFactory,
        testBody: DialogGroupNavigationTestContext.() -> Unit
    ) = runTest {
        runPmTest(
            pmArgs = ROOT_PM_ARGS,
            pmFactory = TestPmFactory,
            pmStateSaverFactory = pmStateSaverFactory
        ) {
            val testContext = DialogGroupNavigationTestContext(this)
            testBody.invoke(testContext)
        }
    }
}
