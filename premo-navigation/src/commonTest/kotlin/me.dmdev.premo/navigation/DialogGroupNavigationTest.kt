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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import me.dmdev.premo.PmLifecycle
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.navigation.dialog.DEFAULT_DIALOG_GROUP_NAVIGATION_KEY
import me.dmdev.premo.navigation.dialog.DialogGroupNavigation
import me.dmdev.premo.navigation.dialog.DialogNavigator
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class DialogGroupNavigationTest {

    companion object {
        private const val DIALOG_KEY1 = "dialog1"
        private const val DIALOG_KEY2 = "dialog2"
        private const val DIALOG_KEY3 = "dialog3"
    }

    private lateinit var stateSaverFactory: TestStateSaverFactory
    private lateinit var parentPm: TestPm
    private lateinit var dialogPm1: TestPm
    private lateinit var dialogPm2: TestPm
    private lateinit var dialogPm3: TestPm
    private lateinit var navigator1: DialogNavigator<TestPm, TestPm.ResultMessage>
    private lateinit var navigator2: DialogNavigator<TestPm, TestPm.ResultMessage>
    private lateinit var navigator3: DialogNavigator<TestPm, TestPm.ResultMessage>
    private lateinit var dialogGroupNavigation: DialogGroupNavigation

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        stateSaverFactory = TestStateSaverFactory()
        parentPm = TestPm.buildRootPm(stateSaverFactory)
        parentPm.lifecycle.moveTo(PmLifecycle.State.IN_FOREGROUND)
        dialogPm1 = parentPm.Child(TestPm.PM1_ARGS)
        dialogPm2 = parentPm.Child(TestPm.PM2_ARGS)
        dialogPm3 = parentPm.Child(TestPm.PM3_ARGS)
        navigator1 = parentPm.DialogNavigator(DIALOG_KEY1)
        navigator2 = parentPm.DialogNavigator(DIALOG_KEY2)
        navigator3 = parentPm.DialogNavigator(DIALOG_KEY3)
        dialogGroupNavigation = parentPm.DialogGroupNavigation(navigator1, navigator2, navigator3)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState() {
        val expectedDialogs = listOf<TestPm>()
        assertEquals(expectedDialogs, dialogGroupNavigation.dialogs)
        assertEquals(expectedDialogs, dialogGroupNavigation.dialogsFlow.value)
    }

    @Test
    fun testShowDialogs() {
        navigator1.show(dialogPm1)
        navigator2.show(dialogPm2)
        navigator3.show(dialogPm3)

        val expectedDialogs = listOf(dialogPm1, dialogPm2, dialogPm3)
        assertEquals(expectedDialogs, dialogGroupNavigation.dialogs)
        assertEquals(expectedDialogs, dialogGroupNavigation.dialogsFlow.value)
    }

    @Test
    fun testDismissDialog() {
        navigator1.show(dialogPm1)
        navigator2.show(dialogPm2)
        navigator3.show(dialogPm3)
        navigator2.dismiss()

        val expectedDialogs = listOf(dialogPm1, dialogPm3)
        assertEquals(expectedDialogs, dialogGroupNavigation.dialogs)
        assertEquals(expectedDialogs, dialogGroupNavigation.dialogsFlow.value)
    }

    @Test
    fun testHandleBack() {
        navigator1.show(dialogPm1)
        navigator2.show(dialogPm2)
        navigator3.show(dialogPm3)
        parentPm.back()

        val expectedDialogs = listOf(dialogPm1, dialogPm2)
        assertEquals(null, navigator3.dialog)
        assertEquals(expectedDialogs, dialogGroupNavigation.dialogs)
        assertEquals(expectedDialogs, dialogGroupNavigation.dialogsFlow.value)
    }

    @Test
    fun testRestoreState() {
        val stateSaverFactory = TestStateSaverFactory(
            initialState = mutableMapOf(
                TestPm.ROOT_PM_KEY to mutableMapOf(
                    DIALOG_KEY1 to TestPm.PM1_ARGS,
                    DIALOG_KEY2 to TestPm.PM2_ARGS,
                    DIALOG_KEY3 to TestPm.PM3_ARGS,
                    DEFAULT_DIALOG_GROUP_NAVIGATION_KEY to listOf(
                        TestPm.PM1_ARGS,
                        TestPm.PM3_ARGS,
                        TestPm.PM2_ARGS
                    )
                ),
                "${TestPm.ROOT_PM_KEY}/${TestPm.PM1_ARGS.key}" to mutableMapOf(),
                "${TestPm.ROOT_PM_KEY}/${TestPm.PM2_ARGS.key}" to mutableMapOf(),
                "${TestPm.ROOT_PM_KEY}/${TestPm.PM3_ARGS.key}" to mutableMapOf()
            )
        )

        val parentPm: TestPm = TestPm.buildRootPm(stateSaverFactory)
        val navigator1: DialogNavigator<TestPm, TestPm.ResultMessage> =
            parentPm.DialogNavigator(DIALOG_KEY1)
        val navigator2: DialogNavigator<TestPm, TestPm.ResultMessage> =
            parentPm.DialogNavigator(DIALOG_KEY2)
        val navigator3: DialogNavigator<TestPm, TestPm.ResultMessage> =
            parentPm.DialogNavigator(DIALOG_KEY3)

        parentPm.lifecycle.moveTo(PmLifecycle.State.IN_FOREGROUND)

        val navigator = parentPm.DialogGroupNavigation(navigator1, navigator2, navigator3)

        val expectedDialogs = listOf<PresentationModel>(
            navigator1.dialog!!,
            navigator3.dialog!!,
            navigator2.dialog!!
        )

        assertEquals(expectedDialogs, navigator.dialogs)
        assertEquals(expectedDialogs, navigator.dialogsFlow.value)
    }

    @Test
    fun testSaveState() {
        navigator1.show(dialogPm1)
        navigator3.show(dialogPm3)
        navigator2.show(dialogPm2)

        parentPm.saveState()

        assertEquals(
            mutableMapOf(
                TestPm.ROOT_PM_KEY to mutableMapOf(
                    DIALOG_KEY1 to TestPm.PM1_ARGS,
                    DIALOG_KEY2 to TestPm.PM2_ARGS,
                    DIALOG_KEY3 to TestPm.PM3_ARGS,
                    DEFAULT_DIALOG_GROUP_NAVIGATION_KEY to listOf(
                        TestPm.PM1_ARGS,
                        TestPm.PM3_ARGS,
                        TestPm.PM2_ARGS
                    )
                ),
                "${TestPm.ROOT_PM_KEY}/${TestPm.PM1_ARGS.key}" to mutableMapOf(),
                "${TestPm.ROOT_PM_KEY}/${TestPm.PM2_ARGS.key}" to mutableMapOf(),
                "${TestPm.ROOT_PM_KEY}/${TestPm.PM3_ARGS.key}" to mutableMapOf()
            ),
            stateSaverFactory.pmStates
        )
    }
}
