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

/**
 * Petites icônes dessinées à la main avec Canvas plutôt qu'importées depuis
 * androidx.compose.material:material-icons-extended : cette bibliothèque
 * d'icônes a un cycle de publication indépendant des autres modules Compose
 * (contrairement à ce qu'on pourrait supposer), ce qui la rend plus fragile
 * à référencer par une version précise. Dessiner ces 4 pictogrammes très
 * simples évite complètement ce risque de version, au prix d'un rendu plus
 * basique qu'un vrai jeu d'icônes Material.
 *
 * Chacune lit LocalContentColor.current (comme le fait Icon() en interne) :
 * la couleur s'adapte donc automatiquement à l'état sélectionné/non
 * sélectionné géré par NavigationBarItem, sans code supplémentaire.
 */

@Composable
fun TimerIcon(modifier: Modifier = Modifier) {
    val color = LocalContentColor.current
    Canvas(modifier = modifier.size(24.dp).semantics { contentDescription = "Chrono" }) {
        val stroke = size.minDimension * 0.09f
        val radius = size.minDimension / 2 - stroke
        drawCircle(color = color, radius = radius, style = Stroke(width = stroke))
        // Aiguille du chrono, du centre vers le haut.
        drawLine(
            color = color,
            start = center,
            end = Offset(center.x, center.y - radius * 0.7f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}
// Cercle + aiguille : symbolise le chronomètre.

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
// Trois lignes de longueur décroissante : symbolise une liste/historique.

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
// Trois barres de hauteur croissante : symbolise un graphique de synthèse.

@Composable
fun SettingsIcon(modifier: Modifier = Modifier) {
    val color = LocalContentColor.current
    Canvas(modifier = modifier.size(24.dp).semantics { contentDescription = "Réglages" }) {
        val outerStroke = size.minDimension * 0.09f
        drawCircle(color = color, radius = size.minDimension * 0.42f, style = Stroke(width = outerStroke))
        drawCircle(color = color, radius = size.minDimension * 0.14f)
    }
}
// Anneau + point central : symbolise un réglage/molette.
