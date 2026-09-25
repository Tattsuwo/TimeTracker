package com.example.timetracker.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Nuance atténuée d'une couleur de catégorie, obtenue en la superposant à
 * faible opacité sur la couleur de fond actuelle du thème (compositeOver).
 * Cette approche s'adapte automatiquement au thème clair ou sombre : dans les
 * deux cas, on obtient un ton doux et cohérent avec le reste de l'écran,
 * plutôt qu'un mélange fixe avec du blanc qui rendrait mal en thème sombre.
 */
fun Color.attenuated(background: Color, alpha: Float = 0.10f): Color =
    this.copy(alpha = alpha).compositeOver(background)

/**
 * Palette fixe (plutôt qu'un sélecteur de couleur libre façon roue HSV) :
 * dix teintes choisies pour rester bien distinguables entre elles, simple à
 * afficher et à choisir au toucher, sans dépendance externe.
 */
val CATEGORY_COLOR_PALETTE: List<Color> = listOf(
    Color(0xFFE53935), // rouge
    Color(0xFFFB8C00), // orange
    Color(0xFFFFB300), // ambre
    Color(0xFF43A047), // vert
    Color(0xFF00897B), // teal
    Color(0xFF1E88E5), // bleu
    Color(0xFF3949AB), // indigo
    Color(0xFF8E24AA), // violet
    Color(0xFFD81B60), // rose
    Color(0xFF6D6D6D)  // gris (couleur par défaut)
)

/** Petit rond coloré utilisé pour représenter une catégorie dans les listes. */
@Composable
fun CategoryColorDot(color: Color, modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .size(14.dp)
            .clip(CircleShape)
            .background(color)
    )
}

/**
 * Rangée de ronds cliquables pour choisir une couleur dans la palette fixe.
 * Le rond actuellement sélectionné est entouré d'un anneau pour rester
 * visible même quand sa couleur est proche du fond.
 */
@Composable
fun ColorPickerRow(selectedColor: Color, onColorSelected: (Color) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        CATEGORY_COLOR_PALETTE.forEachIndexed { index, color ->
            val isSelected = color == selectedColor
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .padding(end = if (index == CATEGORY_COLOR_PALETTE.lastIndex) 0.dp else 10.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (isSelected) 3.dp else 0.dp,
                        color = MaterialTheme.colorScheme.onSurface,
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(color) }
                    .semantics { contentDescription = "Couleur ${index + 1}" }
            )
        }
    }
}
