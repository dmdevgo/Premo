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

package me.dmdev.premo

class PmMessageHandler internal constructor(
    private val hostPm: PresentationModel
) {

    private val handlers = mutableListOf<(message: PmMessage) -> Boolean>()

    fun addHandler(handler: (message: PmMessage) -> Boolean) {
        handlers.add(handler)
    }

    fun removeHandler(handler: (message: PmMessage) -> Boolean) {
        handlers.remove(handler)
    }

    fun handle(message: PmMessage): Boolean {
        if (hostPm.lifecycle.isDestroyed) return false

        return handlers.any {
            it.invoke(message)
        }
    }

    /**
     * Sends a message towards the root. Any parent can intercept the message and process it.
     */
    fun send(message: PmMessage): Boolean {
        if (hostPm.lifecycle.isDestroyed) return false

        message.sender = hostPm.tag

        var pm: PresentationModel? = hostPm
        while (pm != null) {
            if (pm.messageHandler.handle(message)) return true

            pm = pm.parent
        }
        return false
    }

    /**
     * Sends a message to the target presentation model with related tag.
     */
    fun sendToTarget(message: PmMessage, tag: String): Boolean {
        if (hostPm.lifecycle.isDestroyed) return false

        message.sender = hostPm.tag

        fun PresentationModel.findTarget(tag: String): PresentationModel? {
            if (this.tag == tag) return this
            allChildren.forEach { pm ->
                val targetPm = pm.findTarget(tag)
                if (targetPm != null) return targetPm
            }
            return null
        }

        return findRootPm()
            .findTarget(tag)
            ?.messageHandler
            ?.handle(message)
            ?: false
    }

    /**
     * Sends a message to its children.
     * First, the message is delivered to the active leaves, if the message is not processed, it can be intercepted by the parent.
     * If the message is not processed by any child, the message will be intercepted by the sender.
     */
    fun sendToChildren(message: PmMessage): Boolean {
        if (hostPm.lifecycle.isDestroyed) return false

        message.sender = hostPm.tag
        hostPm.allChildren.reversed().forEach { pm ->
            if (pm.messageHandler.sendToChildren(message)) {
                return true
            }
        }
        if (hostPm.lifecycle.state == PmLifecycle.State.IN_FOREGROUND) {
            return hostPm.messageHandler.handle(message)
        }
        return false
    }

    internal fun findRootPm(): PresentationModel {
        var root: PresentationModel = hostPm
        while (true) {
            val parent = root.parent
            if (parent != null) {
                root = parent
            } else {
                break
            }
        }
        return root
    }

    internal fun release() {
        handlers.clear()
    }
}

inline fun <reified M : PmMessage> PmMessageHandler.onMessage(
    noinline handler: (message: M) -> Unit
) {
    addHandler {
        if (it is M) {
            handler(it)
            true
        } else {
            false
        }
    }
}

inline fun <reified M : PmMessage> PmMessageHandler.handle(
    noinline handler: (message: M) -> Boolean
) {
    addHandler {
        if (it is M) {
            handler(it)
        } else {
            false
        }
    }
}
