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

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import me.dmdev.premo.PresentationModel.Description
import me.dmdev.premo.save.PmState
import me.dmdev.premo.save.PmStateSaver

class JsonPmStateSaver : PmStateSaver {

    private val json = Json {
        serializersModule = SerializersModule {
            polymorphic(
                PmState::class,
                SerializablePmState::class,
                SerializablePmState.serializer()
            )
            polymorphic(
                Description::class,
                MainPm.Description::class,
                MainPm.Description.serializer()
            )
            polymorphic(
                Description::class,
                SamplesPm.Description::class,
                SamplesPm.Description.serializer()
            )
            polymorphic(
                Description::class,
                CounterPm.Description::class,
                CounterPm.Description.serializer()
            )
            polymorphic(
                Description::class,
                CounterUdfPm.Description::class,
                CounterUdfPm.Description.serializer()
            )
            polymorphic(
                Description::class,
                CountdownPm.Description::class,
                CountdownPm.Description.serializer()
            )
            polymorphic(
                Description::class,
                DialogPm.Description::class,
                DialogPm.Description.serializer()
            )
            polymorphic(
                Description::class,
                BottomBarPm.Description::class,
                BottomBarPm.Description.serializer()
            )
            polymorphic(
                Description::class,
                TabPm.Description::class,
                TabPm.Description.serializer()
            )
            polymorphic(
                Description::class,
                TabItemPm.Description::class,
                TabItemPm.Description.serializer()
            )
        }
    }

    override fun save(state: PmState): ByteArray {
        return json.encodeToString(state).encodeToByteArray()
    }

    override fun restore(bytes: ByteArray): PmState {
        return json.decodeFromString(bytes.decodeToString())
    }

    override fun createPmState(
        tag: String,
        description: Description,
        backstack: List<PmState>,
        children: Map<String, PmState>,
        states: Map<String, String>
    ): PmState {
        return SerializablePmState(
            tag = tag,
            description = description,
            backstack = backstack,
            children = children,
            states = states
        )
    }
}

@Serializable
class SerializablePmState(
    override val tag: String,
    @Polymorphic override val description: Description,
    override val backstack: List<PmState>,
    override val children: Map<String, PmState>,
    override val states: Map<String, String>
) : PmState