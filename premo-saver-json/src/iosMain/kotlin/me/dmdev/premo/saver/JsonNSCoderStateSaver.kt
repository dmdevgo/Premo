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

import kotlinx.cinterop.CPointed
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import kotlinx.serialization.json.Json
import me.dmdev.premo.NSCoderStateSaver
import me.dmdev.premo.PmStateSaver
import platform.Foundation.NSCoder
import platform.Foundation.decodeBytesForKey
import platform.Foundation.encodeBytes
import platform.darwin.NSUIntegerVar
import platform.posix.memcpy

class JsonNSCoderStateSaver(json: Json) : NSCoderStateSaver {

    private val jsonStateSaverFactory = JsonStateSaverFactory(json)

    override fun save(coder: NSCoder) {
        jsonStateSaverFactory.save().encodeToByteArray().usePinned {
            coder.encodeBytes(
                it.addressOf(0).reinterpret(),
                it.get().size.toULong(),
                PM_STATE_KEY
            )
        }
    }

    override fun restore(coder: NSCoder?) {
        if (coder == null) return
        memScoped {
            val length: NSUIntegerVar = alloc()
            val decodedBytesPtr = coder.decodeBytesForKey(PM_STATE_KEY, length.ptr) ?: return

            val bytes = ByteArray(length.value.toInt())
            bytes.usePinned {
                memcpy(
                    it.addressOf(0),
                    decodedBytesPtr.reinterpret<CPointed>(),
                    length.value.toULong()
                )
            }
            jsonStateSaverFactory.restore(bytes.toKString())
        }
    }

    override fun createPmStateSaver(key: String): PmStateSaver {
        return jsonStateSaverFactory.createPmStateSaver(key)
    }

    companion object {
        private const val PM_STATE_KEY = "pm_state"
    }
}
