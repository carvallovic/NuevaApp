package com.example.nuevaapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuModulosScreen(
    nombreTecnico: String,
    onNavigateToEquipos: () -> Unit,
    onNavigateToRegistro: () -> Unit,
    onNavigateToHistorial: () -> Unit,
    onNavigateToProtocolos: () -> Unit,
    onNavigateToSync: () -> Unit
) {
    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    selected = true,
                    onClick = { /* Ya estamos aquí */ },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1976D2),
                        selectedTextColor = Color(0xFF1976D2),
                        indicatorColor = Color(0xFFE3F2FD)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.MoreHoriz, contentDescription = "Más") },
                    label = { Text("Más") },
                    selected = false,
                    onClick = { /* Futuras configuraciones */ }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            // 1. Encabezado Superior (Logo e Info de Empresa)
            TopHeader()

            // 2. Saludo y Estado de Conexión
            GreetingSection(nombreTecnico)

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Lista de Módulos (Botones)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    ModuloCard(
                        prefix = "EQ",
                        title = "Equipos y ubicaciones",
                        subtitle = "Instalaciones y equipos",
                        colorHex = 0xFF1976D2, // Azul
                        onClick = onNavigateToEquipos
                    )
                }
                item {
                    ModuloCard(
                        prefix = "PR",
                        title = "Registrar prueba",
                        subtitle = "Ingresar mediciones eléctricas",
                        colorHex = 0xFF009688, // Verde azulado (Teal)
                        onClick = onNavigateToRegistro
                    )
                }
                item {
                    ModuloCard(
                        prefix = "HT",
                        title = "Historial y trazabilidad",
                        subtitle = "Consultar resultados anteriores",
                        colorHex = 0xFF5C6BC0, // Morado/Indigo
                        onClick = onNavigateToHistorial
                    )
                }
                item {
                    ModuloCard(
                        prefix = "PDF",
                        title = "Protocolos de prueba",
                        subtitle = "Generar y compartir el PDF",
                        colorHex = 0xFFD87B28, // Naranja oscuro
                        onClick = onNavigateToProtocolos
                    )
                }
                item {
                    ModuloCard(
                        prefix = "SYNC",
                        title = "Sincronización",
                        subtitle = "3 registros pendientes",
                        colorHex = 0xFF2E7D32, // Verde oscuro
                        onClick = onNavigateToSync
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp)) // Espaciador final
                }
            }
        }
    }
}

@Composable
fun TopHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Icono del Rayo (Logo)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1976D2)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "⚡", fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "INDUZTEK",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color(0xFF0D47A1)
                )
                Text(
                    text = "Ingeniería SpA",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
        // Avatar del Técnico
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFE3F2FD)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "T",
                color = Color(0xFF1976D2),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun GreetingSection(nombre: String) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Hola, $nombre",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF101828)
        )
        Text(
            text = "¿Qué deseas hacer?",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Píldora de estado (Sin conexión)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE8F5E9)) // Verde muy claro
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Modo sin conexión disponible",
                color = Color(0xFF2E7D32),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ModuloCard(
    prefix: String,
    title: String,
    subtitle: String,
    colorHex: Long,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)), // Borde sutil gris claro
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cuadro de prefijo con color (Ej: "EQ")
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(colorHex)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = prefix,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Textos centrales
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF101828)
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            // Icono Flecha
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ir a $title",
                tint = Color.Gray
            )
        }
    }
}