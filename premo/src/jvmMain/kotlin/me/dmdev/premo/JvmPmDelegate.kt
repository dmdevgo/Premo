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

import me.dmdev.premo.saver.FileStateSaver
import java.io.File

class JvmPmDelegate<PM : PresentationModel>(
    pmArgs: PmArgs,
    pmFactory: PmFactory,
    private val pmStateSaver: FileStateSaver,
    private val pmStateFile: File = File("pm_state.txt"),
    private val onSaveOrRestoreStateError: (e: Throwable) -> Unit = { throw it }
) {

    private val pmDelegate: PmDelegate<PM> by lazy {
        PmDelegate(
            pmArgs = pmArgs,
            pmFactory = pmFactory,
            pmStateSaverFactory = pmStateSaver
        )
    }

    val presentationModel: PM get() = pmDelegate.presentationModel

    fun onCreate() {
        if (pmStateFile.exists()) {
            try {
                pmStateSaver.restore(pmStateFile)
            } catch (e: Throwable) {
                onSaveOrRestoreStateError(e)
            }
            pmStateFile.delete()
        }
        pmDelegate.onCreate()
    }

    fun onForeground() {
        pmDelegate.onForeground()
    }

    fun onBackground() {
        pmDelegate.onBackground()
    }

    fun onDestroy() {
        pmDelegate.onDestroy()
    }

    fun onSaveState() {
        try {
            pmDelegate.onSave()
            pmStateSaver.save(pmStateFile)
        } catch (e: Throwable) {
            onSaveOrRestoreStateError(e)
        }
    }
}
