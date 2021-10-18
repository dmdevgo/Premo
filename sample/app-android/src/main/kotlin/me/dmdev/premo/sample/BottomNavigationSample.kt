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

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.dmdev.premo.sample.bottom_navigation.BottomNavigationPm
import me.dmdev.premo.sample.bottom_navigation.TabItemPm
import me.dmdev.premo.sample.bottom_navigation.TabPm

@OptIn(ExperimentalAnimationApi::class)
@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun BottomNavigationScreen(pm: BottomNavigationPm = Stubs.bottomBarPm) {

    val currentTabPm = pm.navigator.currentState.bind()

    Scaffold(
        bottomBar = {
            BottomNavigation {
                pm.navigator.values.forEach { tabPm ->
                    val title = (tabPm as? TabPm)?.tabTitle ?: ""
                    BottomNavigationItem(
                        selected = tabPm == currentTabPm,
                        onClick = { pm.navigator.setCurrent(tabPm) },
                        label = { Text(title) },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Android,
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
                navigation = currentTabPm.navigation,
                enterTransition = { _, _ -> slideInHorizontally({ height -> height }) },
                exitTransition = { _, _ -> slideOutHorizontally({ height -> -height }) },
                popEnterTransition = { _, _ -> slideInHorizontally({ height -> -height }) },
                popExitTransition = { _, _ -> slideOutHorizontally({ height -> height }) },
            ) { pm ->
                when (pm) {
                    is TabItemPm -> ItemScreen(pm)
                    else -> EmptyScreen()
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun ItemScreen(pmTab: TabItemPm = Stubs.tabItemPm) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            fontSize = 24.sp,
            text = pmTab.screenTitle
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            fontSize = 24.sp,
            text = pmTab.tabTitle
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { pmTab.nextClick() }) {
            Text("Next")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { pmTab.previousClick() }) {
            Text("Previous")
        }
    }
}