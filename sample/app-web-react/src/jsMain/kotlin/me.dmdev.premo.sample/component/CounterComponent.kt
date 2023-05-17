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

package me.dmdev.premo.sample.component

import me.dmdev.premo.sample.CounterPm
import me.dmdev.premo.sample.collectAsState
import mui.material.Button
import mui.material.ButtonVariant
import mui.material.Stack
import mui.material.StackDirection
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import web.cssom.AlignItems
import web.cssom.JustifyContent

external interface CounterComponentProps : Props {
    var pm: CounterPm
}

val CounterComponent = FC<CounterComponentProps> { props ->

    val state = props.pm.stateFlow.collectAsState()

    Stack {

        direction = responsive(StackDirection.row)
        spacing = responsive(2)

        sx {
            justifyContent = JustifyContent.center
            alignItems = AlignItems.center
        }

        Button {
            onClick = {
                props.pm.minus()
            }
            variant = ButtonVariant.contained
            disabled = !state.minusEnabled
            +"-"
        }

        Typography {
            variant = TypographyVariant.h5
            +"${state.count}"
        }

        Button {
            onClick = {
                props.pm.plus()
            }
            variant = ButtonVariant.contained
            disabled = !state.plusEnabled
            +"+"
        }
    }
}
