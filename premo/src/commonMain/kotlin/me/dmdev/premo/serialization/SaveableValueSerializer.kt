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

package me.dmdev.premo.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.nullable
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.*
import kotlinx.serialization.serializer

object SaveableValueSerializer : KSerializer<SaveableValue> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("SaveableValue") {
        element("type", serialDescriptor<String>())
        element("data", buildClassSerialDescriptor("Any").nullable)
    }

    @Suppress("UNCHECKED_CAST")
    private val dataTypeSerializers: Map<String, KSerializer<Any>> =
        mapOf(
            Int::class.qualifiedName!! to serializer<Int>(),
            // TODO register all base types and allow setup for custom user types
        ).mapValues { (_, v) -> v as KSerializer<Any> }

    private fun getPayloadSerializer(dataType: String): KSerializer<Any> = dataTypeSerializers[dataType]
        ?: throw SerializationException("Serializer for class $dataType is not registered in SaveableValueSerializer")

    @ExperimentalSerializationApi
    override fun serialize(encoder: Encoder, value: SaveableValue) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.type)
            encodeNullableSerializableElement(descriptor, 1, getPayloadSerializer(value.type), value.data)
        }
    }

    @ExperimentalSerializationApi
    override fun deserialize(decoder: Decoder): SaveableValue = decoder.decodeStructure(descriptor) {
        if (decodeSequentially()) {
            val type = decodeStringElement(descriptor, 0)
            val data = decodeSerializableElement(descriptor, 1, getPayloadSerializer(type))
            SaveableValue(type, data)
        } else {
            require(decodeElementIndex(descriptor) == 0) { "type field should precede data field" }
            val type = decodeStringElement(descriptor, 0)
            val data = when (val index = decodeElementIndex(descriptor)) {
                1 -> decodeSerializableElement(descriptor, 1, getPayloadSerializer(type))
                CompositeDecoder.DECODE_DONE -> throw SerializationException("data field is missing")
                else -> error("Unexpected index: $index")
            }
            SaveableValue(type, data)
        }
    }
}