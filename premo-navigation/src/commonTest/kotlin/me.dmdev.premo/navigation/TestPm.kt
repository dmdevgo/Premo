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

package me.dmdev.premo.navigation

import me.dmdev.premo.PmDescription
import me.dmdev.premo.PmParams
import me.dmdev.premo.PresentationModel

class TestPm(
    pmParams: PmParams = PmParams(
        parent = null,
        description = ROOT_PM_DESCRIPTION,
        factory = TestPmFactory(),
        stateSaverFactory = TestStateSaverFactory()
    )
) : PresentationModel(pmParams) {

    data class Description(
        override val key: String = "test_pm"
    ) : PmDescription

    companion object {
        val ROOT_PM_KEY = "root_pm"
        val ROOT_PM_DESCRIPTION = Description(ROOT_PM_KEY)

        val PM1_DESCRIPTION = Description("pm1")
        val PM2_DESCRIPTION = Description("pm2")
        val PM3_DESCRIPTION = Description("pm3")
        val PM4_DESCRIPTION = Description("pm4")
        val PM5_DESCRIPTION = Description("pm5")
        val PM6_DESCRIPTION = Description("pm6")

        fun buildRootPm(stateSaverFactory: TestStateSaverFactory = TestStateSaverFactory()): TestPm {
            return TestPm(
                pmParams = PmParams(
                    description = ROOT_PM_DESCRIPTION,
                    parent = null,
                    factory = TestPmFactory(),
                    stateSaverFactory = stateSaverFactory
                )
            )
        }
    }
}
