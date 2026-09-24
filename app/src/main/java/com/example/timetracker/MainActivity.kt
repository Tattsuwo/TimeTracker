package com.example.timetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.timetracker.ui.navigation.TimeTrackerApp
import com.example.timetracker.ui.theme.TimeTrackerTheme

/**
 * Une seule Activity pour toute l'application : la navigation entre écrans
 * (Chrono, Historique, Synthèse, Réglages) se fait entièrement en Compose via
 * TimeTrackerApp/NavHost, pas avec plusieurs Activities. C'est l'approche
 * standard recommandée pour une appli 100% Compose.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TimeTrackerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TimeTrackerApp()
                }
            }
        }
    }
}
