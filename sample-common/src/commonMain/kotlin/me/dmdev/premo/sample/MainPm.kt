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

import me.dmdev.premo.PresentationModel
import me.dmdev.premo.navigation.NavigationMessage
import me.dmdev.premo.navigation.PmFactory
import kotlin.reflect.KClass

class MainPm : PresentationModel() {

    private val pmFactory = object : PmFactory {
        override fun createPm(
            pmClass: KClass<out PresentationModel>,
            params: Any?
        ): PresentationModel {
            return when (pmClass) {
                SamplesPm::class -> SamplesPm()
                CounterPm::class -> CounterPm(params as CounterPm.Params)
                else -> throw IllegalStateException("Not handled instance creation for class $pmClass")
            }
        }
    }

    val router = Router(pmFactory)

    override fun onCreate() {
        super.onCreate()
        if (router.pmStack.isEmpty()) {
            router.push(SamplesPm::class, null)
        }
    }
    override fun handleNavigationMessage(message: NavigationMessage) {
        when (message) {
            CounterSampleMessage -> router.push(CounterPm::class, CounterPm.Params(10))
            else -> super.handleNavigationMessage(message)
        }
    }

    override fun handleBack(): Boolean {
        return if (router.pmStack.size > 1) {
            router.pop()
            true
        } else {
            false
        }
    }
}
