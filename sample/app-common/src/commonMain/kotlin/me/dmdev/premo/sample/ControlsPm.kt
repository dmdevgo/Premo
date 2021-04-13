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

import kotlinx.serialization.Serializable
import me.dmdev.premo.*

class ControlsPm(config: PmConfig) : PresentationModel(config) {

    @Serializable
    object Description : Saveable

    val input = Input(
        initialText = "",
        formatter = { it.toUpperCase() }
    )

    val check = Check(
        initialChecked = true,
        onChange = {
            input.enabled.value = it
        }
    )

}

@Suppress("FunctionName")
fun PresentationModel.Input(
    initialText: String = "",
    formatter: (String) -> String = { it }
): InputPm {
    return AttachedChild { config ->
        InputPm(initialText, formatter, config)
    }
}

@Suppress("FunctionName")
fun PresentationModel.Check(
    initialChecked: Boolean,
    onChange: (checked: Boolean) -> Unit = {}
): CheckPm {
    return AttachedChild { config ->
        CheckPm(initialChecked, onChange, config)
    }
}

class InputPm(
    initialText: String,
    formatter: (String) -> String = { it },
    config: PmConfig
) : PresentationModel(config) {

    val text = State(formatter(initialText))
    val textChanges = Action<String> {
        if (enabled.value) {
            text.value = formatter(it)
        } else {
            text.value
        }
    }

    val enabled = State(true)
}

class CheckPm(
    initialChecked: Boolean,
    onChange: (checked: Boolean) -> Unit,
    config: PmConfig
) : PresentationModel(config) {
    val checked = State(initialChecked)
    val checkedChanges = Action<Boolean> {
        checked.value = it
        onChange(it)
    }
}