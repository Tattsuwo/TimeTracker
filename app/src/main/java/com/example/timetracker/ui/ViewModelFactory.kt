package com.example.timetracker.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.timetracker.TimeTrackerApplication
import com.example.timetracker.ui.history.HistoryViewModel
import com.example.timetracker.ui.settings.SettingsViewModel
import com.example.timetracker.ui.summary.SummaryViewModel
import com.example.timetracker.ui.timer.TimerViewModel

/**
 * Sans Hilt ni autre framework de DI (le projet reste volontairement petit),
 * on construit la ViewModelProvider.Factory "à la main" avec le DSL
 * viewModelFactory { initializer { ... } } fourni par
 * androidx.lifecycle.viewmodel : chaque ViewModel reçoit directement le
 * repository unique exposé par TimeTrackerApplication.
 */
@Composable
fun rememberTimeTrackerViewModelFactory(): ViewModelProvider.Factory {
    val application = LocalContext.current.applicationContext as TimeTrackerApplication
    return remember(application) {
        viewModelFactory {
            initializer { TimerViewModel(application) }
            initializer { HistoryViewModel(application.repository) }
            initializer { SummaryViewModel(application.repository) }
            initializer { SettingsViewModel(application.repository) }
        }
    }
}
