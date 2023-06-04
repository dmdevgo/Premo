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

package me.dmdev.premo.saver

import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import me.dmdev.premo.PmDescription
import kotlin.reflect.KType

@Suppress("UNCHECKED_CAST")
class JsonPmStateSaver(
    private val json: Json,
    private val map: MutableMap<String, String>
) : PmStateSaver {

    override fun <T> saveState(key: String, kType: KType, value: T?) {
        if (value != null) {
            try {
                val serializer = json.serializersModule.serializer(kType) as KSerializer<T>
                map[key] = json.encodeToString(serializer, value)
            } catch (e: SerializationException) {
                // Workaround for JS and Native https://github.com/Kotlin/kotlinx.serialization/issues/1077
                try {
                    val serializer = polymorphicPmDescriptionSerializer as KSerializer<T>
                    map[key] = json.encodeToString(serializer, value)
                    return
                } catch (_: Throwable) {}

                try {
                    val serializer = polymorphicListSerializer as KSerializer<T>
                    map[key] = json.encodeToString(serializer, value)
                    return
                } catch (_: Throwable) {}

                throw e
            }
        } else {
            map.remove(key)
        }
    }

    override fun <T> restoreState(key: String, kType: KType): T? {
        return map[key]?.let { jsonString ->
            try {
                val serializer = json.serializersModule.serializer(kType) as KSerializer<T>
                return json.decodeFromString(serializer, jsonString)
            } catch (e: SerializationException) {
                // Workaround for JS and Native https://github.com/Kotlin/kotlinx.serialization/issues/1077
                try {
                    val serializer = polymorphicPmDescriptionSerializer as KSerializer<T>
                    return json.decodeFromString(serializer, jsonString)
                } catch (_: Throwable) {}

                try {
                    val serializer = polymorphicListSerializer as KSerializer<T>
                    return json.decodeFromString(serializer, jsonString)
                } catch (_: Throwable) {}

                throw e
            }
        }
    }

    private val polymorphicPmDescriptionSerializer = PolymorphicSerializer(PmDescription::class)

    private val polymorphicListSerializer = ListSerializer(
        PolymorphicSerializer(PmDescription::class)
    )
}
