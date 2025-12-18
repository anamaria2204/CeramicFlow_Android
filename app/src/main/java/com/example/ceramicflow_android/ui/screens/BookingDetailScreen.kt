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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ceramicflow_android.ui.viewmodel.CeramicDetailUiState
import com.example.ceramicflow_android.ui.viewmodel.CeramicDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CeramicDetailScreen(
    ceramicId: String,
    onNavigateBack: () -> Unit,
    viewModel: CeramicDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(ceramicId) {
        viewModel.loadCeramic(ceramicId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ceramic Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            when (val state = uiState) {
                is CeramicDetailUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is CeramicDetailUiState.Success -> {
                    val ceramic = state.ceramic
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(ceramic.name, style = MaterialTheme.typography.headlineSmall)
                        Text("Type: ${ceramic.type.name}")
                        Text("Quantity: ${ceramic.quantity}")
                        Text("Description: ${ceramic.description}")
                    }
                }
                is CeramicDetailUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}