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
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
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
    providedJson: Json,
    private val pmProvider: () -> PM,
)
        where PM : PresentationModel,
              A : Activity {

    companion object {
        private const val SAVED_PM_TAG_KEY = "premo_presentation_model_tag"
        private const val SAVED_PM_STATE_KEY = "premo_presentation_model_state"
    }

    private var json: Json
    private var commonDelegate: CommonDelegate<PM>? = null

    val presentationModel: PM? get() = commonDelegate?.presentationModel

    init {
        json = Json(providedJson) {
            serializersModule = SerializersModule {
                include(providedJson.serializersModule)
                polymorphic(Saveable::class, SaveableBoolean::class, SaveableBoolean.serializer())
                polymorphic(Saveable::class, SaveableByte::class, SaveableByte.serializer())
                polymorphic(Saveable::class, SaveableShort::class, SaveableShort.serializer())
                polymorphic(Saveable::class, SaveableInt::class, SaveableInt.serializer())
                polymorphic(Saveable::class, SaveableLong::class, SaveableLong.serializer())
                polymorphic(Saveable::class, SaveableFloat::class, SaveableFloat.serializer())
                polymorphic(Saveable::class, SaveableDouble::class, SaveableDouble.serializer())
                polymorphic(Saveable::class, SaveableChar::class, SaveableChar.serializer())
                polymorphic(Saveable::class, SaveableString::class, SaveableString.serializer())
                polymorphic(Saveable::class, SaveableAny::class, SaveableAny.serializer())
            }
        }
    }

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

    private fun savePm(pm: PresentationModel): PmState {

        val router = pm.routerOrNull
        val routerState = if (router != null) {
            RouterState(
                router.pmStack.value.map { entry ->
                    BackStackEntryState(
                        description = entry.description,
                        pmState = savePm(entry.pm)
                    )
                }
            )
        } else {
            null
        }

        return PmState(
            pmTag = pm.tag,
            router = routerState,
            children = pm.children.map { childPm -> savePm(childPm) },
            states = pm.saveableStates.map { state ->
                when (val value = state.value) {
                    is Boolean -> SaveableBoolean(value)
                    is Byte -> SaveableByte(value)
                    is Short -> SaveableShort(value)
                    is Int -> SaveableInt(value)
                    is Long -> SaveableLong(value)
                    is Float -> SaveableFloat(value)
                    is Double -> SaveableDouble(value)
                    is Char -> SaveableChar(value)
                    is String -> SaveableString(value)
                    else -> SaveableAny(value)
                }
            }
        )
    }

    private fun savePmState(outState: Bundle) {
        outState.putString(SAVED_PM_TAG_KEY, commonDelegate?.pmTag)
        val pmState = presentationModel?.let { pm ->
            savePm(pm)
        }
        outState.putString(SAVED_PM_STATE_KEY, json.encodeToString(pmState))
    }

    private fun restorePm(pm: PresentationModel, pmState: PmState) {
        pm.tag = pmState.pmTag
        pmState.states.forEachIndexed { index, saveable ->
            pm.saveableStates[index].value = when (saveable) {
                is SaveableValue -> saveable.value
                else -> null
            }
        }
        pmState.children.forEachIndexed { index, pmState ->
            restorePm(pm.children[index], pmState)
        }

        val router = pm.routerOrNull
        if (router != null && pmState.router != null) {
            pmState.router.backStackState.forEach { entry ->
                router.push(entry.description, entry.pmState.pmTag)
                restorePm(router.pmStack.value.last().pm, entry.pmState)
            }
        }
    }

    private fun restorePmState(pm: PresentationModel, savedInstanceState: Bundle) {

        val pmStateAsString = savedInstanceState.getString(SAVED_PM_STATE_KEY)
        val pmState = if (pmStateAsString != null) {
            json.decodeFromString<PmState>(pmStateAsString)
        } else {
            null
        }

        if (pmState != null) {
            restorePm(pm, pmState)
        }
    }

    private interface SaveableValue : Saveable {
        val value: Any?
    }

    @Serializable
    private data class SaveableBoolean(override val value: Boolean) : SaveableValue

    @Serializable
    private data class SaveableByte(override val value: Byte) : SaveableValue

    @Serializable
    private data class SaveableShort(override val value: Short) : SaveableValue

    @Serializable
    private data class SaveableInt(override val value: Int) : SaveableValue

    @Serializable
    private data class SaveableLong(override val value: Long) : SaveableValue

    @Serializable
    private data class SaveableFloat(override val value: Float) : SaveableValue

    @Serializable
    private data class SaveableDouble(override val value: Double) : SaveableValue

    @Serializable
    private data class SaveableChar(override val value: Char) : SaveableValue

    @Serializable
    private data class SaveableString(override val value: String) : SaveableValue

    @Serializable
    private data class SaveableAny(@Polymorphic override val value: Any?) : SaveableValue

    @Serializable
    private data class PmState(
        val pmTag: String,
        val router: RouterState?,
        val children: List<PmState>,
        val states: List<@Polymorphic Saveable?>
    )

    @Serializable
    private data class RouterState(val backStackState: List<BackStackEntryState>)

    @Serializable
    private data class BackStackEntryState(
        @Polymorphic val description: Saveable,
        val pmState: PmState
    )
}
