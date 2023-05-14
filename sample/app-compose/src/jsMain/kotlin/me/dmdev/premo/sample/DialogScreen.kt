package me.dmdev.premo.sample

import androidx.compose.runtime.*

@Composable
actual fun DialogScreen(
    title: String,
    message: String,
    okButtonText: String,
    cancelButtonText: String,
    onOkButtonClick: () -> Unit,
    onCancelButtonClick: () -> Unit,
    onDismissRequest: () -> Unit
) {
    CommonDialog(
        title = title,
        message = message,
        okButtonText = okButtonText,
        cancelButtonText = cancelButtonText,
        onOkButtonClick = onOkButtonClick,
        onCancelButtonClick = onCancelButtonClick,
        onDismissRequest = onDismissRequest
    )
}
