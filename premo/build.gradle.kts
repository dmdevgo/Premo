/*
 * MIT License
 *
 * Copyright (c) 2020 Dmitriy Gorbunov
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

plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {

    android()
    ios {
        binaries.framework {
            freeCompilerArgs += "-Xobjc-generics"
        }
    }

    sourceSets {

        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlin:kotlin-stdlib-common")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.3.6")
            }
        }

        val androidMain by getting {
            dependsOn(commonMain)
            dependencies {
                api("org.jetbrains.kotlin:kotlin-stdlib")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.6")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.6")
                compileOnly("androidx.appcompat:appcompat:1.3.0-alpha01")
                compileOnly("com.google.android.material:material:1.3.0-alpha01")
            }
        }

        val iosMain by getting {
            dependsOn(commonMain)
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:1.3.6")
            }
        }
    }
}

android {

    compileSdkVersion(29)

    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(29)
    }
}