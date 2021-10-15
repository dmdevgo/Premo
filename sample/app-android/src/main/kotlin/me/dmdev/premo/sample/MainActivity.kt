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

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import me.dmdev.premo.PmActivity
import me.dmdev.premo.PmActivityDelegate

class MainActivity : PmActivity<MainPm>(R.layout.activity_main) {

    override val delegate: PmActivityDelegate<MainPm> by lazy {
        PmActivityDelegate(
            pmActivity = this,
            pmDescription = MainPm.Description,
            pmFactory = MainPmFactory(),
            pmStateSaver = JsonPmStateSaver(),
        )
    }

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen(delegate.presentationModel)
        }
    }

    override fun onBackPressed() {
        if (delegate.presentationModel.messageHandler.handle(SystemBackMessage).not()) {
            super.onBackPressed()
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    fun MainScreen(mainPm: MainPm) {
        AnimatedNavigationBox(
            navigation = mainPm.navigation,
            enterTransition = { _, _ -> slideInHorizontally({ height -> height }) },
            exitTransition = { _, _ -> slideOutHorizontally({ height -> -height }) },
            popEnterTransition = { _, _ -> slideInHorizontally({ height -> -height }) },
            popExitTransition = { _, _ -> slideOutHorizontally({ height -> height }) },
        ) { pm ->
            when (pm) {
                is SamplesPm -> SamplesScreen(pm)
                is CounterPm -> CounterScreen(pm)
                is BottomBarPm -> BottomBarScreen(pm)
                else -> EmptyScreen()
            }
        }
    }
}