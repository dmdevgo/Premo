/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020-2022 Dmitriy Gorbunov (dmitriy.goto@gmail.com)
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

object Premo {

    const val groupId = "me.dmdev.premo"
    const val version = "1.0.0-alpha.04"
    const val description = "Premo helps you implement the presentation layer and share it on iOS and Android."
    const val url = "https://github.com/dmdevgo/Premo"

    object License {
        const val name = "MIT"
        const val url = "https://github.com/dmdevgo/Premo/blob/master/LICENSE"
    }

    object Scm {
        const val url = "https://github.com/dmdevgo/Premo"
    }

    class Developer(
        val id: String,
        val name: String,
        val email: String,
    )

    val developers = listOf(
        Developer(
            id = "dmdevgo",
            name = "Dmitriy Gorbunov",
            email = "dmitriy.goto@gmail.com"
        )
    )
}