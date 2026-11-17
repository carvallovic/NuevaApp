package com.example.nuevaapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.nuevaapp.model.Usuario
import com.example.nuevaapp.viewmodel.UsuarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionUsuariosScreen(
    viewModel: UsuarioViewModel,
    onVolver: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var perfil by remember { mutableStateOf("TECNICO") } // ADMIN, TECNICO, CLIENTE

    var usuarioEnEdicion by remember { mutableStateOf<Usuario?>(null) }
    var expandedPerfil by remember { mutableStateOf(false) }
    val perfilesDisponibles = listOf("ADMIN", "TECNICO", "CLIENTE")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (usuarioEnEdicion == null) "Gestión de Usuarios" else "Editando Usuario",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Formulario
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre de Usuario") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Dropdown de perfil
        ExposedDropdownMenuBox(
            expanded = expandedPerfil,
            onExpandedChange = { expandedPerfil = !expandedPerfil },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = perfil,
                onValueChange = {},
                readOnly = true,
                label = { Text("Perfil / Rol") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPerfil) },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedPerfil,
                onDismissRequest = { expandedPerfil = false }
            ) {
                perfilesDisponibles.forEach { rol ->
                    DropdownMenuItem(
                        text = { Text(rol) },
                        onClick = {
                            perfil = rol
                            expandedPerfil = false
                        }
                    )
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
                    val idToSave = usuarioEnEdicion?.id ?: 0
                    viewModel.guardarUsuario(idToSave, nombre, correo, contrasena, perfil)
                    if (state.error == null) {
                        nombre = ""
                        correo = ""
                        contrasena = ""
                        perfil = "TECNICO"
                        usuarioEnEdicion = null
                    }
                },
                modifier = Modifier.weight(1.8f)
            ) {
                Text(if (usuarioEnEdicion == null) "Crear Usuario" else "Guardar Cambios")
            }

            if (usuarioEnEdicion != null) {
                OutlinedButton(
                    onClick = {
                        usuarioEnEdicion = null
                        nombre = ""
                        correo = ""
                        contrasena = ""
                        perfil = "TECNICO"
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
                items(state.usuarios, key = { it.id }) { usuario ->
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
                                Text(text = usuario.nombre, style = MaterialTheme.typography.titleMedium)
                                Text(text = "Correo: ${usuario.correo}", style = MaterialTheme.typography.bodyMedium)
                                Text(text = "Perfil: ${usuario.perfil}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                            Row {
                                IconButton(onClick = {
                                    usuarioEnEdicion = usuario
                                    nombre = usuario.nombre
                                    correo = usuario.correo
                                    contrasena = usuario.contrasena
                                    perfil = usuario.perfil
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { viewModel.eliminarUsuario(usuario) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
