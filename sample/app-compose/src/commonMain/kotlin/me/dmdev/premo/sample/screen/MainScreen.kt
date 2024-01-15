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

package me.dmdev.premo.sample.screen

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import me.dmdev.premo.ExperimentalPremoApi
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.sample.CounterPm
import me.dmdev.premo.sample.EmptyBox
import me.dmdev.premo.sample.LocalWindowSizes
import me.dmdev.premo.sample.MainPm
import me.dmdev.premo.sample.Stubs
import me.dmdev.premo.sample.WindowSizeClass.Medium
import me.dmdev.premo.sample.bind
import me.dmdev.premo.sample.bottomnavigation.BottomNavigationPm
import me.dmdev.premo.sample.dialognavigation.DialogNavigationPm
import me.dmdev.premo.sample.stacknavigation.StackNavigationPm

@OptIn(ExperimentalPremoApi::class)
@Composable
fun MainScreen(mainPm: MainPm) {
    val detailPm = mainPm.navigation.detailFlow.bind()

    if (LocalWindowSizes.current.value.widthSizeClass >= Medium) {
        ExpandedMainScreen(mainPm, detailPm)
    } else {
        CompactMainScreen(mainPm, detailPm)
    }
}

@Composable
fun CompactMainScreen(
    mainPm: MainPm = Stubs.mainPm,
    detailPm: PresentationModel?
) {
    AnimatedContent(
        targetState = detailPm,
        transitionSpec = {
            if (targetState != null) {
                slideInHorizontally { height -> height } togetherWith
                    slideOutHorizontally { height -> -height }
            } else {
                slideInHorizontally { height -> -height } togetherWith
                    slideOutHorizontally { height -> height }
            }
        }
    ) { pm ->
        if (pm == null) {
            SamplesScreen(mainPm.navigation.master)
        } else {
            pm.mapToComposable()
        }
    }
}

@Composable
fun ExpandedMainScreen(
    mainPm: MainPm,
    detailPm: PresentationModel?
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
            SamplesScreen(mainPm.navigation.master)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(0.5f)
        ) {
            detailPm.mapToComposable()
        }
    }
}

@Composable
private fun PresentationModel?.mapToComposable() {
    when (this) {
        is CounterPm -> CounterScreen(this)
        is StackNavigationPm -> StackNavigationScreen(this)
        is BottomNavigationPm -> BottomNavigationScreen(this)
        is DialogNavigationPm -> DialogNavigationScreen(this)
        else -> EmptyBox()
    }
}
