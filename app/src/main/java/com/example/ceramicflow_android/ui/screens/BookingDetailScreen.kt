package com.example.ceramicflow_android.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ceramicflow_android.ui.components.CameraCapture
import com.example.ceramicflow_android.ui.viewmodel.BookingListViewModel
import com.example.ceramicflow_android.ui.viewmodel.UiState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import androidx.compose.foundation.clickable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun BookingDetailScreen(
    bookingId: String,
    viewModel: BookingListViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val booking = (uiState as? UiState.Success)?.data?.find { it.id == bookingId }
    val context = LocalContext.current

    // --- STATE PENTRU CAMERA & GALERIE ---
    var showCamera by remember { mutableStateOf(false) }
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Launcher Galerie
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            val file = copyUriToInternalStorage2(context, selectedUri)
            file?.let {
                // SALVĂM POZA IMEDIAT ÎN BAZA DE DATE
                viewModel.addPhotoToBooking(bookingId, Uri.fromFile(it).toString())
            }
        }
    }

    if (showCamera) {
        // --- ECRAN CAMERĂ (FULL SCREEN) ---
        if (cameraPermissionState.status.isGranted) {
            CameraCapture(
                onImageFileTaken = { file ->
                    // SALVĂM POZA IMEDIAT ÎN BAZA DE DATE
                    viewModel.addPhotoToBooking(bookingId, Uri.fromFile(file).toString())
                    showCamera = false
                },
                onClose = { showCamera = false }
            )
        } else {
            LaunchedEffect(Unit) { cameraPermissionState.launchPermissionRequest() }
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Camera permission required")
                Button(onClick = { showCamera = false }) { Text("Back") }
            }
        }
    } else {
        // --- ECRAN NORMAL DE DETALII ---
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Booking Details") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            floatingActionButton = {
                // BUTON FLOTANT PENTRU ADĂUGARE POZĂ
                if (booking != null) {
                    ExtendedFloatingActionButton(
                        onClick = { showCamera = true },
                        icon = { Icon(Icons.Default.CameraAlt, "Add Photo") },
                        text = { Text("Add Photo") },
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                }
            }
        ) { paddingValues ->
            if (booking == null) {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text("Booking not found.")
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = booking.ceramic.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // --- GALERIE FOTO ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Photos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        // Buton mic pentru Galerie
                        TextButton(onClick = { galleryLauncher.launch("image/*") }) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("From Gallery")
                        }
                    }

                    if (booking.ceramic.images.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        ) {
                            items(booking.ceramic.images) { imagePath ->
                                AsyncImage(
                                    model = imagePath,
                                    contentDescription = "Ceramic Photo",
                                    modifier = Modifier
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, Color.Gray, RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                .clickable { showCamera = true }, // Click pe placeholder deschide camera
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("No photos yet. Tap to add.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Divider()

                    DetailRow(Icons.Default.CalendarToday, "Date", booking.date)
                    DetailRow(Icons.Default.Schedule, "Time", booking.time)
                    DetailRow(Icons.Default.ProductionQuantityLimits, "Quantity", "${booking.ceramic.quantity} items")

                    if (booking.ceramic.description.isNotBlank()) {
                        DetailRow(Icons.Default.Description, "Description", booking.ceramic.description)
                    }

                    // Spațiu extra jos pentru FAB
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

fun copyUriToInternalStorage2(context: Context, uri: Uri): File? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "gallery_added_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}