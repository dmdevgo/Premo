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

package me.dmdev.premo.compose

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.*
import androidx.compose.ui.*
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.annotation.ExperimentalPremoApi
import me.dmdev.premo.navigation.BackStackChange
import me.dmdev.premo.navigation.StackNavigation

@ExperimentalPremoApi
@Composable
fun StackNavigation.bindNavigation(): BackStackChange {
    return backStackChangesFlow.collectAsState(
        currentTop?.let { BackStackChange.Set(it, listOf()) } ?: BackStackChange.Nothing
    ).value
}

@ExperimentalPremoApi
@Composable
fun NavigationBox(
    navigation: StackNavigation,
    modifier: Modifier = Modifier,
    content: @Composable (PresentationModel?) -> Unit
) {
    val backstackChange = navigation.backStackChangesFlow
        .collectAsState(BackStackChange.Nothing).value

    NavigationBox(
        backStackChange = backstackChange,
        modifier = modifier,
        content = content
    )
}

@ExperimentalPremoApi
@Composable
fun NavigationBox(
    backStackChange: BackStackChange,
    modifier: Modifier = Modifier,
    content: @Composable (PresentationModel?) -> Unit
) {
    val stateHolder = rememberSaveableStateHolder()

    fun removePms(pms: List<PresentationModel>) {
        pms.forEach {
            stateHolder.removeState(it.tag)
        }
    }

    val pm = when (backStackChange) {
        is BackStackChange.Push -> {
            removePms(backStackChange.removedPms)
            backStackChange.enterPm
        }

        is BackStackChange.Pop -> {
            removePms(backStackChange.removedPms)
            backStackChange.enterPm
        }

        is BackStackChange.Set -> {
            removePms(backStackChange.removedPms)
            backStackChange.pm
        }

        is BackStackChange.Nothing -> null
    }

    Box(modifier) {
        stateHolder.SaveableStateProvider(pm?.tag ?: "") {
            content(pm)
        }
    }
}

@ExperimentalPremoApi
@Composable
fun AnimatedNavigationBox(
    navigation: StackNavigation,
    modifier: Modifier = Modifier,
    enterTransition: ((initialPm: PresentationModel, targetPm: PresentationModel) -> EnterTransition) =
        { _, _ -> slideInHorizontally { height -> height } },
    exitTransition: ((initialPm: PresentationModel, targetPm: PresentationModel) -> ExitTransition) =
        { _, _ -> slideOutHorizontally { height -> -height } },
    popEnterTransition: ((initialPm: PresentationModel, targetPm: PresentationModel) -> EnterTransition) =
        { _, _ -> slideInHorizontally { height -> -height } },
    popExitTransition: ((initialPm: PresentationModel, targetPm: PresentationModel) -> ExitTransition) =
        { _, _ -> slideOutHorizontally { height -> height } },
    setTransition: ((pm: PresentationModel) -> EnterTransition) = { EnterTransition.None },
    defaultTransition: (() -> EnterTransition) = { EnterTransition.None },
    content: @Composable (PresentationModel?) -> Unit
) {
    AnimatedNavigationBox(
        backStackChange = navigation.bindNavigation(),
        modifier = modifier,
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition,
        setTransition = setTransition,
        defaultTransition = defaultTransition,
        content = content
    )
}

@ExperimentalPremoApi
@Composable
fun AnimatedNavigationBox(
    backStackChange: BackStackChange,
    modifier: Modifier = Modifier,
    enterTransition: ((initialPm: PresentationModel, targetPm: PresentationModel) -> EnterTransition) =
        { _, _ -> slideInHorizontally { height -> height } },
    exitTransition: ((initialPm: PresentationModel, targetPm: PresentationModel) -> ExitTransition) =
        { _, _ -> slideOutHorizontally { height -> -height } },
    popEnterTransition: ((initialPm: PresentationModel, targetPm: PresentationModel) -> EnterTransition) =
        { _, _ -> slideInHorizontally { height -> -height } },
    popExitTransition: ((initialPm: PresentationModel, targetPm: PresentationModel) -> ExitTransition) =
        { _, _ -> slideOutHorizontally { height -> height } },
    setTransition: ((pm: PresentationModel) -> EnterTransition) = { EnterTransition.None },
    defaultTransition: (() -> EnterTransition) = { EnterTransition.None },
    content: @Composable (PresentationModel?) -> Unit
) {
    AnimatedContent(
        targetState = backStackChange,
        contentAlignment = Alignment.Center,
        transitionSpec = {
            when (backStackChange) {
                is BackStackChange.Push -> {
                    enterTransition(backStackChange.exitPm, backStackChange.enterPm) togetherWith
                        exitTransition(backStackChange.exitPm, backStackChange.enterPm)
                }

                is BackStackChange.Pop -> {
                    popEnterTransition(backStackChange.exitPm, backStackChange.enterPm) togetherWith
                        popExitTransition(backStackChange.exitPm, backStackChange.enterPm)
                }

                is BackStackChange.Set -> {
                    setTransition(backStackChange.pm) togetherWith ExitTransition.None
                }

                else -> {
                    defaultTransition() togetherWith ExitTransition.None
                }
            }
        }
    ) { backstackChange: BackStackChange ->
        NavigationBox(
            backStackChange = backstackChange,
            modifier = modifier,
            content = content
        )
    }
}
