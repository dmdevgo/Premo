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
import me.dmdev.premo.navigation.PmFactory
import me.dmdev.premo.save.PmState
import me.dmdev.premo.save.PmStateSaver
import me.dmdev.premo.save.StateSaver
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
class PmActivityDelegate<PM : PresentationModel>(
    private val pmActivity: Activity,
    private val pmStateSaver: PmStateSaver,
    private val stateSaver: StateSaver,
    private val pmFactory: PmFactory,
    private val pmDescription: PresentationModel.Description,
) {

    companion object {
        private const val SAVED_PM_TAG_KEY = "premo_presentation_model_tag"
        private const val SAVED_PM_STATE_KEY = "premo_presentation_model_state"
    }

    private var commonDelegate: CommonDelegate<PM>? = null

    val presentationModel: PM? get() = commonDelegate?.presentationModel

    /**
     * You must call this method from the containing [Activity]'s corresponding method.
     */
    fun onCreate(savedInstanceState: Bundle?) {

        val config = PmConfig(
            tag = getPmTag(savedInstanceState),
            parent = null,
            state = restorePmState(savedInstanceState),
            factory = pmFactory,
            description = pmDescription,
            stateSaver = stateSaver
        )

        commonDelegate = CommonDelegate(
            pmTag = config.tag,
            pmProvider = {
                @Suppress("UNCHECKED_CAST")
                pmFactory.createPm(config) as PM
            }
        )
        commonDelegate?.onCreate()
    }

    /**
     * You must call this method from the containing [Activity]'s corresponding method.
     */
    fun onStart() {
        commonDelegate?.onForeground()
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
        commonDelegate?.onBackground()
    }

    /**
     * You must call this method from the containing [Activity]'s corresponding method.
     */
    fun onDestroy() {
        if (pmActivity.isFinishing) {
            commonDelegate?.onDestroy()
        }
    }

    /**
     * You must call this method from the containing [Activity]'s corresponding method.
     */
    fun handleBack(): Boolean {
        return presentationModel?.handleBack() ?: false
    }

    private fun getPmTag(savedInstanceState: Bundle?): String {
        return savedInstanceState?.getString(SAVED_PM_TAG_KEY) ?: UUID.randomUUID().toString()
    }

    private fun savePmState(outState: Bundle) {
        outState.putString(SAVED_PM_TAG_KEY, commonDelegate?.pmTag)
        presentationModel?.let { pm ->
            val pmState = pm.saveState(pmStateSaver)
            outState.putByteArray(SAVED_PM_STATE_KEY, pmStateSaver.save(pmState))
        }
    }

    private fun restorePmState(savedInstanceState: Bundle?): PmState? {
        val pmStateAsByteArray = savedInstanceState?.getByteArray(SAVED_PM_STATE_KEY)
        return if (pmStateAsByteArray != null) {
            pmStateSaver.restore(pmStateAsByteArray)
        } else {
            null
        }
    }
}