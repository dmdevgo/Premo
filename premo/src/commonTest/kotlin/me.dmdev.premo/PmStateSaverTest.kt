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

package me.dmdev.premo

import kotlin.reflect.KType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer

class PmStateSaverTest {

    @Test
    fun testPmStateRestoring() {
        val delegate = createDelegate(state = mapOf(), tag = "Root")
        val rootPm = delegate.presentationModel
        delegate.onForeground()
        rootPm.children.forEach { containerPm ->
            containerPm.children.forEachIndexed { index, childPm ->
                childPm.setNumber(index)
            }
        }

        val savedState = delegate.savePm()

        val delegateForRestoredPm = createDelegate(tag = "Restored Root", state = savedState)
        val restoredPm = delegateForRestoredPm.presentationModel
        delegateForRestoredPm.onForeground()

        assertEquals(rootPm, restoredPm)
    }

    private fun createDelegate(
        tag: String,
        state: Map<String, String>,
    ): PmDelegate<RootPm> {
        return PmDelegate(
            pmParams = PmParams(
                tag = tag,
                parent = null,
                description = RootPm.Description,
                state = state,
                factory = MainPmFactory(),
                stateSaver = JsonPmStateSaver()
            )
        )
    }
}

private class RootPm(params: PmParams): PresentationModel(params) {

    @Serializable
    object Description: PmDescription

    val keys = listOf("container1", "container2", "container3", "container4", "container5")

    val children: List<ContainerPm> = keys.map { key ->
        Child(
            description = ContainerPm.Description,
            tag = key
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as RootPm

        if (keys != other.keys) return false
        if (children != other.children) return false

        return true
    }

    override fun hashCode(): Int {
        var result = keys.hashCode()
        result = 31 * result + children.hashCode()
        return result
    }
}

private class ContainerPm(params: PmParams): PresentationModel(params) {

    @Serializable
    object Description : PmDescription

    val keys = listOf("child1", "child2", "child3", "child4", "child5")

    val children: List<ChildPm> = keys.map { key ->
        Child(
            description = ChildPm.Description(key),
            tag = key
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ContainerPm

        if (keys != other.keys) return false
        if (children != other.children) return false

        return true
    }

    override fun hashCode(): Int {
        var result = keys.hashCode()
        result = 31 * result + children.hashCode()
        return result
    }


}

private class ChildPm(params: PmParams): PresentationModel(params) {

    private val state = SaveableFlow<State?>("state",null)

    @Serializable
    class Description(val key: String): PmDescription

    fun setNumber(number: Int) {
        state.value = State(
            int =  number,
            long = number.toLong(),
            float = number.toFloat(),
            double = number.toDouble(),
            string = number.toString()
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ChildPm

        if (state.value != other.state.value) return false

        return true
    }

    override fun hashCode(): Int {
        return state.value.hashCode()
    }

    @Serializable
    data class State(
        val int: Int,
        val long: Long,
        val float: Float,
        val double: Double,
        val string: String
    )
}

private class MainPmFactory : PmFactory {
    override fun createPm(params: PmParams): PresentationModel {
        return when (val description = params.description) {
            is RootPm.Description -> RootPm(params)
            is ContainerPm.Description -> ContainerPm(params)
            is ChildPm.Description -> ChildPm(params)
            else -> throw IllegalArgumentException("Not handled instance creation for pm description $description")
        }
    }
}

private class JsonPmStateSaver : PmStateSaver {

    private val json = Json {
        serializersModule = SerializersModule {
            polymorphic(
                PmDescription::class,
                RootPm.Description::class,
                RootPm.Description.serializer()
            )
            polymorphic(
                PmDescription::class,
                ContainerPm.Description::class,
                ContainerPm.Description.serializer()
            )
            polymorphic(
                PmDescription::class,
                ChildPm.Description::class,
                ChildPm.Description.serializer()
            )
            polymorphic(
                ChildPm.State::class,
                ChildPm.State::class,
                ChildPm.State.serializer()
            )
        }
    }

    override fun <T> saveState(kType: KType, value: T): String {
        return json.encodeToString(serializer(kType), value)
    }

    override fun <T> restoreState(kType: KType, jsonString: String): T {
        @Suppress("UNCHECKED_CAST")
        return json.decodeFromString(serializer(kType) as KSerializer<T>, jsonString)
    }
}