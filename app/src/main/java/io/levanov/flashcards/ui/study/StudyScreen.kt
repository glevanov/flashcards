package io.levanov.flashcards.ui.study

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.levanov.flashcards.R
import io.levanov.flashcards.ui.theme.FlashcardsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    deckName: String?,
    reversed: Boolean,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm: StudyViewModel = viewModel(factory = StudyViewModel.factory(deckName, reversed))
    val state by vm.uiState.collectAsStateWithLifecycle()

    val tts = LocalTtsManager.current

    val title = when {
        state.finished -> "Done"
        deckName != null -> deckName.substringAfterLast('/')
        else -> "All decks"
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onExit) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.queue != null && !state.finished && state.ttsEnabled) {
                        val spokenText = ttsText(state)
                        IconButton(
                            enabled = tts?.available == true && spokenText.isNotEmpty(),
                            onClick = { tts?.speak(spokenText) },
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_volume_up_24),
                                contentDescription = "Play pronunciation",
                            )
                        }
                    }
                },
            )
        }
    ) { innerPadding ->
        when {
            state.queue == null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            state.finished -> SessionSummary(
                state = state,
                onDone = onExit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )

            state.queue.isNullOrEmpty() -> EmptySession(
                onDone = onExit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )

            else -> SessionContent(
                state = state,
                onToggleFlip = vm::toggleFlip,
                onGrade = vm::grade,
                onSkip = vm::skip,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        }
    }
}

@Composable
private fun SessionContent(
    state: StudyUiState,
    onToggleFlip: () -> Unit,
    onGrade: (Boolean) -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val queue = state.queue!!
    val card = queue[state.index]
    val density = LocalDensity.current
    val thresholdPx = with(density) { 120.dp.toPx() }

    var offsetX by remember { mutableFloatStateOf(0f) }
    val draggableState = rememberDraggableState { delta -> offsetX += delta }

    Column(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                "${state.index + 1} / ${queue.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { state.index.toFloat() / queue.size },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            val rotation = remember(card.key) { Animatable(if (state.flipped) 180f else 0f) }
            LaunchedEffect(card.key, state.flipped) {
                val target = if (state.flipped) 180f else 0f
                if (rotation.value != target) {
                    rotation.animateTo(target, tween(durationMillis = 400))
                }
            }
            val showFront = rotation.value <= 90f

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .graphicsLayer {
                        translationX = offsetX
                        rotationY = rotation.value
                        cameraDistance = 12f * density.density
                    }
                    .then(
                        Modifier
                            .clickable { onToggleFlip() }
                            .then(
                                if (state.answerSeen) {
                                    Modifier.draggable(
                                        state = draggableState,
                                        orientation = Orientation.Horizontal,
                                        onDragStopped = {
                                            when {
                                                offsetX > thresholdPx -> {
                                                    onGrade(true)
                                                    offsetX = 0f
                                                }

                                                offsetX < -thresholdPx -> {
                                                    onGrade(false)
                                                    offsetX = 0f
                                                }

                                                else -> offsetX = 0f
                                            }
                                        },
                                    )
                                } else {
                                    Modifier
                                },
                            ),
                    ),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    if (showFront) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp),
                        ) {
                            Text(
                                card.deck,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                if (state.reversed) card.english else card.swedish,
                                style = MaterialTheme.typography.headlineLarge,
                                textAlign = TextAlign.Center,
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .graphicsLayer { rotationY = 180f },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                if (state.reversed) card.swedish else card.english,
                                style = MaterialTheme.typography.headlineMedium,
                                textAlign = TextAlign.Center,
                            )
                            if (card.example.isNotBlank()) {
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    card.example,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }
        }

        // Grade row — reserve a constant height so the card above stays put
        // when the buttons appear/disappear instead of shifting up/down.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(124.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            if (state.answerSeen) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(
                            onClick = { onGrade(false) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            ),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Didn't know", style = MaterialTheme.typography.labelLarge)
                        }
                        Button(
                            onClick = { onGrade(true) },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Knew it", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    TextButton(
                        onClick = onSkip,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                    ) {
                        Text("Skip")
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionSummary(
    state: StudyUiState,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accuracy = if (state.gradedCount == 0) 0 else state.correctCount * 100 / state.gradedCount
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Session complete", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Text(
            "${state.correctCount} / ${state.gradedCount} correct ($accuracy%)",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = onDone) {
            Text("Done")
        }
    }
}

@Composable
private fun EmptySession(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Nothing due \uD83C\uDF89", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))
        Button(onClick = onDone) {
            Text("Done")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StudyScreenFrontPreview() {
    FlashcardsTheme {
        SessionContent(
            state = StudyUiState(
                queue = listOf(
                    SessionCard(
                        key = "rivstart/kapitel-01::en fritid",
                        deck = "rivstart/kapitel-01",
                        swedish = "en fritid",
                        english = "free time",
                        example = "På min fritid spelar jag fotboll.",
                    ),
                ),
                index = 0,
            ),
            onToggleFlip = {},
            onGrade = {},
            onSkip = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StudyScreenBackPreview() {
    FlashcardsTheme {
        SessionContent(
            state = StudyUiState(
                queue = listOf(
                    SessionCard(
                        key = "rivstart/kapitel-01::en fritid",
                        deck = "rivstart/kapitel-01",
                        swedish = "en fritid",
                        english = "free time",
                        example = "På min fritid spelar jag fotboll.",
                    ),
                ),
                index = 0,
                flipped = true,
                answerSeen = true,
            ),
            onToggleFlip = {},
            onGrade = {},
            onSkip = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StudyScreenReversedPreview() {
    FlashcardsTheme {
        SessionContent(
            state = StudyUiState(
                queue = listOf(
                    SessionCard(
                        key = "rivstart/kapitel-01::en fritid",
                        deck = "rivstart/kapitel-01",
                        swedish = "en fritid",
                        english = "free time",
                        example = "På min fritid spelar jag fotboll.",
                    ),
                ),
                index = 0,
                reversed = true,
            ),
            onToggleFlip = {},
            onGrade = {},
            onSkip = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StudyScreenFlippedBackPreview() {
    FlashcardsTheme {
        SessionContent(
            state = StudyUiState(
                queue = listOf(
                    SessionCard(
                        key = "rivstart/kapitel-01::en fritid",
                        deck = "rivstart/kapitel-01",
                        swedish = "en fritid",
                        english = "free time",
                        example = "På min fritid spelar jag fotboll.",
                    ),
                ),
                index = 0,
                flipped = false,
                answerSeen = true,
            ),
            onToggleFlip = {},
            onGrade = {},
            onSkip = {},
        )
    }
}

private fun ttsText(state: StudyUiState): String {
    val card = state.queue?.getOrNull(state.index) ?: return ""
    if (!swedishVisible(state)) return ""
    return when {
        state.flipped && card.example.isNotBlank() -> card.example
        else -> card.swedish
    }
}

private fun swedishVisible(state: StudyUiState): Boolean = !state.reversed || state.flipped
