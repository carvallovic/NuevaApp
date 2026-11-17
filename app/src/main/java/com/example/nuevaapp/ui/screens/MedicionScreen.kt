package com.example.nuevaapp.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.example.nuevaapp.model.Medicion
import com.example.nuevaapp.viewmodel.EquipoViewModel
import com.example.nuevaapp.viewmodel.MedicionViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicionScreen(
    viewModel: MedicionViewModel,
    equipoViewModel: EquipoViewModel,
    currentUserId: Int = 0,
    currentUserName: String = "",
    registroId: Int = -1,
    onVolver: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val equipoState by equipoViewModel.uiState.collectAsState()
    val equipos = equipoState.equipos

    var equipoId by remember { mutableStateOf("") }
    var valor by remember { mutableStateOf("") }
    var unidad by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }
    var fotosList by remember { mutableStateOf<List<String>>(emptyList()) }

    var expanded by remember { mutableStateOf(false) }
    var selectedEquipoText by remember { mutableStateOf("") }

    var medicionEnEdicion by remember { mutableStateOf<Medicion?>(null) }
    var medicionAEliminar by remember { mutableStateOf<Medicion?>(null) }
    var showCameraDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showCameraDialog = true
        }
    }

    // Actualizar texto seleccionado cuando cambia equipoId o equipos
    LaunchedEffect(equipoId, equipos) {
        if (equipoId.isNotBlank()) {
            val idInt = equipoId.toIntOrNull()
            val eq = equipos.find { it.id == idInt }
            selectedEquipoText = if (eq != null) "${eq.id} - ${eq.nombre}" else equipoId
        } else {
            selectedEquipoText = ""
        }
    }

    // Cargar medición cuando se pasa un registroId desde la navegación
    LaunchedEffect(registroId, state.mediciones, equipos) {
        if (registroId != -1) {
            val encontrada = state.mediciones.find { it.id == registroId }
            if (encontrada != null) {
                medicionEnEdicion = encontrada
                equipoId = encontrada.equipoId.toString()
                val eq = equipos.find { it.id == encontrada.equipoId }
                selectedEquipoText = if (eq != null) "${eq.id} - ${eq.nombre}" else encontrada.equipoId.toString()
                valor = encontrada.valor.toString()
                unidad = encontrada.unidad
                observaciones = encontrada.observaciones
                fotosList = if (encontrada.fotoUri.isNotBlank()) encontrada.fotoUri.split(",").filter { it.isNotBlank() } else emptyList()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Text(
            text = if (medicionEnEdicion == null) "Registro de Mediciones" else "Editando Medición",
            style = MaterialTheme.typography.headlineSmall
        )
        if (currentUserName.isNotBlank()) {
            Text(
                text = "Técnico: $currentUserName (ID: $currentUserId)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Lista desplegable para seleccionar el equipo (ID - Nombre)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedEquipoText,
                onValueChange = {},
                readOnly = true,
                label = { Text("Equipo (ID - Nombre)") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                if (equipos.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No hay equipos registrados") },
                        onClick = { expanded = false }
                    )
                } else {
                    equipos.forEach { equipo ->
                        val displayText = "${equipo.id} - ${equipo.nombre}"
                        DropdownMenuItem(
                            text = { Text(displayText) },
                            onClick = {
                                equipoId = equipo.id.toString()
                                selectedEquipoText = displayText
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = valor,
            onValueChange = { valor = it },
            label = { Text("Valor Medido") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = unidad,
            onValueChange = { unidad = it },
            label = { Text("Unidad (Ej: V, A, °C, PSI)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = observaciones,
            onValueChange = { observaciones = it },
            label = { Text("Observaciones (Opcional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Sección de Fotografías (Obligatoria al menos 1, hasta 5 con botón +)
        Text(text = "Fotografías (Obligatoria al menos 1, máx. 5):", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            fotosList.forEachIndexed { index, uriStr ->
                Box(
                    modifier = Modifier
                        .size(70.dp)
                ) {
                    AsyncImage(
                        model = Uri.parse(uriStr),
                        contentDescription = "Foto $index",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = {
                            fotosList = fotosList.toMutableList().apply { removeAt(index) }
                        },
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar foto",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Botón '+' si hay menos de 5 fotos
            if (fotosList.size < 5) {
                OutlinedButton(
                    onClick = {
                        val permissionCheck = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        )
                        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                            showCameraDialog = true
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.size(70.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("+", style = MaterialTheme.typography.headlineMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val eqId = equipoId.toIntOrNull() ?: 0
                    val valMed = valor.toDoubleOrNull() ?: 0.0
                    val fotoUriString = fotosList.joinToString(",")
                    val nombreTecnico = currentUserName.ifBlank { "Técnico General" }

                    if (medicionEnEdicion == null) {
                        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                        val nuevaMedicion = Medicion(
                            equipoId = eqId,
                            valor = valMed,
                            unidad = unidad,
                            observaciones = observaciones,
                            fecha = currentDate,
                            fotoUri = fotoUriString,
                            usuarioId = currentUserId,
                            tecnicoNombre = nombreTecnico
                        )
                        viewModel.agregarMedicion(nuevaMedicion)
                    } else {
                        medicionEnEdicion?.let { med ->
                            viewModel.editarMedicion(
                                med.copy(
                                    equipoId = eqId,
                                    valor = valMed,
                                    unidad = unidad,
                                    observaciones = observaciones,
                                    fotoUri = fotoUriString,
                                    usuarioId = if (med.usuarioId == 0) currentUserId else med.usuarioId,
                                    tecnicoNombre = med.tecnicoNombre.ifBlank { nombreTecnico }
                                )
                            )
                        }
                        medicionEnEdicion = null
                    }
                    if (state.error == null) {
                        equipoId = ""
                        selectedEquipoText = ""
                        valor = ""
                        unidad = ""
                        observaciones = ""
                        fotosList = emptyList()

                        if (registroId != -1) {
                            onVolver()
                        }
                    }
                },
                modifier = Modifier.weight(1.8f)
            ) {
                Text(if (medicionEnEdicion == null) "Registrar Medición" else "Guardar Cambios")
            }

            if (medicionEnEdicion != null) {
                OutlinedButton(
                    onClick = {
                        medicionEnEdicion = null
                        equipoId = ""
                        selectedEquipoText = ""
                        valor = ""
                        unidad = ""
                        observaciones = ""
                        fotosList = emptyList()
                        if (registroId != -1) {
                            onVolver()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }
            }
        }

        state.error?.let { msg ->
            Text(text = msg, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 4.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.mediciones, key = { it.id }) { medicion ->
                    val eq = equipos.find { it.id == medicion.equipoId }
                    val equipoDisplay = if (eq != null) "${eq.id} - ${eq.nombre}" else "ID: ${medicion.equipoId}"

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "${medicion.valor} ${medicion.unidad}", style = MaterialTheme.typography.titleMedium)
                                Text(text = "Equipo: $equipoDisplay | Fecha: ${medicion.fecha}")
                                Text(text = "Técnico: ${medicion.tecnicoNombre} (ID: ${medicion.usuarioId})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                if (medicion.observaciones.isNotBlank()) {
                                    Text(text = "Obs: ${medicion.observaciones}", style = MaterialTheme.typography.bodySmall)
                                }
                                if (medicion.fotoUri.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val uris = medicion.fotoUri.split(",").filter { it.isNotBlank() }
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        uris.forEach { uriStr ->
                                            AsyncImage(
                                                model = Uri.parse(uriStr),
                                                contentDescription = "Miniatura foto",
                                                modifier = Modifier
                                                    .size(50.dp)
                                                    .padding(2.dp),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                }
                            }
                            Row {
                                IconButton(onClick = {
                                    medicionEnEdicion = medicion
                                    equipoId = medicion.equipoId.toString()
                                    selectedEquipoText = equipoDisplay
                                    valor = medicion.valor.toString()
                                    unidad = medicion.unidad
                                    observaciones = medicion.observaciones
                                    fotosList = medicion.fotoUri.split(",").filter { it.isNotBlank() }
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { medicionAEliminar = medicion }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal de diálogo de cámara con CameraX
    if (showCameraDialog) {
        CameraCaptureDialog(
            onImageCaptured = { uri ->
                fotosList = fotosList + uri.toString()
                showCameraDialog = false
            },
            onError = {
                showCameraDialog = false
            },
            onDismiss = {
                showCameraDialog = false
            }
        )
    }

    // Modal de confirmación para eliminar
    medicionAEliminar?.let { medicion ->
        AlertDialog(
            onDismissRequest = { medicionAEliminar = null },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Estás seguro de que deseas eliminar la medición de ${medicion.valor} ${medicion.unidad}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.borrarMedicion(medicion)
                        medicionAEliminar = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { medicionAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun CameraCaptureDialog(
    onImageCaptured: (Uri) -> Unit,
    onError: (Exception) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        imageCapture = ImageCapture.Builder().build()
                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageCapture
                            )
                        } catch (exc: Exception) {
                            Log.e("CameraX", "Use case binding failed", exc)
                        }
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                Button(
                    onClick = {
                        val capture = imageCapture ?: return@Button
                        val photoFile = File(
                            context.cacheDir,
                            "medicion_${System.currentTimeMillis()}.jpg"
                        )
                        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                        capture.takePicture(
                            outputOptions,
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                    val savedUri = Uri.fromFile(photoFile)
                                    onImageCaptured(savedUri)
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    onError(exception)
                                }
                            }
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text("Capturar Foto")
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
}
