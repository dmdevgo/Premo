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

import kotlin.reflect.KType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import me.dmdev.premo.PmDescription
import me.dmdev.premo.PmStateSaver
import me.dmdev.premo.sample.bottom_navigation.BottomNavigationPm
import me.dmdev.premo.sample.bottom_navigation.TabItemPm
import me.dmdev.premo.sample.bottom_navigation.TabPm
import me.dmdev.premo.sample.stack_navigation.SimpleScreenPm
import me.dmdev.premo.sample.stack_navigation.StackNavigationPm

class JsonPmStateSaver(
    private val map: MutableMap<String, String>
) : PmStateSaver {

    override fun <T> saveState(key: String, kType: KType, value: T?) {
        @Suppress("UNCHECKED_CAST")
        if (value != null) {
            map[key] = json.encodeToString(serializer(kType) as KSerializer<T>, value)
        }
    }

    override fun <T> restoreState(key: String, kType: KType): T? {
        @Suppress("UNCHECKED_CAST")
        return map[key]?.let {
            json.decodeFromString(serializer(kType) as KSerializer<T>, it)
        }
    }

    companion object {
        val json = Json {
            serializersModule = SerializersModule {
                polymorphic(
                    PmDescription::class,
                    MainPm.Description::class,
                    MainPm.Description.serializer()
                )
                polymorphic(
                    PmDescription::class,
                    MainPm.Description::class,
                    MainPm.Description.serializer()
                )
                polymorphic(
                    PmDescription::class,
                    SamplesPm.Description::class,
                    SamplesPm.Description.serializer()
                )
                polymorphic(
                    PmDescription::class,
                    CounterPm.Description::class,
                    CounterPm.Description.serializer()
                )
                polymorphic(
                    PmDescription::class,
                    StackNavigationPm.Description::class,
                    StackNavigationPm.Description.serializer()
                )
                polymorphic(
                    PmDescription::class,
                    SimpleScreenPm.Description::class,
                    SimpleScreenPm.Description.serializer()
                )
                polymorphic(
                    PmDescription::class,
                    BottomNavigationPm.Description::class,
                    BottomNavigationPm.Description.serializer()
                )
                polymorphic(
                    PmDescription::class,
                    TabPm.Description::class,
                    TabPm.Description.serializer()
                )
                polymorphic(
                    PmDescription::class,
                    TabItemPm.Description::class,
                    TabItemPm.Description.serializer()
                )
            }
        }
    }
}