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

import android.app.Activity
import android.os.Bundle
import java.util.*

class PmActivityDelegate<PM : PresentationModel>(
    private val pmActivity: Activity,
    private val pmDescription: PmDescription,
    private val pmFactory: PmFactory,
    private val stateSaver: BundleStateSaver
) {

    private var pmDelegate: PmDelegate<PM>? = null

    val presentationModel: PM
        get() = pmDelegate?.presentationModel
            ?: throw IllegalStateException("Presentation Model has not been initialized yet, call this method after onCreate.")

    /**
     * You must call this method from the containing [Activity]'s corresponding method.
     */
    fun onCreate(savedInstanceState: Bundle?) {
        stateSaver.restore(savedInstanceState)
        val pmTag = getPmTag(savedInstanceState) ?: UUID.randomUUID().toString()
        val pmParams = PmParams(
            tag = pmTag,
            parent = null,
            description = pmDescription,
            factory = pmFactory,
            stateSaverFactory = stateSaver
        )

        pmDelegate = PmDelegate(pmParams)
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

    private fun getPmTag(savedInstanceState: Bundle?): String? {
        return savedInstanceState?.getString(SAVED_PM_TAG_KEY)
    }

    private fun savePmState(outState: Bundle) {
        outState.putString(SAVED_PM_TAG_KEY, pmDelegate?.presentationModel?.tag)
        pmDelegate?.savePm()
        stateSaver.save(outState)
    }

    companion object {
        private const val SAVED_PM_TAG_KEY = "premo_presentation_model_tag"
    }
}
