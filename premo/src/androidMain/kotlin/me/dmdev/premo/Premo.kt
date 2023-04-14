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

package me.dmdev.premo

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle

object Premo {

    fun init(application: Application) {
        application.registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                if (activity is PmActivity<*>) {
                    activity.delegate.onCreate(savedInstanceState)
                }
            }

            override fun onActivityStarted(activity: Activity) {
                if (activity is PmActivity<*>) {
                    activity.delegate.onStart()
                }
            }

            override fun onActivityResumed(activity: Activity) {
                if (activity is PmActivity<*>) {
                    activity.delegate.onResume()
                }
            }

            override fun onActivityPaused(activity: Activity) {
                if (activity is PmActivity<*>) {
                    activity.delegate.onPause()
                }
            }

            override fun onActivityStopped(activity: Activity) {
                if (activity is PmActivity<*>) {
                    activity.delegate.onStop()
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                if (activity is PmActivity<*>) {
                    activity.delegate.onSaveInstanceState(outState)
                }
            }

            override fun onActivityDestroyed(activity: Activity) {
                if (activity is PmActivity<*>) {
                    activity.delegate.onDestroy()
                }
            }
        })
    }
}
