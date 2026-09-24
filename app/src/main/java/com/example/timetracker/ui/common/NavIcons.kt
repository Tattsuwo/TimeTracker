package com.example.timetracker.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun TimerIcon(modifier: Modifier = Modifier) {
    val color = LocalContentColor.current
    Canvas(modifier = modifier.size(24.dp).semantics { contentDescription = "Chrono" }) {
        val stroke = size.minDimension * 0.09f
        val radius = size.minDimension / 2 - stroke
        drawCircle(color = color, radius = radius, style = Stroke(width = stroke))
        drawLine(
            color = color,
            start = center,
            end = Offset(center.x, center.y - radius * 0.7f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun HistoryIcon(modifier: Modifier = Modifier) {
    val color = LocalContentColor.current
    Canvas(modifier = modifier.size(24.dp).semantics { contentDescription = "Historique" }) {
        val stroke = size.minDimension * 0.12f
        val widths = listOf(0.9f, 0.7f, 0.5f)
        widths.forEachIndexed { index, widthFraction ->
            val y = size.height * (0.25f + index * 0.25f)
            drawLine(
                color = color,
                start = Offset(size.width * 0.05f, y),
                end = Offset(size.width * (0.05f + widthFraction), y),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun SummaryIcon(modifier: Modifier = Modifier) {
    val color = LocalContentColor.current
    Canvas(modifier = modifier.size(24.dp).semantics { contentDescription = "Synthèse" }) {
        val stroke = size.minDimension * 0.16f
        val baseline = size.height * 0.85f
        val heights = listOf(0.35f, 0.6f, 0.85f)
        heights.forEachIndexed { index, heightFraction ->
            val x = size.width * (0.2f + index * 0.3f)
            drawLine(
                color = color,
                start = Offset(x, baseline),
                end = Offset(x, baseline - size.height * heightFraction),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun SettingsIcon(modifier: Modifier = Modifier) {
    val color = LocalContentColor.current
    Canvas(modifier = modifier.size(24.dp).semantics { contentDescription = "Réglages" }) {
        val outerStroke = size.minDimension * 0.09f
        drawCircle(color = color, radius = size.minDimension * 0.42f, style = Stroke(width = outerStroke))
        drawCircle(color = color, radius = size.minDimension * 0.14f)
    }
}
