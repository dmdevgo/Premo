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

package me.dmdev.premo.sample.screen

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.*
import me.dmdev.premo.ExperimentalPremoApi
import me.dmdev.premo.navigation.back
import me.dmdev.premo.sample.AnimatedNavigationBox
import me.dmdev.premo.sample.CardBox
import me.dmdev.premo.sample.EmptyBox
import me.dmdev.premo.sample.ScreenBox
import me.dmdev.premo.sample.Stubs
import me.dmdev.premo.sample.bind
import me.dmdev.premo.sample.bindNavigation
import me.dmdev.premo.sample.bottomnavigation.BottomNavigationPm
import me.dmdev.premo.sample.bottomnavigation.TabItemPm
import me.dmdev.premo.sample.bottomnavigation.TabPm

@OptIn(ExperimentalPremoApi::class)
@Composable
fun BottomNavigationScreen(
    pm: BottomNavigationPm
) {
    val currentTabPm = pm.navigation.currentFlow.bind()

    ScreenBox(
        title = "Bottom Navigation",
        backHandler = { pm.back() }
    ) {
        Scaffold(
            backgroundColor = Color.Transparent,
            bottomBar = {
                BottomNavigation {
                    pm.navigation.values.forEachIndexed { index, tabPm ->
                        val title = (tabPm as? TabPm)?.tabTitle ?: ""
                        BottomNavigationItem(
                            selected = tabPm == currentTabPm,
                            onClick = { pm.navigation.onChangeCurrent(index) },
                            label = { Text(title) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
            }
        ) {
            if (currentTabPm is TabPm) {
                AnimatedNavigationBox(
                    backStackChange = currentTabPm.navigation.bindNavigation(),
                    enterTransition = { _, _ -> slideInHorizontally { height -> height } },
                    exitTransition = { _, _ -> slideOutHorizontally { height -> -height } },
                    popEnterTransition = { _, _ -> slideInHorizontally { height -> -height } },
                    popExitTransition = { _, _ -> slideOutHorizontally { height -> height } }
                ) { pm ->
                    when (pm) {
                        is TabItemPm -> ItemScreen(pm)
                        else -> EmptyBox()
                    }
                }
            }
        }
    }
}

@Composable
fun ItemScreen(pmTab: TabItemPm = Stubs.tabItemPm) {
    CardBox {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                fontSize = 24.sp,
                text = pmTab.screenTitle
            )
            Spacer(modifier = Modifier.height(32.dp))
            Row {
                Button(
                    onClick = { pmTab.previousClick() },
                    modifier = Modifier.width(120.dp)
                ) {
                    Text("Previous")
                }
                Spacer(Modifier.width(16.dp))
                Button(
                    onClick = { pmTab.nextClick() },
                    modifier = Modifier.width(120.dp)
                ) {
                    Text("Next")
                }
            }
        }
    }
}
