package com.example.ceramicflow_android.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ceramicflow_android.data.model.Booking
import com.example.ceramicflow_android.ui.viewmodel.BookingListViewModel
import com.example.ceramicflow_android.ui.viewmodel.UiState
import com.example.ceramicflow_android.util.ShakeDetector // Asigură-te că ai clasa ShakeDetector creată anterior
import kotlinx.coroutines.delay
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

    // --- STATE-URI UI ---
    // 0 = Upcoming, 1 = History
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Upcoming", "History")

    // Stare pentru animația vizuală (Flash)
    var showShakeFeedback by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // --- ANIMATIE RESET ---
    // Oprește efectul vizual după 1 secundă
    LaunchedEffect(showShakeFeedback) {
        if (showShakeFeedback) {
            delay(1000)
            showShakeFeedback = false
        }
    }

    // --- LIFECYCLE OBSERVER (Auto-refresh la revenire în ecran) ---
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

    // --- SHAKE DETECTOR (Senzori) ---
    DisposableEffect(Unit) {
        val shakeDetector = ShakeDetector(context) {
            // 1. Refresh date
            viewModel.loadBookings()

            // 2. Activare animatie vizuala
            showShakeFeedback = true

            // 3. Feedback Haptic (Vibratie)
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(200)
            }

            // 4. Toast Message
            Toast.makeText(context, "📳 Shake detected! Refreshing...", Toast.LENGTH_SHORT).show()
        }
        shakeDetector.start() // Pornim senzorul
        onDispose { shakeDetector.stop() } // Oprim senzorul la ieșire
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
                // Tab-urile pentru filtrare
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
            // Butonul (+) apare doar în tab-ul "Upcoming"
            if (selectedTabIndex == 0) {
                FloatingActionButton(onClick = onAddBookingClick) {
                    Icon(Icons.Default.Add, contentDescription = "Add Booking")
                }
            }
        }
    ) { paddingValues ->

        // Folosim un Box container pentru a putea suprapune animația peste listă
        Box(modifier = Modifier.fillMaxSize()) {

            // --- CONȚINUT LISTĂ ---
            when (val state = uiState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Success -> {
                    val allBookings = state.data
                    val now = Date()
                    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

                    // Împărțim lista în Trecut și Viitor
                    val (pastBookings, upcomingBookings) = allBookings.partition { booking ->
                        try {
                            val bookingDate = formatter.parse("${booking.date} ${booking.time}")
                            bookingDate != null && bookingDate.before(now)
                        } catch (e: Exception) {
                            false
                        }
                    }

                    // Alegem lista în funcție de Tab-ul selectat
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
                                bottom = 130.dp // Spațiu extra jos pentru FAB și Navigație
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(displayedBookings) { booking ->
                                BookingCard(
                                    booking = booking,
                                    isHistory = selectedTabIndex == 1,
                                    onClick = { onBookingClick(booking.ceramic.id) },
                                    // Apelăm funcția de ștergere din ViewModel
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

            // --- ANIMATIE OVERLAY (FLASH) ---
            // Acest Box apare peste listă doar când showShakeFeedback este true
            if (showShakeFeedback) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)), // Albastru transparent
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(100.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Refreshing...",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
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
    // Culoare diferită pentru istoric (Verde pastel)
    val cardBackgroundColor = if (isHistory) {
        Color(0xFFE8F5E9)
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
                    // Iconiță eroare sincronizare
                    if (!booking.isSynced) {
                        Icon(
                            Icons.Default.SyncProblem,
                            contentDescription = "Not Synced",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    // Buton Ștergere
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