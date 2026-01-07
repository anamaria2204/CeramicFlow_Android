package com.example.ceramicflow_android.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ceramicflow_android.data.model.CeramicType
import com.example.ceramicflow_android.data.model.NewCeramicData
import com.example.ceramicflow_android.ui.viewmodel.AddBookingUiState
import com.example.ceramicflow_android.ui.viewmodel.AddBookingViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBookingScreen(
    onBookingSuccess: () -> Unit,
    viewModel: AddBookingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // State for the new ceramic item
    var ceramicName by remember { mutableStateOf("") }
    var ceramicType by remember { mutableStateOf(CeramicType.OTHER) }
    var ceramicQuantity by remember { mutableStateOf("1") }
    var ceramicDescription by remember { mutableStateOf("") }
    var isTypeDropdownExpanded by remember { mutableStateOf(false) }

    // State for the booking itself
    var selectedDate by remember { mutableStateOf<Calendar?>(null) }
    var selectedTime by remember { mutableStateOf<String?>(null) }
    val showDatePicker = remember { mutableStateOf(false) }

    val timeSlots = (12..22).map { "$it:00" } // 12 PM to 10 PM

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AddBookingUiState.Success -> {
                Toast.makeText(context, "Booking created successfully!", Toast.LENGTH_LONG).show()
                onBookingSuccess()
            }
            is AddBookingUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()), // Make the column scrollable
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("1. Create New Ceramic Item", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(value = ceramicName, onValueChange = { ceramicName = it }, label = { Text("Ceramic Name") })

        ExposedDropdownMenuBox(expanded = isTypeDropdownExpanded, onExpandedChange = { isTypeDropdownExpanded = it }) {
            OutlinedTextField(
                value = ceramicType.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Ceramic Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isTypeDropdownExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = isTypeDropdownExpanded, onDismissRequest = { isTypeDropdownExpanded = false }) {
                CeramicType.values().forEach { type ->
                    DropdownMenuItem(text = { Text(type.name) }, onClick = { ceramicType = type; isTypeDropdownExpanded = false })
                }
            }
        }

        OutlinedTextField(value = ceramicQuantity, onValueChange = { ceramicQuantity = it }, label = { Text("Quantity") })
        OutlinedTextField(value = ceramicDescription, onValueChange = { ceramicDescription = it }, label = { Text("Description") })

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        Text("2. Schedule Booking", style = MaterialTheme.typography.headlineMedium)

        Button(onClick = { showDatePicker.value = true }) {
            Text(selectedDate?.let { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(it.time) } ?: "Choose Date")
        }

        if (showDatePicker.value) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(onDismissRequest = { showDatePicker.value = false }, confirmButton = { Button(onClick = {
                datePickerState.selectedDateMillis?.let {
                    val cal = Calendar.getInstance(); cal.timeInMillis = it; selectedDate = cal
                }
                showDatePicker.value = false
            }) { Text("OK") }}) { DatePicker(state = datePickerState) }
        }

        if (selectedDate != null) {
            Column(modifier = Modifier.heightIn(max = 200.dp)) {
                timeSlots.forEach { time ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { selectedTime = time },
                        colors = CardDefaults.cardColors(containerColor = if (time == selectedTime) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(time, modifier = Modifier.padding(16.dp))
                    }
                }
            }
        }

        Button(
            onClick = {
                val dateStr = selectedDate?.let { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(it.time) }
                val newCeramic = NewCeramicData(
                    name = ceramicName,
                    type = ceramicType.name,
                    quantity = ceramicQuantity.toIntOrNull() ?: 1,
                    description = ceramicDescription
                )
                if (dateStr != null && selectedTime != null) {
                    // Corrected the function calls to match the updated ViewModel
                    viewModel.setCeramicData(newCeramic)
                    viewModel.createBooking(dateStr, selectedTime!!)
                }
            },
            enabled = ceramicName.isNotBlank() && selectedDate != null && selectedTime != null && uiState !is AddBookingUiState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState is AddBookingUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Create Item and Book")
            }
        }
    }
}