package com.example.ceramicflow_android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
            .fillMaxSize()
            .padding(24.dp) // Padding mai generos pe margini
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp), // Spațiu mai mare între elemente
        horizontalAlignment = Alignment.CenterHorizontally // Centrăm elementele pe orizontală (inclusiv textele din titlu)
    ) {

        // --- HEADER CENTRAT ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 16.dp, top = 16.dp)
        ) {
            Text(
                text = "New Ceramic Item",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Enter the details of the object you want to paint.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        // --- FORM FIELDS ---

        // 1. Name
        OutlinedTextField(
            value = ceramicName,
            onValueChange = { ceramicName = it },
            label = { Text("Item Name") },
            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // 2. Type Dropdown
        ExposedDropdownMenuBox(
            expanded = isTypeDropdownExpanded,
            onExpandedChange = { isTypeDropdownExpanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = ceramicType.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Type") },
                leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isTypeDropdownExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = isTypeDropdownExpanded,
                onDismissRequest = { isTypeDropdownExpanded = false }
            ) {
                CeramicType.values().forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.name) },
                        onClick = { ceramicType = type; isTypeDropdownExpanded = false }
                    )
                }
            }
        }

        // 3. Quantity
        OutlinedTextField(
            value = ceramicQuantity,
            onValueChange = { if (it.all { char -> char.isDigit() }) ceramicQuantity = it },
            label = { Text("Quantity") },
            leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // 4. Description
        OutlinedTextField(
            value = ceramicDescription,
            onValueChange = { ceramicDescription = it },
            label = { Text("Description (Optional)") },
            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp), // Câmp mai înalt pentru descriere
            shape = RoundedCornerShape(12.dp),
            maxLines = 5
        )

        Spacer(modifier = Modifier.weight(1f))

        // --- NEXT BUTTON ---
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
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Next Step")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null)
        }
    }
}