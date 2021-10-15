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

import me.dmdev.premo.PmLifecycle.Event.*
import me.dmdev.premo.PmLifecycle.State.*

class PmLifecycle {

    private val observers: MutableList<Observer> = mutableListOf()

    var state: State = CREATED
        private set

    fun addObserver(observer: Observer) {
        observers.add(observer)
    }

    fun removeObserver(observer: Observer) {
        observers.remove(observer)
    }

    fun moveTo(targetState: State) {

        if (targetState == state) return

        when (targetState) {
            CREATED -> {
                when (state) {
                    IN_FOREGROUND -> {
                        notifyChange(CREATED, ON_BACKGROUND)
                    }
                    else -> { /*do nothing */
                    }
                }
            }
            IN_FOREGROUND -> {
                when (state) {
                    CREATED -> {
                        notifyChange(IN_FOREGROUND, ON_FOREGROUND)
                    }
                    else -> { /*do nothing */
                    }
                }
            }
            DESTROYED -> {
                when (state) {
                    CREATED -> {
                        notifyChange(DESTROYED, ON_DESTROY)
                    }
                    IN_FOREGROUND -> {
                        notifyChange(CREATED, ON_BACKGROUND)
                        notifyChange(DESTROYED, ON_DESTROY)
                    }
                    DESTROYED -> { /*do nothing */
                    }
                }
            }
        }
    }

    enum class State {
        CREATED,
        IN_FOREGROUND,
        DESTROYED
    }

    enum class Event {
        ON_FOREGROUND,
        ON_BACKGROUND,
        ON_DESTROY
    }

    interface Observer {
        fun onLifecycleChange(lifecycle: PmLifecycle, event: Event)
    }

    private fun notifyChange(state: State, event: Event) {
        this.state = state

        observers.forEach { observer ->
            observer.onLifecycleChange(this, event)
        }

        if (event == ON_DESTROY) {
            observers.clear()
        }
    }
}