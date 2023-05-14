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
import me.dmdev.premo.BundleStateSaver
import me.dmdev.premo.PmStateSaver

class BundlizerBundleStateSaver : BundleStateSaver {

    private var bundles = Bundle()

    override fun save(outState: Bundle) {
        outState.putBundle(PM_STATE_KEY, bundles)
    }

    override fun restore(savedState: Bundle?) {
        bundles = savedState?.getBundle(PM_STATE_KEY) ?: Bundle()
    }

    override fun createPmStateSaver(key: String): PmStateSaver {
        val bundle = bundles.getBundle(key) ?: Bundle().also {
            bundles.putBundle(key, it)
        }
        return BundlizerPmStateSaver(bundle)
    }

    companion object {
        private const val PM_STATE_KEY = "pm_state"
    }
}
