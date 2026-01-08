package com.example.ceramicflow_android.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ceramicflow_android.ui.viewmodel.CeramicDetailViewModel
import com.example.ceramicflow_android.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CeramicDetailScreen(
    ceramicId: String, // Primit din navigație
    onNavigateBack: () -> Unit,
    viewModel: CeramicDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ceramic Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    CircularProgressIndicator()
                }
                is UiState.Success -> {
                    val ceramic = state.data
                    Column(modifier = Modifier.padding(16.dp).align(Alignment.TopStart), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(ceramic.name, style = MaterialTheme.typography.headlineSmall)
                        Text("Type: ${ceramic.type.name}")
                        Text("Quantity: ${ceramic.quantity}")
                        Text("Description: ${ceramic.description}")
                    }
                }
                is UiState.Error -> {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}