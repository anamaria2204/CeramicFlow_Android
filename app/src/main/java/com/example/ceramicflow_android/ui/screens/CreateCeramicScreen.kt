package com.example.ceramicflow_android.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.ceramicflow_android.data.model.CeramicType
import com.example.ceramicflow_android.data.model.NewCeramicData
import com.example.ceramicflow_android.ui.components.CameraCapture
import com.example.ceramicflow_android.ui.viewmodel.AddBookingViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
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

    val capturedImages = remember { mutableStateListOf<Uri>() }
    var showCamera by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            // Copiem poza din galerie într-un fișier local al aplicației
            // pentru a avea acces permanent la ea (la fel ca la Cameră)
            val file = copyUriToInternalStorage(context, selectedUri)
            file?.let { capturedImages.add(Uri.fromFile(it)) }
        }
    }

    // LOGICA DE AFIȘARE
    if (showCamera) {
        if (cameraPermissionState.status.isGranted) {
            CameraCapture(
                onImageFileTaken = { file ->
                    capturedImages.add(Uri.fromFile(file))
                    showCamera = false
                },
                onClose = { showCamera = false }
            )
        } else {
            LaunchedEffect(Unit) {
                cameraPermissionState.launchPermissionRequest()
            }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Camera permission needed.")
                Button(onClick = { showCamera = false }) { Text("Back") }
            }
        }
    } else {
        // --- FORMULARUL PRINCIPAL ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // HEADER
            Text(
                text = "New Ceramic Item",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // 1. NAME FIELD
            OutlinedTextField(
                value = ceramicName,
                onValueChange = { ceramicName = it },
                label = { Text("Item Name") },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // 2. TYPE DROPDOWN (Restaurat)
            ExposedDropdownMenuBox(
                expanded = isTypeDropdownExpanded,
                onExpandedChange = { isTypeDropdownExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = ceramicType.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Ceramic Type") },
                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isTypeDropdownExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = isTypeDropdownExpanded,
                    onDismissRequest = { isTypeDropdownExpanded = false }
                ) {
                    CeramicType.values().forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.name) },
                            onClick = {
                                ceramicType = type
                                isTypeDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // 3. QUANTITY FIELD (Restaurat)
            OutlinedTextField(
                value = ceramicQuantity,
                onValueChange = { if (it.all { char -> char.isDigit() }) ceramicQuantity = it },
                label = { Text("Quantity") },
                leadingIcon = { Icon(Icons.Default.ProductionQuantityLimits, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // 4. DESCRIPTION FIELD (Restaurat)
            OutlinedTextField(
                value = ceramicDescription,
                onValueChange = { ceramicDescription = it },
                label = { Text("Description (Optional)") },
                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp)
            )

            Divider()

            // 5. PHOTOS SECTION
            Text(
                text = "Photos",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Start)
            )

            // Lista orizontală de poze
            if (capturedImages.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                ) {
                    items(capturedImages) { uri ->
                        Box {
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = "Captured Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                            )
                            IconButton(
                                onClick = { capturedImages.remove(uri) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(24.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.White, modifier = Modifier.padding(4.dp))
                            }
                        }
                    }
                }
            }

            // BUTOANELE PENTRU POZE (Camera & Galerie)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Buton Galerie
                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload")
                }

                // Buton Cameră
                Button(
                    onClick = { showCamera = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Camera")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // BUTON NEXT
            Button(
                onClick = {
                    val newCeramic = NewCeramicData(
                        name = ceramicName,
                        type = ceramicType.name,
                        quantity = ceramicQuantity.toIntOrNull() ?: 1,
                        description = ceramicDescription,
                        images = capturedImages.map { it.toString() }
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
}

// --- Helper pentru a copia poza din Galerie în Cache-ul aplicației ---
fun copyUriToInternalStorage(context: Context, uri: Uri): File? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "gallery_${System.currentTimeMillis()}.jpg")
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