/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020-2024 Dmitriy Gorbunov (dmitriy.goto@gmail.com)
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

import me.dmdev.premo.PmArgs
import me.dmdev.premo.PmMessage
import me.dmdev.premo.PresentationModel

class TestPm(args: Args) : PresentationModel(args) {

    data class Args(
        override val key: String = "test_pm"
    ) : PmArgs()

    sealed class ResultMessage : PmMessage {
        data object Ok : ResultMessage()
        data object Cancel : ResultMessage()
    }

    fun sendResultMessage(resultMessage: ResultMessage) {
        messageHandler.send(resultMessage)
    }

    companion object {
        const val ROOT_PM_KEY = "root_pm"

        val PM1_ARGS = Args("pm1")
        val PM2_ARGS = Args("pm2")
        val PM3_ARGS = Args("pm3")
        val PM4_ARGS = Args("pm4")
        val PM5_ARGS = Args("pm5")
        val PM6_ARGS = Args("pm6")

        fun buildRootPm(pmStateSaverFactory: TestStateSaverFactory = TestStateSaverFactory()): TestPm {
            return TestPm(
                Args(ROOT_PM_KEY).apply {
                    overridePmFactory(TestPmFactory())
                    overridePmStateSaverFactory(pmStateSaverFactory)
                }
            )
        }
    }
}
