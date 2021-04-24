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

package me.dmdev.premo

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.Transient
import me.dmdev.premo.navigation.NavigationMessage
import me.dmdev.premo.navigation.PmFactory
import me.dmdev.premo.navigation.PmRouter

abstract class PresentationModel(private val args: Args) {

    abstract class Args: Saveable {
        @Transient
        internal var tag: String = ""

        @Transient
        internal var parent: PresentationModel? = null

        @Transient
        internal var state: PmState? = null

        @Transient
        internal lateinit var factory: PmFactory
    }

    private val pmState: PmState? = args.state
    private val pmFactory: PmFactory = args.factory
    val tag: String = args.tag
    val parentPm: PresentationModel? = args.parent

    val pmScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    var pmInForegroundScope: CoroutineScope? = null
        private set

    private var routerOrNull: PmRouter? = null
    private val saveableStates = mutableMapOf<String, SaveableState<*, *>>()
    private val children = mutableMapOf<String, PresentationModel>()
    val lifecycle: PmLifecycle = PmLifecycle()

    init {

        lifecycle.stateFlow.onEach { state ->
            children.forEach { entry ->
                entry.value.lifecycle.moveTo(state)
            }
        }.launchIn(pmScope)

        lifecycle.eventFlow.onEach { event ->
            when (event) {
                PmLifecycle.Event.ON_CREATE -> {
                    onCreate()
                }
                PmLifecycle.Event.ON_FOREGROUND -> {
                    pmInForegroundScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
                    onForeground()
                }
                PmLifecycle.Event.ON_BACKGROUND -> {
                    onBackground()
                    pmInForegroundScope?.cancel()
                }
                PmLifecycle.Event.ON_DESTROY -> {
                    onDestroy()
                    pmScope.cancel()
                }
            }
        }.launchIn(pmScope)
    }

    @Suppress("FunctionName")
    protected fun Router(initialArgs: Args): PmRouter {

        val restoredPmBackStack = pmState?.routerState?.map { pmState ->

            val args = pmState.args
            args.tag = pmState.tag
            args.parent = this
            args.state = pmState
            args.factory = pmFactory

            pmFactory.createPm(args)
        }

        return routerOrNull ?: PmRouter(
            hostPm = this,
        ).also { router ->

            if (restoredPmBackStack != null) {
                router.setBackStack(restoredPmBackStack)
            }

            if (router.pmStack.value.isEmpty()) {
                router.push(Child(initialArgs))
            }

            routerOrNull = router
        }
    }

    @Suppress("UNCHECKED_CAST", "FunctionName")
    fun <PM : PresentationModel> Child(
        args: Args,
        tag: String = randomUUID()
    ): PM {

        args.tag = tag
        args.parent = this
        args.state = pmState?.childrenStates?.get(tag)
        args.factory = pmFactory

        return pmFactory.createPm(args) as PM
    }

    @Suppress("FunctionName")
    fun <PM : PresentationModel> AttachedChild(
        args: Args,
        tag: String
    ): PM {

        val pm = Child<PM>(args, tag)
        pm.lifecycle.moveTo(lifecycle.state)
        children[tag] = pm
        return pm
    }

    @Suppress("UNCHECKED_CAST", "FunctionName")
    inline fun <reified T> SaveableState(
        initialValue: T,
        key: String
    ): State<T> {
        val saver = when {
            T::class == Boolean::class -> BooleanSaver
            T::class == Byte::class -> ByteSaver
            T::class == Short::class -> ShortSaver
            T::class == Int::class -> IntSaver
            T::class == Long::class -> LongSaver
            T::class == Float::class -> FloatSaver
            T::class == Double::class -> DoubleSaver
            T::class == Char::class -> CharSaver
            T::class == String::class -> StringSaver
            else -> SaveableSaver
        }

        return SaveableState(initialValue, key, saver as Saver<T, Saveable>)
    }

    @Suppress("UNCHECKED_CAST", "FunctionName")
    fun <T, S : Saveable> SaveableState(
        initialValue: T,
        key: String,
        saver: Saver<T, S>
    ): State<T> {
        val state: State<T> = if (pmState != null && pmState.states.containsKey(key)) {
            State(saver.restore(pmState.states[key] as S))
        } else {
            State(initialValue)
        }
        saveableStates[key] = SaveableState(state, saver)
        return state
    }

    private class SaveableState<T, S : Saveable>(
        val state: State<T>,
        val saver: Saver<T, S>
    ) {
        @Suppress("UNCHECKED_CAST")
        val saveableValue: Saveable
            get() = saver.save(state.value)
    }

    var <T> State<T>.value: T
        get() = mutableStateFlow.value
        set(value) {
            mutableStateFlow.value = value
        }

    @Suppress("MemberVisibilityCanBePrivate")
    protected open fun onCreate() {
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected open fun onForeground() {
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected open fun onBackground() {
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected open fun onDestroy() {
    }

    protected open fun handleNavigationMessage(message: NavigationMessage) {
        parentPm?.handleNavigationMessage(message)
    }

    open fun back() {
        val router = routerOrNull
        if (router != null && router.pmStack.value.size > 1) {
            router.pop()
        } else {
            parentPm?.back()
        }
    }

    open fun handleBack(): Boolean {
        val router = routerOrNull
        if (router != null) {
            val handledByNestedPm =
                router.pmStack.value.lastOrNull()?.handleBack() ?: false
            if (handledByNestedPm.not()) {
                if (router.pmStack.value.size > 1) {
                    router.pop()
                    return true
                }
            } else {
                return true
            }
            return false
        } else {
            return false
        }
    }

    internal fun saveState(): PmState {

        val router = routerOrNull
        val routerState = router?.pmStack?.value?.map { pm -> pm.saveState() } ?: listOf()

        return PmState(
            tag = tag,
            routerState = routerState,
            childrenStates = children.mapValues { entry -> entry.value.saveState() },
            states = saveableStates.mapValues { entry -> entry.value.saveableValue },
            args = args
        )
    }
}