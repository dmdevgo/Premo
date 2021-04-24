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

package me.dmdev.premo.sample

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import me.dmdev.premo.*

class JsonPmStateSaver: PmStateSaver {

    private val json = Json {
        serializersModule = SerializersModule {
            polymorphic(Saveable::class, SaveableBoolean::class, SaveableBoolean.serializer())
            polymorphic(Saveable::class, SaveableByte::class, SaveableByte.serializer())
            polymorphic(Saveable::class, SaveableShort::class, SaveableShort.serializer())
            polymorphic(Saveable::class, SaveableInt::class, SaveableInt.serializer())
            polymorphic(Saveable::class, SaveableLong::class, SaveableLong.serializer())
            polymorphic(Saveable::class, SaveableFloat::class, SaveableFloat.serializer())
            polymorphic(Saveable::class, SaveableDouble::class, SaveableDouble.serializer())
            polymorphic(Saveable::class, SaveableChar::class, SaveableChar.serializer())
            polymorphic(Saveable::class, SaveableString::class, SaveableString.serializer())

            polymorphic(Saveable::class, CounterUdfPm.CounterState::class, CounterUdfPm.CounterState.serializer())

            polymorphic(PresentationModel.Args::class, MainPm.Args::class, MainPm.Args.serializer())
            polymorphic(PresentationModel.Args::class, SamplesPm.Args::class, SamplesPm.Args.serializer())
            polymorphic(PresentationModel.Args::class, CounterPm.Args::class, CounterPm.Args.serializer())
            polymorphic(PresentationModel.Args::class, CounterUdfPm.Args::class, CounterUdfPm.Args.serializer())
            polymorphic(PresentationModel.Args::class, CountdownPm.Args::class, CountdownPm.Args.serializer())
            polymorphic(PresentationModel.Args::class, DialogPm.Args::class, DialogPm.Args.serializer())
            polymorphic(PresentationModel.Args::class, BottomBarPm.Args::class, BottomBarPm.Args.serializer())
            polymorphic(PresentationModel.Args::class, TabPm.Args::class, TabPm.Args.serializer())
            polymorphic(PresentationModel.Args::class, TabItemPm.Args::class, TabItemPm.Args.serializer())
        }
    }

    override fun save(state: PmState): ByteArray {
        return json.encodeToString(state).encodeToByteArray()
    }

    override fun restore(bytes: ByteArray): PmState {
        return json.decodeFromString(bytes.decodeToString())
    }
}