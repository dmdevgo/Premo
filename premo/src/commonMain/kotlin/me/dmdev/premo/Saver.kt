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

interface Saver<T, S : Saveable> {
    fun save(value: T): S
    fun restore(saveable: S): T
}

object SaveableSaver: Saver<Saveable, Saveable> {
    override fun save(value: Saveable): Saveable {
        return value
    }

    override fun restore(saveable: Saveable): Saveable {
        return saveable
    }
}

object CharSaver: Saver<Char?, SaveableChar> {
    override fun save(value: Char?): SaveableChar {
        return SaveableChar(value)
    }

    override fun restore(saveable: SaveableChar): Char? {
        return saveable.value
    }
}

object ByteSaver: Saver<Byte?, SaveableByte> {
    override fun save(value: Byte?): SaveableByte {
        return SaveableByte(value)
    }

    override fun restore(saveable: SaveableByte): Byte? {
        return saveable.value
    }
}

object ShortSaver: Saver<Short?, SaveableShort> {
    override fun save(value: Short?): SaveableShort {
        return SaveableShort(value)
    }

    override fun restore(saveable: SaveableShort): Short? {
        return saveable.value
    }
}

object IntSaver: Saver<Int?, SaveableInt> {
    override fun save(value: Int?): SaveableInt {
        return SaveableInt(value)
    }

    override fun restore(saveable: SaveableInt): Int? {
        return saveable.value
    }
}

object LongSaver: Saver<Long?, SaveableLong> {
    override fun save(value: Long?): SaveableLong {
        return SaveableLong(value)
    }

    override fun restore(saveable: SaveableLong): Long? {
        return saveable.value
    }
}

object FloatSaver: Saver<Float?, SaveableFloat> {
    override fun save(value: Float?): SaveableFloat {
        return SaveableFloat(value)
    }

    override fun restore(saveable: SaveableFloat): Float? {
        return saveable.value
    }
}

object DoubleSaver: Saver<Double?, SaveableDouble> {
    override fun save(value: Double?): SaveableDouble {
        return SaveableDouble(value)
    }

    override fun restore(saveable: SaveableDouble): Double? {
        return saveable.value
    }
}

object BooleanSaver: Saver<Boolean?, SaveableBoolean> {
    override fun save(value: Boolean?): SaveableBoolean {
        return SaveableBoolean(value)
    }

    override fun restore(saveable: SaveableBoolean): Boolean? {
        return saveable.value
    }
}

object StringSaver: Saver<String?, SaveableString> {
    override fun save(value: String?): SaveableString {
        return SaveableString(value)
    }

    override fun restore(saveable: SaveableString): String? {
        return saveable.value
    }
}