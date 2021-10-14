/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020-2021 Dmitriy Gorbunov (dmitriy.goto@gmail.com)
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

import android.app.Activity
import android.os.Bundle
import me.dmdev.premo.state.StateSaver
import me.dmdev.premo.state.restoreState
import me.dmdev.premo.state.saveState
import java.util.*


class PmActivityDelegate<PM : PresentationModel>(
    private val pmActivity: Activity,
    private val stateSaver: StateSaver,
    private val pmFactory: PmFactory,
    private val pmDescription: PmDescription,
) {

    companion object {
        private const val SAVED_PM_TAG_KEY = "premo_presentation_model_tag"
        private const val SAVED_PM_STATE_KEY = "premo_presentation_model_state"
    }

    private var pmDelegate: PmDelegate<PM>? = null

    val presentationModel: PM
        get() = pmDelegate?.presentationModel
            ?: throw IllegalStateException("Presentation Model has not been initialized yet, call this method after onCreate.")

    /**
     * You must call this method from the containing [Activity]'s corresponding method.
     */
    fun onCreate(savedInstanceState: Bundle?) {

        val pmParams = PmParams(
            tag = getPmTag(savedInstanceState),
            parent = null,
            state = restorePmState(savedInstanceState),
            factory = pmFactory,
            description = pmDescription,
            stateSaver = stateSaver
        )

        pmDelegate = PmDelegate(
            pmParams = pmParams,
            exitHandler = {
                pmActivity.finish()
                true
            }
        )
        pmDelegate?.onCreate()
    }

    /**
     * You must call this method from the containing [Activity]'s corresponding method.
     */
    fun onStart() {
        pmDelegate?.onForeground()
    }

    /**
     * You must call this method from the containing [Activity]'s corresponding method.
     */
    fun onResume() {
        // For symmetry, may be used in the future
    }

    /**
     * You must call this method from the containing [Activity]'s corresponding method.
     */
    fun onSaveInstanceState(outState: Bundle) {
        savePmState(outState)
    }

    /**
     * You must call this method from the containing [Activity]'s corresponding method.
     */
    fun onPause() {
        // For symmetry, may be used in the future
    }

    /**
     * You must call this method from the containing [Activity]'s corresponding method.
     */
    fun onStop() {
        pmDelegate?.onBackground()
    }

    /**
     * You must call this method from the containing [Activity]'s corresponding method.
     */
    fun onDestroy() {
        if (pmActivity.isFinishing) {
            pmDelegate?.onDestroy()
        }
        pmDelegate = null
    }

    /**
     * You must call this method from the containing [Activity]'s corresponding method.
     */
    fun onBackPressed() {
        pmDelegate?.onSystemBack()
    }

    private fun getPmTag(savedInstanceState: Bundle?): String {
        return savedInstanceState?.getString(SAVED_PM_TAG_KEY) ?: UUID.randomUUID().toString()
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun savePmState(outState: Bundle) {
        outState.putString(SAVED_PM_TAG_KEY, pmDelegate?.presentationModel?.tag)
        val pmState = pmDelegate?.savePm()
        if (pmState != null) {
            outState.putString(SAVED_PM_STATE_KEY, stateSaver.saveState(pmState))
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun restorePmState(savedInstanceState: Bundle?): Map<String, String> {
        val pmStateString = savedInstanceState?.getString(SAVED_PM_STATE_KEY)
        return if (pmStateString != null) {
            stateSaver.restoreState(pmStateString)
        } else {
            mapOf()
        }
    }
}