/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020-2022 Dmitriy Gorbunov (dmitriy.goto@gmail.com)
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

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.sample.WindowSizeClass.Medium
import me.dmdev.premo.sample.bottom_navigation.BottomNavigationPm
import me.dmdev.premo.sample.dilaog_navigation.DialogNavigationPm
import me.dmdev.premo.sample.stack_navigation.StackNavigationPm

@Composable
fun MainScreen(mainPm: MainPm, windowSizes: WindowSizes) {

    val detailPm = mainPm.navigation.detailFlow.bind()

    if (windowSizes.widthSizeClass >= Medium) {
        ExpandedMainScreen(mainPm, detailPm, windowSizes)
    } else {
        CompactMainScreen(mainPm, detailPm, windowSizes)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CompactMainScreen(
    mainPm: MainPm = Stubs.mainPm,
    detailPm: PresentationModel?,
    windowSizes: WindowSizes
) {

    AnimatedContent(
        targetState = detailPm,
        transitionSpec = {
            if (targetState != null) {
                slideInHorizontally { height -> height } with
                        slideOutHorizontally { height -> -height }
            } else {
                slideInHorizontally { height -> -height } with
                        slideOutHorizontally { height -> height }
            }
        }
    ) { pm ->
        if (pm == null) {
            SamplesScreen(mainPm.navigation.master, windowSizes)
        } else {
            pm.mapToComposable(windowSizes)
        }
    }
}

@Composable
fun ExpandedMainScreen(
    mainPm: MainPm,
    detailPm: PresentationModel?,
    windowSizes: WindowSizes
) {
    Row(
        Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(0.5f)
        ) {
            SamplesScreen(mainPm.navigation.master, windowSizes)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(0.5f)
        ) {
            detailPm.mapToComposable(windowSizes)
        }
    }
}

@Composable
private fun PresentationModel?.mapToComposable(windowSizes: WindowSizes) {
    when (this) {
        is CounterPm -> CounterScreen(this, windowSizes)
        is StackNavigationPm -> StackNavigationScreen(this, windowSizes)
        is BottomNavigationPm -> BottomNavigationScreen(this, windowSizes)
        is DialogNavigationPm -> DialogNavigationScreen(this, windowSizes)
        else -> EmptyBox()
    }
}