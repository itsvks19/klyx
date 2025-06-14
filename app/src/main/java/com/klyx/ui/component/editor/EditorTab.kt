package com.klyx.ui.component.editor

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.klyx.core.Env
import com.klyx.core.file.DocumentFileWrapper
import com.klyx.core.file.FileWrapper
import com.klyx.core.file.KWatchEvent.Kind
import com.klyx.core.file.asWatchChannel
import com.klyx.ui.theme.DefaultKlyxShape
import com.klyx.viewmodel.TabItem
import kotlinx.coroutines.channels.consumeEach

@Composable
fun EditorTabBar(
    tabs: List<TabItem>,
    modifier: Modifier = Modifier,
    selectedTab: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    onClose: (Int) -> Unit = {},
    isDirty: (Int) -> Boolean = { false },
) {
    val dirty by rememberUpdatedState(isDirty)

    Box(
        modifier = modifier
            .zIndex(5f)
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
            .fillMaxWidth()
    ) {
        LazyRow {
            itemsIndexed(tabs) { index, tab ->
                EditorTab(
                    tabItem = tab,
                    isSelected = index == selectedTab,
                    isDirty = dirty(index),
                    onClick = { onTabSelected(index) },
                    onClose = { onClose(index) }
                )
            }
        }
    }
}

@Composable
fun EditorTab(
    tabItem: TabItem,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    isDirty: Boolean = false,
    onClick: () -> Unit = {},
    onClose: () -> Unit = {},
) {
    val recomposeScope = currentRecomposeScope

    LaunchedEffect(tabItem) {
        if (tabItem.data is FileWrapper) {
            if (tabItem.data.exists().not()) return@LaunchedEffect

            tabItem.data.asRawFile()?.let { file ->
                file.asWatchChannel().consumeEach { event ->
                    if (event.kind == Kind.Deleted || event.kind == Kind.Created) {
                        recomposeScope.invalidate()
                    }
                }
            }
        }
    }

    val colorScheme = MaterialTheme.colorScheme

    val surface4 = colorScheme.surfaceColorAtElevation(4.dp)
    val onSurface4 = contentColorFor(surface4)
    val surface1 = colorScheme.surfaceColorAtElevation(1.dp)
    val onSurface1 = contentColorFor(surface1)

    val backgroundColor by animateColorAsState(
        if (isSelected) surface4 else surface1,
        label = "TabBackgroundColor"
    )

    val textColor = if (isSelected) onSurface4 else onSurface1

    Row(
        modifier = modifier
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .then(if (isSelected) Modifier.drawWithCache {
                onDrawBehind {
                    val strokeWidth = 1.dp.toPx()
                    val y = size.height - strokeWidth / 2

                    drawLine(
                        color = colorScheme.onSurface,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = strokeWidth
                    )
                }
            } else Modifier)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isDirty) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        val isTabFileMissing = { (tabItem.data as? FileWrapper)?.exists() == false && tabItem.data.name != "untitled" }

        Text(
            text = tabItem.name,
            color = if (isTabFileMissing()) colorScheme.error else textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall,
            textDecoration = if (isTabFileMissing()) {
                TextDecoration.LineThrough
            } else TextDecoration.None
        )

        if (tabItem.type == "file") {
            val file = tabItem.data as? FileWrapper

            if (file != null && file.path != "untitled") {
                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = file.path.let {
                        var result = if (it.startsWith(Env.APP_HOME_DIR)) {
                            it.replaceFirst(Env.APP_HOME_DIR, "~")
                        } else if (it.startsWith(Env.DEVICE_HOME_DIR)) {
                            it.replaceFirst(Env.DEVICE_HOME_DIR, "~")
                        } else it

                        result = result.substringBeforeLast("/")

                        if (file is DocumentFileWrapper && file.isFromTermux()) {
                            result = "~/termux${result.substringAfterLast("com.termux/files/home")}"
                        }

                        if (result.length > 20) {
                            result = "${result.take(20)}..."
                        }
                        result
                    },
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.alpha(0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close tab",
            tint = textColor,
            modifier = Modifier
                .size(15.dp)
                .clip(DefaultKlyxShape)
                .clickable(onClick = onClose)
                .padding(2.dp)
        )
    }
}

