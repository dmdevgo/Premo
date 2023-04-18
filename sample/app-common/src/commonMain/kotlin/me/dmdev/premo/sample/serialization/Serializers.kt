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

package me.dmdev.premo.sample.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlinx.serialization.protobuf.ProtoBuf
import me.dmdev.premo.PmDescription
import me.dmdev.premo.sample.CounterPm
import me.dmdev.premo.sample.MainPm
import me.dmdev.premo.sample.SamplesPm
import me.dmdev.premo.sample.bottomnavigation.BottomNavigationPm
import me.dmdev.premo.sample.bottomnavigation.TabItemPm
import me.dmdev.premo.sample.bottomnavigation.TabPm
import me.dmdev.premo.sample.dilaognavigation.DialogNavigationPm
import me.dmdev.premo.sample.dilaognavigation.SimpleDialogPm
import me.dmdev.premo.sample.stacknavigation.SimpleScreenPm
import me.dmdev.premo.sample.stacknavigation.StackNavigationPm

object Serializers {

    val module = SerializersModule {
        polymorphic(PmDescription::class) { registerPmDescriptionSubclasses() }
    }

    val json = Json {
        serializersModule = module
    }

    @OptIn(ExperimentalSerializationApi::class)
    val protoBuf = ProtoBuf {
        serializersModule = module
    }

    private fun PolymorphicModuleBuilder<PmDescription>.registerPmDescriptionSubclasses() {
        subclass(MainPm.Description::class)
        subclass(SamplesPm.Description::class)
        subclass(CounterPm.Description::class)
        subclass(StackNavigationPm.Description::class)
        subclass(SimpleScreenPm.Description::class)
        subclass(BottomNavigationPm.Description::class)
        subclass(TabPm.Description::class)
        subclass(TabItemPm.Description::class)
        subclass(DialogNavigationPm.Description::class)
        subclass(SimpleDialogPm.Description::class)
    }
}
