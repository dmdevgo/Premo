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

package me.dmdev.premo.sample

import android.app.Activity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.*
import androidx.window.layout.WindowMetricsCalculator
import me.dmdev.premo.PmActivity
import me.dmdev.premo.PmActivityDelegate
import me.dmdev.premo.navigation.handleBack
import me.dmdev.premo.sample.serialization.Serializers
import me.dmdev.premo.saver.JsonBundleStateSaver

class MainActivity : AppCompatActivity(), PmActivity<MainPm> {

    override val delegate: PmActivityDelegate<MainPm> by lazy {
        val pmStateSaver = JsonBundleStateSaver(Serializers.json)
//        val pmStateSaver = ParcelableBundleStateSaver()
//        val pmStateSaver = ProtoBufBundleStateSaver()
//        val pmStateSaver = BundlizerBundleStateSaver()
        PmActivityDelegate(
            pmActivity = this,
            pmDelegate = PremoSample.createPmDelegate(pmStateSaver),
            stateSaver = pmStateSaver
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val windowSizes = rememberWindowSizes()
            App(delegate.presentationModel, windowSizes)
        }
    }

    override fun onBackPressed() {
        if (delegate.presentationModel.handleBack().not()) {
            super.onBackPressed()
        }
    }
}

@Composable
fun Activity.rememberWindowSizes(): WindowSizes {
    val configuration = LocalConfiguration.current
    val windowMetrics = remember(configuration) {
        WindowMetricsCalculator.getOrCreate()
            .computeCurrentWindowMetrics(this)
    }

    val windowDpSize = with(LocalDensity.current) {
        windowMetrics.bounds.toComposeRect().size.toDpSize()
    }

    return windowSizes(windowDpSize.width, windowDpSize.height)
}
