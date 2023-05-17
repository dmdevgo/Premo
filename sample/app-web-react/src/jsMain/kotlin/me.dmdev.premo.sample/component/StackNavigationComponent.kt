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

import me.dmdev.premo.sample.collectAsState
import me.dmdev.premo.sample.component
import me.dmdev.premo.sample.stacknavigation.StackNavigationPm
import mui.material.Button
import mui.material.ButtonVariant
import mui.material.Container
import mui.material.Stack
import mui.material.StackDirection
import mui.material.Typography
import mui.material.TypographyAlign
import mui.material.styles.TypographyVariant
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import web.cssom.AlignItems
import web.cssom.JustifyContent
import web.cssom.px

external interface StackNavigationComponentProps : Props {
    var pm: StackNavigationPm
}

val StackNavigationComponent = FC<StackNavigationComponentProps> { props ->

    val pm = props.pm
    val backStackAsString = pm.backstackAsStringState.collectAsState()
    val currentPm = pm.navigation.currentTopFlow.collectAsState()

    Stack {
        direction = responsive(StackDirection.column)
        spacing = responsive(2)

        sx {
            justifyContent = JustifyContent.center
            alignItems = AlignItems.center
        }

        component(currentPm)

        Container {
            maxWidth = 300.px
            Typography {
                align = TypographyAlign.center
                variant = TypographyVariant.h6
                +"Stack: $backStackAsString"
            }
        }

        Stack {
            direction = responsive(StackDirection.row)
            spacing = responsive(2)

            Button {
                onClick = {
                    pm.pushClick()
                }
                variant = ButtonVariant.contained
                +"Push"
            }

            Button {
                onClick = {
                    pm.popClick()
                }
                variant = ButtonVariant.contained
                +"Pop"
            }

            Button {
                onClick = {
                    pm.popToRootClick()
                }
                variant = ButtonVariant.contained
                +"Pop to root"
            }
        }

        Stack {
            direction = responsive(StackDirection.row)
            spacing = responsive(2)

            Button {
                onClick = {
                    pm.replaceTopClick()
                }
                variant = ButtonVariant.contained
                +"Replace top"
            }

            Button {
                onClick = {
                    pm.replaceAllClick()
                }
                variant = ButtonVariant.contained
                +"Replace all"
            }
        }

        Button {
            onClick = {
                pm.setBackstackClick()
            }
            variant = ButtonVariant.contained
            +"Set back stack"
        }
    }
}
