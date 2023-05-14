package me.dmdev.premo.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier

@Composable
fun FullScreenContainer(
    content: @Composable () -> Unit
) {
    val dialogComposableReferenceState = mutableStateOf<DialogComposableReference>(null)

    CompositionLocalProvider(
        LocalDialogState provides dialogComposableReferenceState
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            content()

            if (dialogComposableReferenceState.value != null) {
                dialogComposableReferenceState.value?.invoke()
            }
        }
    }
}
