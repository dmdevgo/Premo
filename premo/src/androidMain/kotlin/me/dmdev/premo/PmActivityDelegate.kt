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
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*
import kotlin.reflect.KClass

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
    private val pmProvider: () -> PM
)
        where PM : PresentationModel,
              A : Activity {

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
        commonDelegate = CommonDelegate(
            pmTag = getPmTag(savedInstanceState),
            pmProvider = {
                pmProvider().also { pm ->
                    if (savedInstanceState != null) {
                        restorePmState(pm, savedInstanceState)
                    }
                }
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
        val pmState = PmState(
            presentationModel?.routers?.map { router ->
                RouterState(
                    router.pmStack.map { entry ->
                        BackStackEntryState(entry.pm::class.qualifiedName.toString(), entry.params)
                    }
                )
            }
        )
        outState.putParcelable(SAVED_PM_STATE_KEY, pmState)
    }

    private fun restorePmState(pm: PresentationModel, savedInstanceState: Bundle) {
        val pmState = savedInstanceState.getParcelable<PmState>(SAVED_PM_STATE_KEY)
        pmState?.routerStates?.forEachIndexed { index, routerState ->
            val router = pm.routers[index]
            routerState.backStackState.forEach { entry ->
                @Suppress("UNCHECKED_CAST")
                router.push(
                    Class.forName(entry.pmClassName).kotlin as KClass<out PresentationModel>,
                    entry.params
                )
            }
        }
    }

    @Parcelize
    internal data class PmState(val routerStates: List<RouterState>?) : Parcelable

    @Parcelize
    internal data class RouterState(val backStackState: List<BackStackEntryState>) : Parcelable

    @Parcelize
    internal data class BackStackEntryState(
        val pmClassName: String,
        val params: Parcelable?
    ) : Parcelable
}