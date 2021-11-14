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

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.sample.bottom_navigation.BottomNavigationPm
import me.dmdev.premo.sample.stack_navigation.StackNavigationPm

@Composable
fun MainScreen(mainPm: MainPm, windowSizes: WindowSizes) {

    val detailPm = mainPm.navigation.detailPmState.bind()

    if (windowSizes.widthSizeClass == WindowSizeClass.Expanded) {
        ExpandedMainScreen(mainPm, detailPm)
    } else {
        CompactMainScreen(mainPm, detailPm)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun CompactMainScreen(
    mainPm: MainPm = Stubs.mainPm,
    detailPm: PresentationModel? = null
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
            SamplesScreen(mainPm.navigation.masterPm)
        } else {
            when (pm) {
                is CounterPm -> CounterScreen(pm)
                is StackNavigationPm -> StackNavigationScreen(pm)
                is BottomNavigationPm -> BottomNavigationScreen(pm)
                else -> EmptyScreen()
            }
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    device = Devices.NEXUS_7,
)
@Composable
fun ExpandedMainScreen(
    mainPm: MainPm = Stubs.mainPm,
    detailPm: PresentationModel? = null
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
            SamplesScreen(pm = mainPm.navigation.masterPm)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(0.5f)
        ) {
            when (detailPm) {
                is CounterPm -> CounterScreen(detailPm)
                is StackNavigationPm -> StackNavigationScreen(detailPm)
                is BottomNavigationPm -> BottomNavigationScreen(detailPm)
                else -> EmptyScreen()
            }
        }
    }
}