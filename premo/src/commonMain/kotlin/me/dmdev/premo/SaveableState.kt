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

class SaveableState<T, S : Saveable> internal constructor(
    pm: PresentationModel,
    initialValue: T,
    val saver: Saver<T, S>
) : State<T>(pm, initialValue) {

    @Suppress("UNCHECKED_CAST")
    var saveableValue: Saveable?
        get() = saver.save(value)
        set(value) {
            this.value = saver.restore(value as S)
        }
}

fun PresentationModel.SaveableState(
    initialValue: Byte
): State<Byte> {
    return SaveableState(
        pm = this,
        initialValue = initialValue,
        saver = ByteSaver
    ).also {
        saveableStates.add(it)
    }
}

fun PresentationModel.SaveableState(
    initialValue: Short
): State<Short> {
    return SaveableState(
        pm = this,
        initialValue = initialValue,
        saver = ShortSaver
    ).also {
        saveableStates.add(it)
    }
}

fun PresentationModel.SaveableState(
    initialValue: Int
): State<Int> {
    return SaveableState(
        pm = this,
        initialValue = initialValue,
        saver = IntSaver
    ).also {
        saveableStates.add(it)
    }
}

fun PresentationModel.SaveableState(
    initialValue: Long
): State<Long> {
    return SaveableState(
        pm = this,
        initialValue = initialValue,
        saver = LongSaver
    ).also {
        saveableStates.add(it)
    }
}

fun PresentationModel.SaveableState(
    initialValue: Float
): State<Float> {
    return SaveableState(
        pm = this,
        initialValue = initialValue,
        saver = FloatSaver
    ).also {
        saveableStates.add(it)
    }
}

fun PresentationModel.SaveableState(
    initialValue: Double
): State<Double> {
    return SaveableState(
        pm = this,
        initialValue = initialValue,
        saver = DoubleSaver
    ).also {
        saveableStates.add(it)
    }
}

fun PresentationModel.SaveableState(
    initialValue: Char
): State<Char> {
    return SaveableState(
        pm = this,
        initialValue = initialValue,
        saver = CharSaver
    ).also {
        saveableStates.add(it)
    }
}

fun PresentationModel.SaveableState(
    initialValue: Boolean
): State<Boolean> {
    return SaveableState(
        pm = this,
        initialValue = initialValue,
        saver = BooleanSaver
    ).also {
        saveableStates.add(it)
    }
}

fun PresentationModel.SaveableState(
    initialValue: String
): State<String> {
    return SaveableState(
        pm = this,
        initialValue = initialValue,
        saver = StringSaver
    ).also {
        saveableStates.add(it)
    }
}

fun <T: Saveable> PresentationModel.SaveableState(
    initialValue: T
): State<T> {
    @Suppress("UNCHECKED_CAST")
    return SaveableState(
        pm = this,
        initialValue = initialValue,
        saver = SaveableSaver
    ).also {
        saveableStates.add(it)
    } as State<T>
}

fun <T, S: Saveable> PresentationModel.SaveableState(
    initialValue: T,
    saver: Saver<T, S>
): State<T> {
    return SaveableState(
        pm = this,
        initialValue = initialValue,
        saver = saver
    ).also {
        saveableStates.add(it)
    }
}