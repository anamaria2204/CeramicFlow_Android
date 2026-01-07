package com.example.ceramicflow_android.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ceramicflow_android.ui.viewmodel.AddBookingUiState
import com.example.ceramicflow_android.ui.viewmodel.AddBookingViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleBookingScreen(
    viewModel: AddBookingViewModel,
    onBookingSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var selectedDate by remember { mutableStateOf<Calendar?>(null) }
    var selectedTime by remember { mutableStateOf<String?>(null) }
    val showDatePicker = remember { mutableStateOf(false) }

    val availableTimeSlots = remember(selectedDate) {
        val now = Calendar.getInstance()
        if (selectedDate == null) return@remember emptyList()

        val selectedCal = selectedDate!!
        val today = now.get(Calendar.DAY_OF_YEAR) == selectedCal.get(Calendar.DAY_OF_YEAR) &&
                now.get(Calendar.YEAR) == selectedCal.get(Calendar.YEAR)

        if (today) {
            val currentHour = now.get(Calendar.HOUR_OF_DAY)
            (12..22).filter { it > currentHour }.map { "$it:00" }
        } else {
            (12..22).map { "$it:00" }
        }
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AddBookingUiState.Success -> {
                onBookingSuccess()
            }
            is AddBookingUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.resetState() }
    }

    Column(
        modifier = Modifier.padding(16.dp).fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("2. Schedule Date and Time", style = MaterialTheme.typography.headlineMedium)

        Button(onClick = { showDatePicker.value = true }) {
            Text(selectedDate?.let { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(it.time) } ?: "Choose Date")
        }

        if (showDatePicker.value) {
            val datePickerState = rememberDatePickerState()
            val confirmEnabled = remember { derivedStateOf { datePickerState.selectedDateMillis != null } }

            DatePickerDialog(
                onDismissRequest = { showDatePicker.value = false },
                confirmButton = {
                    Button(
                        onClick = {
                            datePickerState.selectedDateMillis?.let {
                                val cal = Calendar.getInstance()
                                cal.timeInMillis = it
                                selectedDate = cal
                            }
                            showDatePicker.value = false
                        },
                        enabled = confirmEnabled.value
                    ) { Text("OK") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(availableTimeSlots) { time ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { selectedTime = time },
                    colors = CardDefaults.cardColors(containerColor = if (time == selectedTime) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(time, modifier = Modifier.padding(16.dp))
                }
            }
        }

        Button(
            onClick = {
                val dateStr = selectedDate?.let { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(it.time) }
                if (dateStr != null && selectedTime != null) {
                    viewModel.createBooking(dateStr, selectedTime!!)
                }
            },
            enabled = selectedDate != null && selectedTime != null && uiState !is AddBookingUiState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState is AddBookingUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Confirm Booking")
            }
        }
    }
}