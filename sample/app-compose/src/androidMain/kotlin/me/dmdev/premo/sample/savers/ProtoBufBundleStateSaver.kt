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
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.serializer
import me.dmdev.premo.BundleStateSaver
import me.dmdev.premo.PmStateSaver
import me.dmdev.premo.sample.serialization.ProtoBufPmStateSaver
import me.dmdev.premo.sample.serialization.Serializers.protoBuf

class ProtoBufBundleStateSaver : BundleStateSaver {

    private var pmStates = mutableMapOf<String, MutableMap<String, ByteArray>>()

    @OptIn(ExperimentalSerializationApi::class)
    override fun save(outState: Bundle) {
        outState.putByteArray(PM_STATE_KEY, protoBuf.encodeToByteArray(serializer(), pmStates))
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun restore(savedState: Bundle?) {
        savedState?.getByteArray(PM_STATE_KEY)?.let { byteArray ->
            pmStates = protoBuf.decodeFromByteArray(serializer(), byteArray)
        }
    }

    override fun createPmStateSaver(key: String): PmStateSaver {
        val map = pmStates[key] ?: mutableMapOf<String, ByteArray>().also { pmStates[key] = it }
        return ProtoBufPmStateSaver(map)
    }

    override fun deletePmStateSaver(key: String) {
        pmStates.remove(key)
    }

    companion object {
        private const val PM_STATE_KEY = "pm_state"
    }
}
