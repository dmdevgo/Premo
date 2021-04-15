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

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

class PmLifecycle {

    enum class Event {
        ON_CREATE,
        ON_FOREGROUND,
        ON_BACKGROUND,
        ON_DESTROY
    }

    enum class State {
        INITIALIZED,
        CREATED,
        IN_FOREGROUND,
        DESTROYED
    }

    private val _stateFlow = MutableStateFlow(State.INITIALIZED)
    val stateFlow: StateFlow<PmLifecycle.State> = _stateFlow
    val state: PmLifecycle.State get() = _stateFlow.value

    private val _eventFlow = MutableSharedFlow<Event>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val eventFlow: SharedFlow<PmLifecycle.Event> = _eventFlow

    internal fun moveTo(targetState: PmLifecycle.State) {

        fun notifyOnCreate() {
            _stateFlow.value = State.CREATED
            _eventFlow.tryEmit(Event.ON_CREATE)
        }

        fun notifyOnForeground() {
            _stateFlow.value = State.IN_FOREGROUND
            _eventFlow.tryEmit(Event.ON_FOREGROUND)
        }

        fun notifyOnBackground() {
            _stateFlow.value = State.CREATED
            _eventFlow.tryEmit(Event.ON_BACKGROUND)
        }

        fun notifyOnDestroy() {
            _stateFlow.value = State.DESTROYED
            _eventFlow.tryEmit(Event.ON_DESTROY)
        }
        
        when (targetState) {
            State.INITIALIZED -> {
                // do nothing, initial lifecycle state is INITIALIZED
            }
            State.CREATED -> {
                when (stateFlow.value) {
                    State.INITIALIZED -> {
                        notifyOnCreate()
                    }
                    State.IN_FOREGROUND -> {
                        notifyOnBackground()
                    }
                    else -> { /*do nothing */
                    }
                }
            }
            State.IN_FOREGROUND -> {
                when (stateFlow.value) {
                    State.INITIALIZED -> {
                        notifyOnCreate()
                        notifyOnForeground()
                    }
                    State.CREATED -> {
                        notifyOnForeground()
                    }
                    else -> { /*do nothing */
                    }
                }
            }
            State.DESTROYED -> {
                when (stateFlow.value) {
                    State.INITIALIZED -> {
                        notifyOnDestroy()
                    }
                    State.CREATED -> {
                        notifyOnDestroy()
                    }
                    State.IN_FOREGROUND -> {
                        notifyOnBackground()
                        notifyOnDestroy()
                    }
                    State.DESTROYED -> { /*do nothing */
                    }
                }
            }
        }
    }
}