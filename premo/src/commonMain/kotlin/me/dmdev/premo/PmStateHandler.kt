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

import kotlinx.coroutines.flow.MutableStateFlow
import me.dmdev.premo.annotation.DelicatePremoApi
import me.dmdev.premo.saver.PmStateSaver
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class PmStateHandler internal constructor(
    private val hostPm: PresentationModel
) {

    private val stateSaver: PmStateSaver = hostPm.pmStateSaverFactory.createPmStateSaver(hostPm.tag)

    private class Saver<T>(
        val kType: KType,
        val saveValue: () -> T
    )

    private val savers = mutableMapOf<String, Saver<*>>()

    fun <T> getSaved(key: String, kType: KType): T? {
        return stateSaver.restoreState(key, kType)
    }

    fun <T> setSaver(key: String, kType: KType, saveValue: () -> T) {
        if (savers.contains(key)) {
            throw IllegalArgumentException("Saver with for key [$key] already exists")
        }

        savers[key] = Saver(kType, saveValue)
    }

    fun removeSaver(key: String) {
        savers.remove(key)
    }

    @DelicatePremoApi
    fun saveState() {
        savers.mapValues { entry ->
            stateSaver.saveState(entry.key, entry.value.kType, entry.value.saveValue())
        }
    }

    internal fun release() {
        savers.clear()
        hostPm.pmStateSaverFactory.deletePmStateSaver(hostPm.tag)
    }
}

inline fun <reified T> PmStateHandler.getSaved(key: String): T? {
    return getSaved(key, typeOf<T>())
}

inline fun <reified T> PmStateHandler.setSaver(key: String, noinline saveValue: () -> T) {
    setSaver(key, typeOf<T>(), saveValue)
}

@Suppress("FunctionName")
inline fun <reified T> PmStateHandler.SaveableFlow(
    key: String,
    initialValue: T
): MutableStateFlow<T> {
    return SaveableFlow(
        key = key,
        initialValueProvider = { initialValue },
        saveType = typeOf<T>(),
        saveTypeMapper = { it },
        restoreTypeMapper = { it }
    )
}

@Suppress("FunctionName")
inline fun <T, reified S> PmStateHandler.SaveableFlow(
    key: String,
    noinline initialValueProvider: () -> T,
    noinline saveTypeMapper: (T) -> S,
    noinline restoreTypeMapper: (S) -> T
): MutableStateFlow<T> {
    return SaveableFlow(
        key = key,
        initialValueProvider = initialValueProvider,
        saveType = typeOf<S>(),
        saveTypeMapper = saveTypeMapper,
        restoreTypeMapper = restoreTypeMapper
    )
}

@Suppress("FunctionName")
fun <T, S> PmStateHandler.SaveableFlow(
    key: String,
    initialValueProvider: () -> T,
    saveType: KType,
    saveTypeMapper: (T) -> S,
    restoreTypeMapper: (S) -> T
): MutableStateFlow<T> {
    val savedState = getSaved<S>(key, saveType)
    val state: MutableStateFlow<T> = if (savedState != null) {
        MutableStateFlow(restoreTypeMapper(savedState))
    } else {
        MutableStateFlow(initialValueProvider())
    }
    setSaver(key, saveType) { saveTypeMapper(state.value) }
    return state
}
