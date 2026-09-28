package com.example.util

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

/**
 * CompositionLocal providing global scroll delta events dispatched from
 * mouse wheel actions (MotionEvent.ACTION_SCROLL) or keyboard navigation keys
 * at the Activity level or emulator bridge.
 */
val LocalEmulatorScrollEvents = staticCompositionLocalOf<SharedFlow<Float>?> { null }

/**
 * Enables smooth mouse wheel and keyboard scrolling for standard [ScrollState] containers.
 * - Listens to Compose-level mouse wheel pointer events ([PointerEventType.Scroll])
 * - Listens to system-level mouse wheel and DPAD/Page/Space key events via [LocalEmulatorScrollEvents]
 * - Listens to local Compose key events (Down/Up arrows, PageDown/PageUp, Space)
 */
fun Modifier.emulatorScrollable(scrollState: ScrollState): Modifier = composed {
    val coroutineScope = rememberCoroutineScope()
    val scrollEvents = LocalEmulatorScrollEvents.current

    // Collect global events from Activity (captures all mouse wheel and keyboard events in emulator)
    LaunchedEffect(scrollEvents) {
        scrollEvents?.collect { delta ->
            try {
                scrollState.scrollBy(delta)
            } catch (_: Exception) {}
        }
    }

    this
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    if (event.type == PointerEventType.Scroll) {
                        val change = event.changes.firstOrNull()
                        val deltaY = change?.scrollDelta?.y ?: 0f
                        if (deltaY != 0f) {
                            coroutineScope.launch {
                                try {
                                    scrollState.scrollBy(deltaY * 72f)
                                } catch (_: Exception) {}
                            }
                            change?.consume()
                        }
                    }
                }
            }
        }
        .onKeyEvent { keyEvent ->
            if (keyEvent.type == KeyEventType.KeyDown) {
                when (keyEvent.key) {
                    Key.DirectionDown -> {
                        coroutineScope.launch { scrollState.scrollBy(150f) }
                        true
                    }
                    Key.DirectionUp -> {
                        coroutineScope.launch { scrollState.scrollBy(-150f) }
                        true
                    }
                    Key.PageDown, Key.Spacebar -> {
                        coroutineScope.launch { scrollState.scrollBy(500f) }
                        true
                    }
                    Key.PageUp -> {
                        coroutineScope.launch { scrollState.scrollBy(-500f) }
                        true
                    }
                    else -> false
                }
            } else false
        }
}

/**
 * Enables smooth mouse wheel and keyboard scrolling for [LazyListState] containers.
 */
fun Modifier.emulatorScrollable(lazyListState: LazyListState): Modifier = composed {
    val coroutineScope = rememberCoroutineScope()
    val scrollEvents = LocalEmulatorScrollEvents.current

    LaunchedEffect(scrollEvents) {
        scrollEvents?.collect { delta ->
            try {
                lazyListState.scrollBy(delta)
            } catch (_: Exception) {}
        }
    }

    this
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    if (event.type == PointerEventType.Scroll) {
                        val change = event.changes.firstOrNull()
                        val deltaY = change?.scrollDelta?.y ?: 0f
                        if (deltaY != 0f) {
                            coroutineScope.launch {
                                try {
                                    lazyListState.scrollBy(deltaY * 72f)
                                } catch (_: Exception) {}
                            }
                            change?.consume()
                        }
                    }
                }
            }
        }
        .onKeyEvent { keyEvent ->
            if (keyEvent.type == KeyEventType.KeyDown) {
                when (keyEvent.key) {
                    Key.DirectionDown -> {
                        coroutineScope.launch { lazyListState.scrollBy(150f) }
                        true
                    }
                    Key.DirectionUp -> {
                        coroutineScope.launch { lazyListState.scrollBy(-150f) }
                        true
                    }
                    Key.PageDown, Key.Spacebar -> {
                        coroutineScope.launch { lazyListState.scrollBy(500f) }
                        true
                    }
                    Key.PageUp -> {
                        coroutineScope.launch { lazyListState.scrollBy(-500f) }
                        true
                    }
                    else -> false
                }
            } else false
        }
}
