/*
 * MIT License
 *
 * Copyright (c) 2020 Dmitriy Gorbunov
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

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import me.dmdev.premo.view.PmActivity

class MainActivity : PmActivity<CounterPm>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun providePresentationModel(): CounterPm {
        return CounterPm()
    }

    override fun onBindPresentationModel(pm: CounterPm) {

        pm.count bindTo {
            countText.text = it.toString()
        }

        pm.plusButtonEnabled bindTo {
            plusButton.isEnabled = it
        }

        pm.minusButtonEnabled bindTo {
            minusButton.isEnabled = it
        }

        plusButton.setOnClickListener {
            pm.plus.accept(Unit)
        }

        minusButton.setOnClickListener {
            pm.minus.accept(Unit)
        }
    }
}
