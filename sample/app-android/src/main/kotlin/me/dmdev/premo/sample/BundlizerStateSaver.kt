/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020-2022 Dmitriy Gorbunov (dmitriy.goto@gmail.com)
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

package me.dmdev.premo.sample

import android.os.Bundle
import dev.ahmedmourad.bundlizer.Bundlizer
import kotlin.reflect.KType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import me.dmdev.premo.PmStateSaver
import me.dmdev.premo.sample.serialization.Serializers

@Suppress("SpellCheckingInspection")
class BundlizerStateSaver(private val bundle: Bundle) : PmStateSaver {

    override fun <T> saveState(key: String, kType: KType, value: T?) {
        if (value != null) {
            @Suppress("UNCHECKED_CAST")
            bundle.putBundle(
                key,
                Bundlizer.bundle(serializer(kType) as KSerializer<T>, value, Serializers.module)
            )
        }
    }

    override fun <T> restoreState(key: String, kType: KType): T? {
        @Suppress("UNCHECKED_CAST")
        return bundle.getBundle(key)?.let { bundle ->
            Bundlizer.unbundle(serializer(kType) as KSerializer<T>, bundle, Serializers.module)
        }
    }
}