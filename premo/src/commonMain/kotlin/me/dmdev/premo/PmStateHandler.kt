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

import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class PmStateHandler(
    private val stateSaver: PmStateSaver,
    private val states: Map<String, String>
) {

    private class Saver<T>(
        val kType: KType,
        val saveValue: () -> T
    )

    private val savers = mutableMapOf<String, Saver<*>>()

    fun <T> getSaved(key: String, kType: KType): T? {
        return states[key]?.let { stateSaver.restoreState(kType, it) }
    }

    fun <T> setSaver(key: String, kType: KType, saveValue: () -> T) {
        savers[key] = Saver(kType, saveValue)
    }

    fun removeSaver(key: String) {
        savers.remove(key)
    }

    fun saveState(): Map<String, String> {
        return savers.mapValues { entry ->
            stateSaver.saveState(entry.value.kType, entry.value.saveValue())
        }
    }
}

inline fun <reified T> PmStateHandler.getSaved(key: String): T? {
    return getSaved(key, typeOf<T>())
}

inline fun <reified T> PmStateHandler.setSaver(key: String, noinline saveValue: () -> T) {
    setSaver(key, typeOf<T>(), saveValue)
}

@Suppress("FunctionName")
inline fun <reified T> PresentationModel.SaveableFlow(
    key: String,
    initialValue: T
): MutableStateFlow<T> {
    return stateHandler.SaveableFlow(key, initialValue, typeOf<T>())
}

@Suppress("FunctionName")
inline fun <reified T> PmStateHandler.SaveableFlow(
    key: String,
    initialValue: T
): MutableStateFlow<T> {
    return SaveableFlow(key, initialValue, typeOf<T>())
}

@Suppress("FunctionName")
fun <T> PmStateHandler.SaveableFlow(
    key: String,
    initialValue: T,
    kType: KType
): MutableStateFlow<T> {
    val savedState = getSaved<T>(key, kType)
    val state: MutableStateFlow<T> = if (savedState != null) {
        MutableStateFlow(savedState)
    } else {
        MutableStateFlow(initialValue)
    }
    setSaver(key, kType) { state.value }
    return state
}