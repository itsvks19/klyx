package com.klyx.ui.component

import androidx.compose.runtime.Composable
import com.klyx.BuildConfig

@Composable
fun AboutDialog(onDismissRequest: () -> Unit) {
    KlyxDialog(
        onDismissRequest = onDismissRequest,
        title = "Info",
        message = "Klyx Dev ${BuildConfig.VERSION_NAME}"
    )
}
