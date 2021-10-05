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

package me.dmdev.premo.lifecycle

import me.dmdev.premo.lifecycle.LifecycleEvent.*
import me.dmdev.premo.lifecycle.LifecycleState.*

class Lifecycle {

    private val observers: MutableList<LifecycleObserver> = mutableListOf()

    var state: LifecycleState = INITIALIZED
        private set

    fun addObserver(observer: LifecycleObserver) {
        observers.add(observer)
    }

    fun removeObserver(observer: LifecycleObserver) {
        observers.remove(observer)
    }

    fun moveTo(targetState: LifecycleState) {

        if (targetState == state) return

        fun notifyOnCreate() {
            notifyChange(CREATED, ON_CREATE)
        }

        fun notifyOnForeground() {
            notifyChange(IN_FOREGROUND, ON_FOREGROUND)
        }

        fun notifyOnBackground() {
            notifyChange(CREATED, ON_BACKGROUND)
        }

        fun notifyOnDestroy() {
            notifyChange(DESTROYED, ON_DESTROY)
        }

        when (targetState) {
            INITIALIZED -> {
                // do nothing, initial lifecycle state is INITIALIZED
            }
            CREATED -> {
                when (state) {
                    INITIALIZED -> {
                        notifyOnCreate()
                    }
                    IN_FOREGROUND -> {
                        notifyOnBackground()
                    }
                    else -> { /*do nothing */
                    }
                }
            }
            IN_FOREGROUND -> {
                when (state) {
                    INITIALIZED -> {
                        notifyOnCreate()
                        notifyOnForeground()
                    }
                    CREATED -> {
                        notifyOnForeground()
                    }
                    else -> { /*do nothing */
                    }
                }
            }
            DESTROYED -> {
                when (state) {
                    INITIALIZED -> {
                        notifyOnDestroy()
                    }
                    CREATED -> {
                        notifyOnDestroy()
                    }
                    IN_FOREGROUND -> {
                        notifyOnBackground()
                        notifyOnDestroy()
                    }
                    DESTROYED -> { /*do nothing */
                    }
                }
            }
        }
    }

    private fun notifyChange(state: LifecycleState, event: LifecycleEvent) {
        this.state = state

        observers.forEach { observer ->
            observer.onLifecycleChange(this, event)
        }

        if (event == ON_DESTROY) {
            observers.clear()
        }
    }
}