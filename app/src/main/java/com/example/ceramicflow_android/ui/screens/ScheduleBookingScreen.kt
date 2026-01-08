package com.example.ceramicflow_android.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ceramicflow_android.ui.viewmodel.AddBookingUiState
import com.example.ceramicflow_android.ui.viewmodel.AddBookingViewModel
import kotlinx.coroutines.delay
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
    val existingBookings by viewModel.existingBookings.collectAsState()
    val context = LocalContext.current

    var selectedDate by remember { mutableStateOf<Calendar?>(null) }
    var selectedTime by remember { mutableStateOf<String?>(null) }
    val showDatePicker = remember { mutableStateOf(false) }

    // State pentru animația de succes
    var showSuccessAnimation by remember { mutableStateOf(false) }
    var isOfflineSuccess by remember { mutableStateOf(false) }

    // --- LOGICA DE FILTRARE ---
    val availableTimeSlots = remember(selectedDate, existingBookings) {
        val now = Calendar.getInstance()
        if (selectedDate == null) return@remember emptyList()

        val selectedCal = selectedDate!!
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val selectedDateString = dateFormatter.format(selectedCal.time)

        val isToday = now.get(Calendar.DAY_OF_YEAR) == selectedCal.get(Calendar.DAY_OF_YEAR) &&
                now.get(Calendar.YEAR) == selectedCal.get(Calendar.YEAR)

        val allSlots = (12..22).map { "$it:00" }

        allSlots.filter { timeSlot ->
            val isTimeValid = if (isToday) {
                val currentHour = now.get(Calendar.HOUR_OF_DAY)
                val slotHour = timeSlot.split(":")[0].toInt()
                slotHour > currentHour
            } else {
                true
            }
            val isSlotFree = existingBookings.none { booking ->
                booking.date == selectedDateString && booking.time == timeSlot
            }
            isTimeValid && isSlotFree
        }
    }

    // --- INTERCEPTARE SUCCES PENTRU ANIMAȚIE ---
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AddBookingUiState.Success -> {
                // 1. Setăm tipul de succes (Offline sau Online)
                isOfflineSuccess = state.isOffline

                // 2. Activăm animația
                showSuccessAnimation = true

                // 3. Așteptăm 2 secunde să se bucure utilizatorul de animație
                delay(2000)

                // 4. Navigăm înapoi
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

    // Folosim un Box ca rădăcină pentru a putea suprapune animația
    Box(modifier = Modifier.fillMaxSize()) {

        // --- CONȚINUTUL PRINCIPAL AL ECRANULUI ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // HEADER
            Column {
                Text(
                    text = "Schedule Booking",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Select your preferred date and time.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // DATE SELECTOR
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker.value = true },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Date",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = selectedDate?.let { SimpleDateFormat("EEEE, d MMMM yyyy", Locale.US).format(it.time) }
                                ?: "Tap to choose a date",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (selectedDate != null) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Select Date",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // DATE PICKER DIALOG
            if (showDatePicker.value) {
                val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
                val confirmEnabled = remember { derivedStateOf { datePickerState.selectedDateMillis != null } }

                DatePickerDialog(
                    onDismissRequest = { showDatePicker.value = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let {
                                    val cal = Calendar.getInstance()
                                    cal.timeInMillis = it
                                    val todayStart = Calendar.getInstance().apply {
                                        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                                    }
                                    if (cal.timeInMillis >= todayStart.timeInMillis) {
                                        selectedDate = cal
                                        selectedTime = null
                                    } else {
                                        Toast.makeText(context, "Cannot select past date", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                showDatePicker.value = false
                            },
                            enabled = confirmEnabled.value
                        ) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker.value = false }) { Text("Cancel") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            Divider()

            // TIME SLOTS (GRID VIEW)
            if (selectedDate != null) {
                Text(
                    text = "Available Time Slots",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                if (availableTimeSlots.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No slots available for this date.", color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(availableTimeSlots) { time ->
                            val isSelected = (time == selectedTime)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { selectedTime = time }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = time,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("Please select a date first", color = MaterialTheme.colorScheme.outline)
                }
            }

            // CONFIRM BUTTON
            Button(
                onClick = {
                    val dateStr = selectedDate?.let { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(it.time) }
                    if (dateStr != null && selectedTime != null) {
                        viewModel.createBooking(dateStr, selectedTime!!)
                    }
                },
                enabled = selectedDate != null && selectedTime != null && uiState !is AddBookingUiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState is AddBookingUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirm Booking")
                }
            }
        }

        // --- ANIMATIA DE SUCCES OVERLAY ---
        AnimatedVisibility(
            visible = showSuccessAnimation,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            // Fundal semi-transparent pe tot ecranul
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                    .clickable(enabled = false) {}, // Blochează click-urile în spate
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Alegem iconița și culoarea în funcție de Offline/Online
                    val icon = if (isOfflineSuccess) Icons.Default.CloudOff else Icons.Default.CloudDone
                    val iconColor = if (isOfflineSuccess) Color(0xFFFBC02D) else Color(0xFF4CAF50) // Galben sau Verde
                    val message = if (isOfflineSuccess) "Saved Locally" else "Booking Confirmed!"
                    val subMessage = if (isOfflineSuccess) "Will sync when online" else "See you at the studio"

                    // Cerc colorat în spate
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(iconColor.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = "Success",
                            tint = iconColor,
                            modifier = Modifier.size(64.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = message,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = subMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}