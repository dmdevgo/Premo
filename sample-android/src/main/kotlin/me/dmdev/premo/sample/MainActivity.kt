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
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import me.dmdev.premo.PmActivity
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.Saveable
import me.dmdev.premo.invoke
import me.dmdev.premo.navigation.PmFactory

class MainActivity : PmActivity<MainPm>(R.layout.activity_main) {

    private val pmFactory = object : PmFactory {
        override fun createPm(description: Saveable): PresentationModel {
            return when (description) {
                is SamplesPm.Description -> SamplesPm()
                is CounterPm.Description -> CounterPm(description.maxCount)
                is MultistackPm.Description -> MultistackPm(this)
                is ItemPm.Description -> ItemPm(description.screenTitle, description.tabTitle)
                else -> throw IllegalStateException("Not handled instance creation for pm description $description")
            }
        }
    }

    override fun providePresentationModel(): MainPm {
        return MainPm(pmFactory)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            mainScreen(getPresentationModel())
        }
    }

    @Composable
    fun mainScreen(pm: MainPm) {
        navigation(pm.router.pmStackChanges.bind()) { pm ->
            when (pm) {
                is SamplesPm -> samplesScreen(pm)
                is CounterPm -> counterScreen(pm)
                is MultistackPm -> multistackScreen(pm)
                else -> {
                }
            }
        }
    }

    @Composable
    fun samplesScreen(pm: SamplesPm) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { pm.counterSampleClick() }) {
                Text("Counter Sample")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { pm.multistackSampleClick() }) {
                Text("Multistack Sample")
            }
        }
    }

    override fun provideJson(): Json {
        return Json {
            serializersModule = SerializersModule {
                polymorphic(
                    Saveable::class,
                    SamplesPm.Description::class,
                    SamplesPm.Description.serializer()
                )
                polymorphic(
                    Saveable::class,
                    CounterPm.Description::class,
                    CounterPm.Description.serializer()
                )
                polymorphic(
                    Saveable::class,
                    MultistackPm.Description::class,
                    MultistackPm.Description.serializer()
                )
                polymorphic(
                    Saveable::class,
                    ItemPm.Description::class,
                    ItemPm.Description.serializer()
                )
            }
        }
    }
}