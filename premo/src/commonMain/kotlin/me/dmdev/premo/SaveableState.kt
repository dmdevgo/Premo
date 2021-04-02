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

@file:Suppress("FunctionName")

package me.dmdev.premo

class SaveableState<T, S : Saveable>(
    initialValue: T,
    val saver: Saver<T, S>
) : State<T>(initialValue) {

    @Suppress("UNCHECKED_CAST")
    var saveableValue: Saveable?
        get() = saver.save(value)
        set(value) {
            this.value = saver.restore(value as S)
        }
}

fun PresentationModel.SaveableState(
    initialValue: Byte,
    key: String
): State<Byte> {
    return SaveableState(
        initialValue = initialValue,
        saver = ByteSaver
    ).also {
        saveableStates[key] = it
    }
}

fun PresentationModel.SaveableState(
    initialValue: Short,
    key: String
): State<Short> {
    return SaveableState(
        initialValue = initialValue,
        saver = ShortSaver
    ).also {
        saveableStates[key] = it
    }
}

fun PresentationModel.SaveableState(
    initialValue: Int,
    key: String
): State<Int> {
    return SaveableState(
        initialValue = initialValue,
        saver = IntSaver
    ).also {
        saveableStates[key] = it
    }
}

fun PresentationModel.SaveableState(
    initialValue: Long,
    key: String
): State<Long> {
    return SaveableState(
        initialValue = initialValue,
        saver = LongSaver
    ).also {
        saveableStates[key] = it
    }
}

fun PresentationModel.SaveableState(
    initialValue: Float,
    key: String
): State<Float> {
    return SaveableState(
        initialValue = initialValue,
        saver = FloatSaver
    ).also {
        saveableStates[key] = it
    }
}

fun PresentationModel.SaveableState(
    initialValue: Double,
    key: String
): State<Double> {
    return SaveableState(
        initialValue = initialValue,
        saver = DoubleSaver
    ).also {
        saveableStates[key] = it
    }
}

fun PresentationModel.SaveableState(
    initialValue: Char,
    key: String
): State<Char> {
    return SaveableState(
        initialValue = initialValue,
        saver = CharSaver
    ).also {
        saveableStates[key] = it
    }
}

fun PresentationModel.SaveableState(
    initialValue: Boolean,
    key: String
): State<Boolean> {
    return SaveableState(
        initialValue = initialValue,
        saver = BooleanSaver
    ).also {
        saveableStates[key] = it
    }
}

fun PresentationModel.SaveableState(
    initialValue: String,
    key: String
): State<String> {
    return SaveableState(
        initialValue = initialValue,
        saver = StringSaver
    ).also {
        saveableStates[key] = it
    }
}

fun <T: Saveable> PresentationModel.SaveableState(
    initialValue: T,
    key: String
): State<T> {
    @Suppress("UNCHECKED_CAST")
    return SaveableState(
        initialValue = initialValue,
        saver = SaveableSaver
    ).also {
        saveableStates[key] = it
    } as State<T>
}