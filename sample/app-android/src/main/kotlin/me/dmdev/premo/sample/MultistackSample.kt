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

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.dmdev.premo.invoke
import me.dmdev.premo.navigation.PmStackChange

@Composable
fun bottomBarScreen(pm: BottomBarPm) {

    val currentTabPm = pm.currentTabPm.bind()

    Scaffold(
        bottomBar = {
            BottomNavigation {
                pm.tabPmList.forEach { tabPm ->
                    BottomNavigationItem(
                        selected = tabPm == currentTabPm,
                        onClick = { pm.onRouterTabClick(tabPm) },
                        label = { Text(tabPm.tabTitle) },
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
        navigation(currentTabPm.pmStackChanges.bind(PmStackChange.Empty)) { pm ->
            when (pm) {
                is TabItemPm -> itemScreen(pm)
                else -> emptyScreen()
            }
        }
    }
}

@Composable
fun itemScreen(pmTab: TabItemPm) {
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