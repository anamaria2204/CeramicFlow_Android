package com.example.ceramicflow_android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ceramicflow_android.data.model.CeramicType
import com.example.ceramicflow_android.data.model.NewCeramicData
import com.example.ceramicflow_android.ui.viewmodel.AddBookingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCeramicScreen(
    viewModel: AddBookingViewModel,
    onNext: () -> Unit
) {
    var ceramicName by remember { mutableStateOf("") }
    var ceramicType by remember { mutableStateOf(CeramicType.OTHER) }
    var ceramicQuantity by remember { mutableStateOf("1") }
    var ceramicDescription by remember { mutableStateOf("") }
    var isTypeDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("1. Create New Ceramic Item", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(value = ceramicName, onValueChange = { ceramicName = it }, label = { Text("Ceramic Name") }, modifier = Modifier.fillMaxWidth())

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

        OutlinedTextField(value = ceramicQuantity, onValueChange = { ceramicQuantity = it }, label = { Text("Quantity") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = ceramicDescription, onValueChange = { ceramicDescription = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                val newCeramic = NewCeramicData(
                    name = ceramicName,
                    type = ceramicType.name,
                    quantity = ceramicQuantity.toIntOrNull() ?: 1,
                    description = ceramicDescription
                )
                viewModel.setCeramicData(newCeramic)
                onNext()
            },
            enabled = ceramicName.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Next: Schedule Date and Time")
        }
    }
}