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

package me.dmdev.premo.delegate

import android.app.Activity
import android.os.Bundle
import me.dmdev.premo.CommonDelegate
import me.dmdev.premo.PmView
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.delegate.PmActivityDelegate.RetainMode.CONFIGURATION_CHANGES
import me.dmdev.premo.delegate.PmActivityDelegate.RetainMode.IS_FINISHING
import me.dmdev.premo.view.PmActivity
import java.util.*

/**
 * Delegate for the [Activity] that helps with creation and binding of
 * a [presentation model][PresentationModel] and a [view][PmView].
 *
 * Use this class only if you can't subclass the [PmActivity].
 *
 * Users of this class must forward all the lifecycle methods from the containing Activity
 * to the corresponding ones in this class.
 */
class PmActivityDelegate<PM, A>(
    private val pmActivity: A,
    private val retainMode: RetainMode
)
        where PM : PresentationModel,
              A : Activity, A : PmView<PM> {

    companion object {
        private const val SAVED_PM_TAG_KEY = "premo_presentation_model_tag"
    }

    private lateinit var pmTag: String
    private val commonDelegate = CommonDelegate<PM, A>(pmActivity)

    val presentationModel: PM get() = commonDelegate.presentationModel

    /**
     * You must call this method from the containing [Activity]'s corresponding method.
     */
    fun onCreate(savedInstanceState: Bundle?) {
        pmTag = savedInstanceState?.getString(SAVED_PM_TAG_KEY) ?: UUID.randomUUID().toString()
        commonDelegate.onCreate(pmTag)
    }

    /**
     * You must call this method from the containing [Activity]'s corresponding method.
     */
    fun onPostCreate() {
        commonDelegate.onBind()
    }

    /**
     * You must call this method from the containing [Activity]'s corresponding method.
     */
    fun onStart() {
        // For symmetry, may be used in the future
    }

    /**
     * You must call this method from the containing [Activity]'s corresponding method.
     */
    fun onResume() {
        commonDelegate.onResume()
    }

    /**
     * You must call this method from the containing [Activity]'s corresponding method.
     */
    fun onSaveInstanceState(outState: Bundle) {
        outState.putString(SAVED_PM_TAG_KEY, pmTag)
        commonDelegate.onPause()
    }

    /**
     * You must call this method from the containing [Activity]'s corresponding method.
     */
    fun onPause() {
        commonDelegate.onPause()
    }

    /**
     * You must call this method from the containing [Activity]'s corresponding method.
     */
    fun onStop() {
        // For symmetry, may be used in the future
    }

    /**
     * You must call this method from the containing [Activity]'s corresponding method.
     */
    fun onDestroy() {
        commonDelegate.onUnbind()

        when (retainMode) {
            RetainMode.IS_FINISHING -> {
                if (pmActivity.isFinishing) {
                    commonDelegate.onDestroy()
                }
            }

            RetainMode.CONFIGURATION_CHANGES -> {
                if (!pmActivity.isChangingConfigurations) {
                    commonDelegate.onDestroy()
                }
            }
        }
    }

    /**
     * Strategies for retaining the PresentationModel[PresentationModel].
     * [IS_FINISHING] - the PresentationModel will be destroyed if the Activity is finishing.
     * [CONFIGURATION_CHANGES] - Retain the PresentationModel during a configuration change.
     */
    enum class RetainMode { IS_FINISHING, CONFIGURATION_CHANGES }
}