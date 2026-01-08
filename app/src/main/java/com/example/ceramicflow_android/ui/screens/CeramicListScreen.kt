package com.example.ceramicflow_android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ceramicflow_android.data.model.Booking
import com.example.ceramicflow_android.ui.viewmodel.BookingListViewModel
import com.example.ceramicflow_android.ui.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CeramicListScreen(
    onAddBookingClick: () -> Unit,
    onBookingClick: (String) -> Unit,
    viewModel: BookingListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Stare pentru Tab-ul selectat (0 = Upcoming, 1 = History)
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Upcoming", "History")

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadBookings()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("My Bookings") },
                    actions = {
                        IconButton(onClick = { viewModel.loadBookings() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh Bookings")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                // --- TAB ROW ---
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            // Arătăm butonul de adăugare doar pe tab-ul "Upcoming" (opțional, dar logic)
            if (selectedTabIndex == 0) {
                FloatingActionButton(onClick = onAddBookingClick) {
                    Icon(Icons.Default.Add, contentDescription = "Add Booking")
                }
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Success -> {
                val allBookings = state.data

                // --- FILTRARE PROGRAMĂRI ---
                val now = Date()
                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

                // Împărțim lista în două
                val (pastBookings, upcomingBookings) = allBookings.partition { booking ->
                    try {
                        val bookingDate = formatter.parse("${booking.date} ${booking.time}")
                        bookingDate != null && bookingDate.before(now)
                    } catch (e: Exception) {
                        false // Dacă e eroare de parsare, o considerăm viitoare (safe fallback)
                    }
                }

                // Alegem lista de afișat în funcție de tab
                val displayedBookings = if (selectedTabIndex == 0) upcomingBookings else pastBookings

                if (displayedBookings.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (selectedTabIndex == 0) "No upcoming bookings." else "No completed bookings.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(
                            top = 16.dp,
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 130.dp // Spațiu pentru FAB
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(displayedBookings) { booking ->
                            BookingCard(
                                booking = booking,
                                isHistory = selectedTabIndex == 1, // Trimitem info dacă e istoric
                                onClick = { onBookingClick(booking.ceramic.id) },
                                onDeleteClick = { viewModel.deleteBooking(booking) }
                            )
                        }
                    }
                }
            }
            is UiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun BookingCard(
    booking: Booking,
    isHistory: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    // Calculăm culoarea: Verde pentru istoric, Normal pentru viitor
    val cardBackgroundColor = if (isHistory) {
        Color(0xFFE8F5E9) // Verde pastel
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = booking.ceramic.name,
                    style = MaterialTheme.typography.titleLarge
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!booking.isSynced) {
                        Icon(
                            Icons.Default.SyncProblem,
                            contentDescription = "Not Synced",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Booking",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (isHistory) {
                Text(
                    text = "Status: Completed",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Text(
                text = "Time: ${booking.time} (${booking.date})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Quantity: ${booking.ceramic.quantity}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}