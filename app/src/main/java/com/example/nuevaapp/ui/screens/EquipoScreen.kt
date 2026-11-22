package com.example.nuevaapp.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.nuevaapp.model.Equipo
import com.example.nuevaapp.viewmodel.EquipoViewModel
import com.example.nuevaapp.viewmodel.UserRole
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipoScreen(
    viewModel: EquipoViewModel,
    currentUserId: Int = 0,
    currentUserName: String = "",
    userRole: UserRole? = null
) {
    val state by viewModel.uiState.collectAsState()

    var nombre by remember { mutableStateOf("") }
    var codigo by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("Operativo") }

    var selectedClienteId by remember { mutableStateOf(0) }
    var selectedClienteText by remember { mutableStateOf("") }
    var expandedCliente by remember { mutableStateOf(false) }

    var equipoEnEdicion by remember { mutableStateOf<Equipo?>(null) }

    val context = LocalContext.current

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (isGranted) {
            obtenerUbicacionActualComuna(context) { comunaObtenida ->
                ubicacion = comunaObtenida
            }
        } else {
            Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    // Si el usuario logueado es CLIENTE, fijar automáticamente su ID y Nombre sin poder modificarlo
    LaunchedEffect(userRole, currentUserId, currentUserName) {
        if (userRole == UserRole.CLIENTE) {
            selectedClienteId = currentUserId
            selectedClienteText = "$currentUserId - $currentUserName"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Text(
            text = if (equipoEnEdicion == null) "Registro de Equipos" else "Editando Equipo",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 1. Campo Cliente (PRIMERO ARRIBA: Desplegable para ADMIN/TECNICO, solo lectura obligatorio para CLIENTE)
        if (userRole == UserRole.CLIENTE) {
            OutlinedTextField(
                value = "$currentUserId - $currentUserName",
                onValueChange = {},
                readOnly = true,
                label = { Text("Cliente (Asignado)") },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            ExposedDropdownMenuBox(
                expanded = expandedCliente,
                onExpandedChange = { expandedCliente = !expandedCliente },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedClienteText,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Seleccionar Cliente (ID - Nombre)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCliente) },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedCliente,
                    onDismissRequest = { expandedCliente = false }
                ) {
                    if (state.clientes.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No hay usuarios con perfil CLIENTE registrados") },
                            onClick = { expandedCliente = false }
                        )
                    } else {
                        state.clientes.forEach { cliente ->
                            val textDisplay = "${cliente.id} - ${cliente.nombre}"
                            DropdownMenuItem(
                                text = { Text(textDisplay) },
                                onClick = {
                                    selectedClienteId = cliente.id
                                    selectedClienteText = textDisplay
                                    expandedCliente = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Formulario de datos del equipo
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre del Equipo") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = codigo,
            onValueChange = { codigo = it },
            label = { Text("Código / TAG") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = ubicacion,
            onValueChange = { ubicacion = it },
            label = { Text("Ubicación / Comuna en Chile") },
            trailingIcon = {
                IconButton(onClick = {
                    val fineCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    val coarseCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                    if (fineCheck == PackageManager.PERMISSION_GRANTED || coarseCheck == PackageManager.PERMISSION_GRANTED) {
                        obtenerUbicacionActualComuna(context) { comunaObtenida ->
                            ubicacion = comunaObtenida
                        }
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Obtener ubicación actual GPS",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val finalClienteId = if (userRole == UserRole.CLIENTE) currentUserId else selectedClienteId
                    val finalClienteNombre = if (userRole == UserRole.CLIENTE) currentUserName else selectedClienteText.substringAfter("- ").ifBlank { "Cliente Genérico" }

                    if (equipoEnEdicion == null) {
                        viewModel.agregarEquipo(nombre, codigo, ubicacion, estado, finalClienteId, finalClienteNombre)
                    } else {
                        equipoEnEdicion?.let { equipo ->
                            viewModel.editarEquipo(
                                equipo.copy(
                                    nombre = nombre,
                                    codigo = codigo,
                                    ubicacion = ubicacion,
                                    estado = estado,
                                    clienteId = finalClienteId,
                                    clienteNombre = finalClienteNombre
                                )
                            )
                        }
                        equipoEnEdicion = null
                    }
                    if (state.error == null) {
                        nombre = ""
                        codigo = ""
                        ubicacion = ""
                        estado = "Operativo"
                        if (userRole != UserRole.CLIENTE) {
                            selectedClienteId = 0
                            selectedClienteText = ""
                        }
                    }
                },
                modifier = Modifier.weight(1.8f)
            ) {
                Text(if (equipoEnEdicion == null) "Guardar en BD" else "Guardar Cambios")
            }

            if (equipoEnEdicion != null) {
                OutlinedButton(
                    onClick = {
                        equipoEnEdicion = null
                        nombre = ""
                        codigo = ""
                        ubicacion = ""
                        estado = "Operativo"
                        if (userRole != UserRole.CLIENTE) {
                            selectedClienteId = 0
                            selectedClienteText = ""
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }
            }
        }

        // Mostrar alerta de error si existe
        state.error?.let { msg ->
            Text(text = msg, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 4.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista que despliega los registros de SQLite
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.equipos, key = { it.id }) { equipo ->
                    EquipoCard(
                        equipo = equipo,
                        onEdit = {
                            equipoEnEdicion = it
                            nombre = it.nombre
                            codigo = it.codigo
                            ubicacion = it.ubicacion
                            estado = it.estado
                            selectedClienteId = it.clienteId
                            selectedClienteText = if (it.clienteId > 0) "${it.clienteId} - ${it.clienteNombre}" else ""
                        },
                        onDelete = { viewModel.borrarEquipo(it) }
                    )
                }
            }
        }
    }
}

// Funciones auxiliares para obtener la ubicación GPS y traducirla a Comuna en Chile
private fun obtenerUbicacionActualComuna(context: Context, onUbicacionObtenida: (String) -> Unit) {
    try {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager == null) {
            Toast.makeText(context, "Servicio de ubicación no disponible", Toast.LENGTH_SHORT).show()
            return
        }

        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled && !isNetworkEnabled) {
            Toast.makeText(context, "Por favor active el GPS o la ubicación en el dispositivo", Toast.LENGTH_SHORT).show()
            return
        }

        val provider = if (isGpsEnabled) LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER

        @Suppress("MissingPermission")
        val lastLocation = locationManager.getLastKnownLocation(provider)
            ?: locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)

        if (lastLocation != null) {
            procesarCoordenadasAComuna(context, lastLocation.latitude, lastLocation.longitude, onUbicacionObtenida)
        } else {
            Toast.makeText(context, "Obteniendo última ubicación conocida...", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error al obtener la ubicación: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun procesarCoordenadasAComuna(context: Context, lat: Double, lng: Double, onUbicacionObtenida: (String) -> Unit) {
    try {
        val geocoder = Geocoder(context, Locale.forLanguageTag("es-CL"))
        @Suppress("DEPRECATION")
        val addresses = geocoder.getFromLocation(lat, lng, 1)
        if (!addresses.isNullOrEmpty()) {
            val address = addresses[0]
            val comuna = address.subLocality
                ?: address.locality
                ?: address.subAdminArea
                ?: address.adminArea
                ?: "Desconocida"
            val region = address.adminArea ?: ""
            val resultado = if (region.isNotBlank() && region != comuna) "$comuna, $region" else comuna
            onUbicacionObtenida(resultado)
            Toast.makeText(context, "Comuna detectada: $resultado", Toast.LENGTH_SHORT).show()
        } else {
            onUbicacionObtenida("Lat: %.4f, Lng: %.4f".format(lat, lng))
        }
    } catch (_: Exception) {
        onUbicacionObtenida("Lat: %.4f, Lng: %.4f".format(lat, lng))
    }
}

@Composable
fun EquipoCard(equipo: Equipo, onEdit: (Equipo) -> Unit, onDelete: (Equipo) -> Unit) {
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
                Text(text = equipo.nombre, style = MaterialTheme.typography.titleMedium)
                Text(text = "TAG: ${equipo.codigo} | Ubic: ${equipo.ubicacion}")
                if (equipo.clienteId > 0) {
                    Text(text = "Cliente: ${equipo.clienteNombre} (ID: ${equipo.clienteId})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
                Text(text = "Estado: ${equipo.estado}", style = MaterialTheme.typography.bodySmall)
            }
            Row {
                TextButton(onClick = { onEdit(equipo) }) {
                    Text("Editar")
                }
                TextButton(onClick = { onDelete(equipo) }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
