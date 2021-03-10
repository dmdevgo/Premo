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
import me.dmdev.premo.Saveable
import me.dmdev.premo.invoke

class MainActivity : PmActivity<MainPm>(R.layout.activity_main) {

    override fun providePresentationModel(): MainPm {
        return MainPm()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            mainScreen(getPresentationModel())
        }
    }

    @Composable
    fun mainScreen(pm: MainPm) {
        when (val screenPm = pm.router.pmOnTop.bind()) {
            is CounterPm -> counterScreen(screenPm)
            is SamplesPm -> samplesScreen(screenPm)
            else -> {
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
            Button(onClick = { pm.counterSampleClicks() }) {
                Text("Counter Sample")
            }
        }
    }

    @Composable
    fun counterScreen(pm: CounterPm) {

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { pm.minus.invoke() },
                enabled = pm.minusButtonEnabled.bind()
            ) {
                Text(" - ")
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("Count: ${pm.count.bind()}")
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = { pm.plus.invoke() },
                enabled = pm.plusButtonEnabled.bind()
            ) {
                Text(" + ")
            }
        }
    }

    override fun provideJson(): Json {
        return Json {
            serializersModule = SerializersModule {
                polymorphic(Saveable::class, SamplesPm.Description::class, SamplesPm.Description.serializer())
                polymorphic(Saveable::class, CounterPm.Description::class, CounterPm.Description.serializer())
            }
        }
    }
}