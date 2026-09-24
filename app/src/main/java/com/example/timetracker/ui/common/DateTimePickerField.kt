package com.example.timetracker.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.timetracker.util.toHourString
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

/**
 * Champ "date + heure" utilisé pour éditer manuellement une heure de
 * début/fin de session. Le sélecteur d'heure est forcé en is24Hour = true
 * pour respecter l'affichage 24h demandé, quels que soient les réglages
 * régionaux de l'appareil.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerField(
    label: String,
    value: Instant,
    onValueChange: (Instant) -> Unit,
    modifier: Modifier = Modifier
) {
    val zone = ZoneId.systemDefault()
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val zonedValue = value.atZone(zone)

    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Row {
            TextButton(onClick = { showDatePicker = true }) {
                Text(dateFormatter.format(zonedValue))
            }
            TextButton(onClick = { showTimePicker = true }) {
                Text(value.toHourString())
            }
        }
    }

    if (showDatePicker) {
        // DatePickerState travaille en UTC "minuit" : on convertit donc la
        // date locale affichée en instant UTC uniquement pour initialiser le
        // sélecteur, puis on reconstruit la date locale à la confirmation.
        val initialUtcMillis = zonedValue.toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialUtcMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val pickedDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        val updated = zonedValue.toLocalTime().atDate(pickedDate).atZone(zone)
                        onValueChange(updated.toInstant())
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Annuler") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = zonedValue.hour,
            initialMinute = zonedValue.minute,
            is24Hour = true
        )
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Heure") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    val updated = zonedValue.withHour(timePickerState.hour).withMinute(timePickerState.minute)
                    onValueChange(updated.toInstant())
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Annuler") }
            }
        )
    }
}
