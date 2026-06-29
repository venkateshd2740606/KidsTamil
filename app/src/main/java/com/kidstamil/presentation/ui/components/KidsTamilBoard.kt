package com.kidstamil.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidstamil.R
import com.kidstamil.domain.model.KidsTamilGame
import com.kidstamil.domain.model.LearningStepMode

private val cardColors = listOf(
    Color(0xFFFF6B6B), Color(0xFF4ECDC4), Color(0xFFFFE66D),
    Color(0xFF95E1D3), Color(0xFFA29BFE), Color(0xFFFF9FF3)
)

@Composable
fun KidsTamilBoard(
    game: KidsTamilGame,
    reducedMotion: Boolean,
    onNextStep: () -> Unit,
    onQuizAnswer: (Int) -> Unit,
    onTracePoint: (Float, Float) -> Unit,
    onCompleteTrace: () -> Unit,
    modifier: Modifier = Modifier
) {
    val step = game.currentStep ?: return
    val progress = (game.currentStepIndex + 1f) / game.level.stepCount
    val cardColor = cardColors[game.level.letterIndex % cardColors.size]
    val boardDescription = stringResource(R.string.color_sort)

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())
            .semantics { contentDescription = boardDescription },
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = cardColor,
        )
        Text(
            text = stringResource(R.string.step_progress, game.currentStepIndex + 1, game.level.stepCount),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        when (step) {
            LearningStepMode.LEARN -> LearnStep(game, cardColor)
            LearningStepMode.TRACE -> TraceStep(game, cardColor, onTracePoint, onCompleteTrace)
            LearningStepMode.QUIZ -> QuizStep(game, cardColor, onQuizAnswer)
        }
        if (step != LearningStepMode.QUIZ && (step != LearningStepMode.TRACE || game.traceCompleted)) {
            Button(
                onClick = onNextStep,
                enabled = when (step) {
                    LearningStepMode.LEARN -> true
                    LearningStepMode.TRACE -> game.traceCompleted
                    else -> false
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = cardColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(stringResource(R.string.tap_next), fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun LearnStep(game: KidsTamilGame, cardColor: Color) {
    val entry = game.level.entry
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(entry.letter, fontSize = 120.sp, fontWeight = FontWeight.Bold, color = cardColor)
            Text(entry.emoji, fontSize = 64.sp)
            Text(entry.exampleWord, fontSize = 32.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            Text(entry.romanization, fontSize = 24.sp, textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Text(entry.meaning, fontSize = 28.sp, textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun TraceStep(
    game: KidsTamilGame, cardColor: Color,
    onTracePoint: (Float, Float) -> Unit, onCompleteTrace: () -> Unit
) {
    val letter = game.level.entry.letter
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.trace_mode_title), fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Box(
            modifier = Modifier.fillMaxWidth().height(280.dp).clip(RoundedCornerShape(24.dp))
                .background(Color.White).border(3.dp, cardColor, RoundedCornerShape(24.dp))
                .pointerInput(game.currentStepIndex) {
                    detectDragGestures(
                        onDragStart = { offset -> onTracePoint(offset.x / size.width, offset.y / size.height) },
                        onDrag = { change, _ ->
                            change.consume()
                            onTracePoint(change.position.x / size.width, change.position.y / size.height)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(letter, fontSize = 160.sp, fontWeight = FontWeight.Bold, color = Color.LightGray.copy(alpha = 0.5f))
            Canvas(Modifier.fillMaxSize()) {
                if (game.tracePoints.size >= 2) {
                    val strokePath = Path()
                    game.tracePoints.forEachIndexed { index, point ->
                        val offset = Offset(point.x * size.width, point.y * size.height)
                        if (index == 0) strokePath.moveTo(offset.x, offset.y) else strokePath.lineTo(offset.x, offset.y)
                    }
                    drawPath(strokePath, color = cardColor, style = Stroke(width = 12f, cap = StrokeCap.Round))
                }
            }
        }
        if (!game.traceCompleted) {
            Button(onClick = onCompleteTrace,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(16.dp)) {
                Text(stringResource(R.string.trace_done), fontSize = 18.sp)
            }
        } else {
            Text(stringResource(R.string.trace_complete), color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

@Composable
private fun QuizStep(game: KidsTamilGame, cardColor: Color, onQuizAnswer: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.quiz_mode_title), fontSize = 24.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
            color = cardColor.copy(alpha = 0.2f)) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(game.level.entry.emoji, fontSize = 48.sp)
                Text(game.level.quizPrompt, fontSize = 22.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
            }
        }
        game.level.quizOptions.forEachIndexed { index, option ->
            val eliminated = index in game.eliminatedQuizOptions
            val wasAnswered = game.quizAnswered || game.awaitingAdvance
            val selected = game.quizSelectedIndex == index
            val isCorrect = index == game.level.quizCorrectIndex
            val showResult = game.awaitingAdvance && selected
            val bg = when {
                showResult && isCorrect -> MaterialTheme.colorScheme.primaryContainer
                showResult && !isCorrect -> MaterialTheme.colorScheme.errorContainer
                eliminated -> MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                else -> cardColors[index % cardColors.size].copy(alpha = 0.35f)
            }
            Box(
                modifier = Modifier.fillMaxWidth().height(64.dp).clip(RoundedCornerShape(16.dp)).background(bg)
                    .border(2.dp, cardColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .clickable(enabled = !eliminated && !wasAnswered) { onQuizAnswer(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (eliminated) "— $option" else option,
                    fontSize = 36.sp, fontWeight = FontWeight.Bold,
                    color = if (eliminated) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun GameStatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Text(text = "$label: $value", style = MaterialTheme.typography.labelLarge, modifier = modifier)
}
