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

package me.dmdev.premo

import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import org.w3c.dom.Document
import org.w3c.dom.get
import org.w3c.dom.set

class JsPmDelegate<PM : PresentationModel>(
    pmDescription: PmDescription,
    pmFactory: PmFactory,
    private val pmStateSaver: StringStateSaver
) {

    private val pmDelegate: PmDelegate<PM> by lazy {
        PmDelegate<PM>(
            pmParams = PmParams(
                description = pmDescription,
                parent = null,
                factory = pmFactory,
                stateSaverFactory = pmStateSaver
            )
        )
    }

    val presentationModel: PM get() = pmDelegate.presentationModel

    init {
        onCreate()

        window.addEventListener(type = "beforeunload", callback = {
            onSaveState()
            pmDelegate.onDestroy()
        })

        if (document.isVisible) {
            pmDelegate.onForeground()
        }

        document.addEventListener(type = "visibilitychange", callback = {
            if (document.isVisible) {
                pmDelegate.onForeground()
            } else {
                pmDelegate.onBackground()
            }
        })
    }

    private val Document.isVisible: Boolean
        get() {
            return window.document["visibilityState"] as? String == "visible"
        }

    private fun onCreate() {
        pmStateSaver.restore(localStorage[PM_STATE_KEY])
        pmDelegate.onCreate()
    }

    private fun onSaveState() {
        pmDelegate.savePm()
        localStorage[PM_STATE_KEY] = pmStateSaver.save()
    }

    companion object {
        private const val PM_STATE_KEY = "pm_state"
    }
}
