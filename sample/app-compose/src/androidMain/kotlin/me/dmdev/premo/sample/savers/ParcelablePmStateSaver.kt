/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020-2023 Dmitriy Gorbunov (dmitriy.goto@gmail.com)
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

package me.dmdev.premo.sample.savers

import android.os.Bundle
import com.chrynan.parcelable.core.Parcelable
import com.chrynan.parcelable.core.getParcelable
import com.chrynan.parcelable.core.putParcelable
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import me.dmdev.premo.PmStateSaver
import me.dmdev.premo.sample.serialization.Serializers
import kotlin.reflect.KType

class ParcelablePmStateSaver(
    private val bundle: Bundle
) : PmStateSaver {

    @OptIn(ExperimentalSerializationApi::class)
    override fun <T> saveState(key: String, kType: KType, value: T?) {
        @Suppress("UNCHECKED_CAST")
        if (value != null) {
            bundle.putParcelable(
                key = key,
                value = value,
                serializer = serializer(kType) as KSerializer<T>,
                parcelable = parcelable
            )
        }
    }

    override fun <T> restoreState(key: String, kType: KType): T? {
        return restore(key, kType)
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun <T : Any> restore(key: String, kType: KType): T? {
        @Suppress("UNCHECKED_CAST")
        return bundle.getParcelable(
            key = key,
            deserializer = serializer(kType) as KSerializer<T>,
            parcelable = parcelable
        )
    }

    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        val parcelable = Parcelable {
            serializersModule = Serializers.module
        }
    }
}
