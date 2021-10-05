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

package me.dmdev.premo.navigation

import me.dmdev.premo.PresentationModel
import me.dmdev.premo.State
import me.dmdev.premo.lifecycle.Lifecycle
import me.dmdev.premo.lifecycle.LifecycleEvent
import me.dmdev.premo.lifecycle.LifecycleObserver
import me.dmdev.premo.lifecycle.LifecycleState.*
import me.dmdev.premo.value

class Navigator internal constructor(
    private val lifecycle: Lifecycle
) {

    internal val backstackState: State<List<PresentationModel>> = State(listOf())

    var backstack: List<PresentationModel>
        get() = backstackState.value
        private set(value) {
            backstackState.value = value
        }

    init {
        subscribeToLifecycle()
    }

    fun push(pm: PresentationModel) {
        backstack.lastOrNull()?.lifecycle?.moveTo(CREATED)
        pm.lifecycle.moveTo(lifecycle.state)
        backstack = backstack.plus(pm)
    }

    fun pop(): Boolean {
        return if (backstack.isNotEmpty()) {
            backstack.lastOrNull()?.lifecycle?.moveTo(DESTROYED)
            if (backstack.isNotEmpty()) backstack = backstack.dropLast(1)
            backstack.lastOrNull()?.lifecycle?.moveTo(lifecycle.state)
            true
        } else {
            false
        }
    }

    fun setBackStack(pmList: List<PresentationModel>) {
        backstack = pmList
        pmList.forEach { pm ->
            pm.lifecycle.moveTo(INITIALIZED)
        }
        pmList.lastOrNull()?.lifecycle?.moveTo(lifecycle.state)
    }

    fun handleBack(): Boolean {
        return if (backstack.size > 1) {
            pop()
            true
        } else {
            false
        }
    }

    private fun subscribeToLifecycle() {
        lifecycle.addObserver(object : LifecycleObserver{
            override fun onLifecycleChange(lifecycle: Lifecycle, event: LifecycleEvent) {
                when(lifecycle.state) {
                    INITIALIZED,
                    CREATED,
                    DESTROYED -> {
                        backstack.forEach { pm ->
                            pm.lifecycle.moveTo(lifecycle.state)
                        }
                    }

                    IN_FOREGROUND -> {
                        backstack.lastOrNull()?.lifecycle?.moveTo(lifecycle.state)
                    }
                }
            }
        })
    }
}