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

import kotlinx.serialization.Serializable

interface Saveable

@Serializable
class SaveableBoolean(val value: Boolean?) : Saveable

@Serializable
class SaveableByte(val value: Byte?) : Saveable

@Serializable
class SaveableShort(val value: Short?) : Saveable

@Serializable
class SaveableInt(val value: Int?) : Saveable

@Serializable
class SaveableLong(val value: Long?) : Saveable

@Serializable
class SaveableFloat(val value: Float?) : Saveable

@Serializable
class SaveableDouble(val value: Double?) : Saveable

@Serializable
class SaveableChar(val value: Char?) : Saveable

@Serializable
class SaveableString(val value: String?) : Saveable