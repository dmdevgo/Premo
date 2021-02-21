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

import android.os.Bundle
import androidx.fragment.app.Fragment
import me.dmdev.premo.CommonDelegate
import me.dmdev.premo.PmView
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.delegate.PmFragmentDelegate.RetainMode.CONFIGURATION_CHANGES
import me.dmdev.premo.delegate.PmFragmentDelegate.RetainMode.SAVED_STATE
import me.dmdev.premo.view.PmFragment
import java.util.*

/**
 * Delegate for the [Fragment] that helps with creation and binding of
 * a [presentation model][PresentationModel] and a [view][PmView].
 *
 * Use this class only if you can't subclass the [PmFragment].
 *
 * Users of this class must forward all the lifecycle methods from the containing Fragment
 * to the corresponding ones in this class.
 */
class PmFragmentDelegate<PM, F>(
    private val pmFragment: F,
    private val retainMode: RetainMode
)
        where PM : PresentationModel,
              F : Fragment, F : PmView<PM> {

    companion object {
        private const val SAVED_PM_TAG_KEY = "premo_presentation_model_tag"
    }

    private lateinit var pmTag: String
    private val commonDelegate = CommonDelegate<PM, F>(pmFragment)

    val presentationModel: PM get() = commonDelegate.presentationModel

    /**
     * You must call this method from the containing [Fragment]'s corresponding method.
     */
    fun onCreate(savedInstanceState: Bundle?) {
        pmTag = savedInstanceState?.getString(SAVED_PM_TAG_KEY) ?: UUID.randomUUID().toString()
        commonDelegate.onCreate(pmTag)
    }

    /**
     * You must call this method from the containing [Fragment]'s corresponding method.
     */
    fun onViewCreated(savedInstanceState: Bundle?) {
        // For symmetry, may be used in the future
    }

    /**
     * You must call this method from the containing [Fragment]'s corresponding method.
     */
    fun onStart() {
        commonDelegate.onForeground()
    }

    /**
     * You must call this method from the containing [Fragment]'s corresponding method.
     */
    fun onResume() {
        // For symmetry, may be used in the future
    }

    /**
     * You must call this method from the containing [Fragment]'s corresponding method.
     */
    fun onSaveInstanceState(outState: Bundle) {
        outState.putString(SAVED_PM_TAG_KEY, pmTag)
    }

    /**
     * You must call this method from the containing [Fragment]'s corresponding method.
     */
    fun onPause() {
        // For symmetry, may be used in the future
    }

    /**
     * You must call this method from the containing [Fragment]'s corresponding method.
     */
    fun onStop() {
        commonDelegate.onBackground()
    }

    /**
     * You must call this method from the containing [Fragment]'s corresponding method.
     */
    fun onDestroyView() {
        // For symmetry, may be used in the future
    }

    /**
     * You must call this method from the containing [Fragment]'s corresponding method.
     */
    fun onDestroy() {
        when (retainMode) {
            RetainMode.SAVED_STATE -> {
                if (pmFragment.activity?.isFinishing == true
                    || (pmFragment.fragmentManager?.isStateSaved?.not() == true)
                ) {
                    commonDelegate.onDestroy()
                }
            }

            RetainMode.CONFIGURATION_CHANGES -> {
                if (pmFragment.activity?.isChangingConfigurations?.not() == true) {
                    commonDelegate.onDestroy()
                }
            }
        }
    }

    /**
     * Strategies for retaining the PresentationModel[PresentationModel].
     * [SAVED_STATE] - the PresentationModel will be destroyed if the Activity is finishing or the Fragment state has not been saved.
     * [CONFIGURATION_CHANGES] - Retain the PresentationModel during a configuration change.
     */
    enum class RetainMode { SAVED_STATE, CONFIGURATION_CHANGES }
}
