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

import androidx.compose.runtime.collectAsState
import me.dmdev.premo.PmLifecycle
import me.dmdev.premo.PmParams
import me.dmdev.premo.sample.CounterPm
import me.dmdev.premo.sample.JsonPmStateSaver
import me.dmdev.premo.sample.MainPmFactory
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable

val pm = CounterPm(
    10,
    PmParams(
        "Counter",
        null,
        CounterPm.Description(10),
        mapOf(),
        MainPmFactory(),
        JsonPmStateSaver()
    )
)

fun main() {

    pm.lifecycle.moveTo(PmLifecycle.State.IN_FOREGROUND)

    renderComposable(rootElementId = "root") {

        val count = pm.count.collectAsState().value
        val minusButtonEnabled = pm.minusButtonEnabled.collectAsState().value
        val plusButtonEnabled = pm.plusButtonEnabled.collectAsState().value

        Div({ style { padding(25.px) } }) {
            Button(attrs = {
                onClick { pm.minus() }
                contentEditable(false)
                if (minusButtonEnabled.not()) disabled()
            }) {
                Text("-")
            }

            Span({ style { padding(15.px) } }) {
                Text("$count")
            }

            Button(attrs = {
                onClick { pm.plus() }
                contentEditable(false)
                if (plusButtonEnabled.not()) disabled()
            }) {
                Text("+")
            }
        }
    }
}