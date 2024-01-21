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

package me.dmdev.premo.test

import kotlinx.coroutines.CoroutineDispatcher
import me.dmdev.premo.PmArgs
import me.dmdev.premo.PmDelegate
import me.dmdev.premo.PmFactory
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.saver.NoPmStateSaverFactory
import me.dmdev.premo.saver.PmStateSaverFactory

interface PmTestContext<PM : PresentationModel> {
    val testDispatcher: CoroutineDispatcher
    val pm: PM
    fun onCreate()
    fun onForeground()
    fun onBackground()
    fun onDestroy()
    fun onSave()
}

internal class PmTestContextImpl<PM : PresentationModel>(
    pmArgs: PmArgs,
    pmFactory: PmFactory,
    pmStateSaverFactory: PmStateSaverFactory = NoPmStateSaverFactory,
    override val testDispatcher: CoroutineDispatcher
) : PmTestContext<PM> {

    private val pmDelegate: PmDelegate<PM> = PmDelegate(
        pmArgs = pmArgs,
        pmFactory = pmFactory,
        pmStateSaverFactory = pmStateSaverFactory
    )

    override val pm: PM get() = pmDelegate.presentationModel

    override fun onCreate() {
        pmDelegate.onCreate()
    }

    override fun onForeground() {
        pmDelegate.onForeground()
    }

    override fun onBackground() {
        pmDelegate.onBackground()
    }

    override fun onDestroy() {
        pmDelegate.onDestroy()
    }

    override fun onSave() {
        pmDelegate.onSave()
    }
}
