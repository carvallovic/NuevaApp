package com.example.nuevaapp.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.nuevaapp.model.Equipo
import com.example.nuevaapp.model.Medicion
import com.example.nuevaapp.viewmodel.HistorialViewModel
import java.io.File
import java.io.FileOutputStream

@Composable
fun HistorialScreen(
    viewModel: HistorialViewModel,
    onEditarRegistro: (Int) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    var medicionAEliminar by remember { mutableStateOf<Medicion?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Text(
            text = "Historial de Mediciones",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.textoBusqueda,
            onValueChange = { viewModel.onBusquedaCambiada(it) },
            label = { Text("Buscar por obs, unidad o nombre/TAG de equipo...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (state.mediciones.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (state.textoBusqueda.isBlank())
                        "No hay mediciones registradas en la base de datos."
                    else
                        "No se encontraron coincidencias.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.mediciones, key = { it.id }) { medicion ->
                    HistorialItem(
                        medicion = medicion,
                        equipos = state.equipos,
                        onDelete = { medicionAEliminar = medicion },
                        onEdit = { onEditarRegistro(medicion.id) }
                    )
                }
            }
        }
    }

    // Modal de confirmación para HistorialScreen
    medicionAEliminar?.let { medicion ->
        AlertDialog(
            onDismissRequest = { medicionAEliminar = null },
            title = { Text("Confirmar eliminación") },
            text = {
                Text("¿Estás seguro de que deseas eliminar la medición de ${medicion.valor} ${medicion.unidad}? Esta acción no se puede deshacer.")
            },
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
fun HistorialItem(
    medicion: Medicion,
    equipos: List<Equipo>,
    onDelete: (Medicion) -> Unit,
    onEdit: () -> Unit
) {
    val context = LocalContext.current
    val eq = equipos.find { it.id == medicion.equipoId }
    val equipoNombre = eq?.nombre ?: "ID: ${medicion.equipoId}"
    val equipoCodigo = eq?.codigo?.let { " (TAG: $it)" } ?: ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${medicion.valor} ${medicion.unidad}",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Equipo: $equipoNombre$equipoCodigo",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Técnico: ${medicion.tecnicoNombre} (ID: ${medicion.usuarioId})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Fecha: ${medicion.fecha}",
                    style = MaterialTheme.typography.labelMedium
                )
                if (medicion.observaciones.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Obs: ${medicion.observaciones}",
                        style = MaterialTheme.typography.bodySmall
                    )
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
                                    .size(60.dp)
                                    .padding(2.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { generarYCompartirPdf(context, medicion, equipoNombre) }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Generar y Compartir PDF",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar registro",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { onDelete(medicion) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar registro",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// Función auxiliar para rotar la imagen a vertical si fue capturada en horizontal
private fun rotarVerticalSiEsHorizontal(bitmap: Bitmap): Bitmap {
    return if (bitmap.width > bitmap.height) {
        val matrix = Matrix().apply {
            postRotate(90f)
        }
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    } else {
        bitmap
    }
}

// Función auxiliar para generar un PDF nativo donde hasta 5 fotos se ajustan perfectamente en 1 sola página con máxima calidad
private fun generarYCompartirPdf(context: Context, medicion: Medicion, equipoNombre: String) {
    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Formato A4 (1 sola página)
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 14f
        }
        val titlePaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 20f
            isFakeBoldText = true
        }
        val headerPaint = Paint().apply {
            color = Color.BLACK
            textSize = 16f
            isFakeBoldText = true
        }

        var y = 50f
        canvas.drawText("REPORTE DE MEDICIÓN - INDUZTEK", 50f, y, titlePaint)
        y += 8f
        paint.color = Color.GRAY
        paint.textSize = 12f
        canvas.drawText("------------------------------------------------------------------------------------------------", 50f, y, paint)
        y += 28f

        paint.color = Color.BLACK
        paint.textSize = 13f
        canvas.drawText("ID de Registro: #${medicion.id}", 50f, y, paint)
        y += 22f
        canvas.drawText("Fecha de Registro: ${medicion.fecha}", 50f, y, paint)
        y += 22f
        canvas.drawText("Equipo: $equipoNombre (ID: ${medicion.equipoId})", 50f, y, paint)
        y += 22f
        canvas.drawText("Valor Medido: ${medicion.valor} ${medicion.unidad}", 50f, y, headerPaint)
        y += 22f
        canvas.drawText("Técnico Responsable: ${medicion.tecnicoNombre} (ID: ${medicion.usuarioId})", 50f, y, paint)
        y += 22f
        if (medicion.observaciones.isNotBlank()) {
            canvas.drawText("Observaciones: ${medicion.observaciones}", 50f, y, paint)
            y += 22f
        }

        // Incluir todas las fotografías adjuntas (máximo 5) en una cuadrícula optimizada para 1 sola página en alta calidad
        if (medicion.fotoUri.isNotBlank()) {
            y += 10f
            canvas.drawText("Fotografías Adjuntas (${medicion.fotoUri.split(",").filter { it.isNotBlank() }.size}):", 50f, y, headerPaint)
            y += 15f

            val uris = medicion.fotoUri.split(",").filter { it.isNotBlank() }
            val startX = 50f
            val colWidth = 220f
            val colGap = 25f
            val rowHeight = 125f
            val rowGap = 12f

            val bitmapPaint = Paint().apply {
                isFilterBitmap = true
                isDither = true
            }

            for ((index, uriStr) in uris.withIndex()) {
                if (index >= 5) break // Máximo 5 fotografías
                try {
                    val uri = Uri.parse(uriStr)
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()

                    if (bitmap != null) {
                        val verticalBitmap = rotarVerticalSiEsHorizontal(bitmap)

                        // Escalar con alta calidad manteniendo proporciones
                        val scale = minOf(colWidth / verticalBitmap.width, rowHeight / verticalBitmap.height)
                        val drawWidth = verticalBitmap.width * scale
                        val drawHeight = verticalBitmap.height * scale

                        val col = index % 2
                        val row = index / 2
                        val cellX = startX + col * (colWidth + colGap) + (colWidth - drawWidth) / 2f
                        val cellY = y + row * (rowHeight + rowGap) + (rowHeight - drawHeight) / 2f

                        val scaledBitmap = Bitmap.createScaledBitmap(verticalBitmap, drawWidth.toInt(), drawHeight.toInt(), true)
                        canvas.drawBitmap(scaledBitmap, cellX, cellY, bitmapPaint)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        pdfDocument.finishPage(page)

        // Formato de nombre solicitado: Reporte de Medicion "Nombre_Equipo" - "Nombre del Tecnico" - Fecha.pdf
        val safeEquipo = equipoNombre.replace(Regex("[\\\\/:*?\"<>|]"), "_")
        val safeTecnico = medicion.tecnicoNombre.replace(Regex("[\\\\/:*?\"<>|]"), "_")
        val safeFecha = medicion.fecha.replace(Regex("[\\\\/:*?\"<>|]"), "-")
        val fileName = "Reporte de Medicion $safeEquipo - $safeTecnico - $safeFecha.pdf"

        val file = File(context.cacheDir, fileName)
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, fileName.substringBefore("."))
            putExtra(Intent.EXTRA_TEXT, "Adjunto reporte oficial de medición para el equipo $equipoNombre.\nValor: ${medicion.valor} ${medicion.unidad}\nTécnico: ${medicion.tecnicoNombre}\nFecha: ${medicion.fecha}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir reporte PDF vía"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
