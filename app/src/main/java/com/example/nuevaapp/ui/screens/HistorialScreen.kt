package com.example.nuevaapp.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
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

// Función auxiliar para generar un PDF idéntico al mockup corporativo solicitado (con logo y rayo corregidos)
private fun generarYCompartirPdf(context: Context, medicion: Medicion, equipoNombre: String) {
    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Formato A4 (1 sola página)
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Colores corporativos del mockup
        val goldColor = Color.rgb(245, 197, 24)
        val darkBgColor = Color.rgb(10, 14, 23)
        val cardBgColor = Color.rgb(248, 250, 252)
        val cardBorderColor = Color.rgb(122, 193, 235)
        val valueBoxBgColor = Color.rgb(219, 234, 254)

        val bgPaint = Paint().apply { color = darkBgColor }
        val goldLinePaint = Paint().apply { color = goldColor; strokeWidth = 4f }

        // --- 1. BANNER DE ENCABEZADO ---
        canvas.drawRect(0f, 0f, 595f, 105f, bgPaint)
        canvas.drawLine(0f, 105f, 595f, 105f, goldLinePaint)

        // Logo: Anillas concéntricas y rayo eléctrico central
        val logoX = 50f
        val logoY = 52.5f
        val outerRingPaint = Paint().apply {
            color = Color.rgb(122, 193, 235)
            style = Paint.Style.STROKE
            strokeWidth = 3.5f
            isAntiAlias = true
        }
        val innerRingPaint = Paint().apply {
            color = goldColor
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
        canvas.drawCircle(logoX, logoY, 20f, outerRingPaint)
        canvas.drawCircle(logoX, logoY, 14f, innerRingPaint)

        // Rayo eléctrico dentro del logo
        val boltPath = Path().apply {
            moveTo(logoX - 2f, logoY - 12f)
            lineTo(logoX - 8f, logoY + 1f)
            lineTo(logoX - 1f, logoY + 1f)
            lineTo(logoX - 3f, logoY + 13f)
            lineTo(logoX + 7f, logoY - 2f)
            lineTo(logoX + 1f, logoY - 2f)
            close()
        }
        val boltPaint = Paint().apply {
            color = goldColor
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawPath(boltPath, boltPaint)

        // Texto Induztek (sin espacios extra entre Induz y tek)
        val induztekPaint = Paint().apply { color = Color.WHITE; textSize = 24f; isFakeBoldText = true; isAntiAlias = true }
        val tekPaint = Paint().apply { color = Color.rgb(122, 193, 235); textSize = 24f; isFakeBoldText = true; isAntiAlias = true }
        val subLogoPaint = Paint().apply { color = Color.rgb(160, 171, 192); textSize = 9f; isAntiAlias = true }

        val textStartX = logoX + 32f
        canvas.drawText("Induz", textStartX, logoY + 6f, induztekPaint)
        val induzWidth = induztekPaint.measureText("Induz")
        canvas.drawText("tek", textStartX + induzWidth, logoY + 6f, tekPaint)
        canvas.drawText("SERVICIOS ELÉCTRICOS", textStartX, logoY + 22f, subLogoPaint)

        // Título derecho: REPORTE DE MEDICIÓN
        val headerTitlePaint = Paint().apply { color = Color.WHITE; textSize = 18f; isFakeBoldText = true; isAntiAlias = true }
        val headerMedicionPaint = Paint().apply { color = goldColor; textSize = 22f; isFakeBoldText = true; isAntiAlias = true }

        canvas.drawText("REPORTE DE", 420f, logoY - 4f, headerTitlePaint)
        canvas.drawText("MEDICIÓN", 420f, logoY + 20f, headerMedicionPaint)

        // --- 2. TARJETA PRINCIPAL DE DATOS ---
        val cardLeft = 40f
        val cardTop = 125f
        val cardRight = 555f
        val cardBottom = 385f

        val cardBg = Paint().apply { color = cardBgColor; isAntiAlias = true }
        val cardBorder = Paint().apply { color = cardBorderColor; style = Paint.Style.STROKE; strokeWidth = 2f; isAntiAlias = true }

        canvas.drawRoundRect(cardLeft, cardTop, cardRight, cardBottom, 12f, 12f, cardBg)
        canvas.drawRoundRect(cardLeft, cardTop, cardRight, cardBottom, 12f, 12f, cardBorder)

        val darkText = Paint().apply { color = Color.rgb(20, 25, 35); textSize = 14f; isAntiAlias = true }
        val boldDark = Paint().apply { color = Color.rgb(20, 25, 35); textSize = 14f; isFakeBoldText = true; isAntiAlias = true }

        var textY = cardTop + 35f
        canvas.drawText("ID de Registro: #${medicion.id}", cardLeft + 25f, textY, boldDark)
        textY += 30f
        canvas.drawText("Fecha de Registro: ${medicion.fecha}", cardLeft + 25f, textY, darkText)
        textY += 30f
        canvas.drawText("Equipo: $equipoNombre (ID: ${medicion.equipoId})", cardLeft + 25f, textY, darkText)
        textY += 35f

        // Caja destacada de Valor Medido
        val boxLeft = cardLeft + 20f
        val boxTop = textY - 6f
        val boxRight = cardRight - 20f
        val boxBottom = boxTop + 45f

        val boxBg = Paint().apply { color = valueBoxBgColor; isAntiAlias = true }
        val boxBorder = Paint().apply { color = goldColor; style = Paint.Style.STROKE; strokeWidth = 2f; isAntiAlias = true }

        canvas.drawRoundRect(boxLeft, boxTop, boxRight, boxBottom, 8f, 8f, boxBg)
        canvas.drawRoundRect(boxLeft, boxTop, boxRight, boxBottom, 8f, 8f, boxBorder)

        val valueTextPaint = Paint().apply { color = Color.rgb(15, 23, 42); textSize = 18f; isFakeBoldText = true; isAntiAlias = true }
        canvas.drawText("Valor Medido: ${medicion.valor} ${medicion.unidad}", boxLeft + 20f, boxTop + 29f, valueTextPaint)

        textY = boxBottom + 35f
        canvas.drawText("Técnico Responsable: ${medicion.tecnicoNombre} (ID: ${medicion.usuarioId})", cardLeft + 25f, textY, boldDark)
        textY += 30f
        val obsText = if (medicion.observaciones.isNotBlank()) medicion.observaciones else "Sin observaciones"
        canvas.drawText("Observaciones: $obsText", cardLeft + 25f, textY, darkText)

        // --- 3. SECCIÓN DE FOTOGRAFÍAS ADJUNTAS ---
        val uris = medicion.fotoUri.split(",").filter { it.isNotBlank() }
        var photoY = cardBottom + 30f

        val sectionTitle = Paint().apply { color = Color.rgb(20, 25, 35); textSize = 16f; isFakeBoldText = true; isAntiAlias = true }
        canvas.drawText("Fotografías Adjuntas (${uris.size})", 40f, photoY, sectionTitle)
        photoY += 15f

        if (uris.isNotEmpty()) {
            val startX = 40f
            val colWidth = 245f
            val colGap = 25f
            val rowHeight = 155f
            val rowGap = 20f

            val bitmapPaint = Paint().apply { isFilterBitmap = true; isDither = true }
            val photoBox = Paint().apply { color = Color.WHITE; isAntiAlias = true }
            val photoBorder = Paint().apply { color = cardBorderColor; style = Paint.Style.STROKE; strokeWidth = 2f; isAntiAlias = true }
            val captionPaint = Paint().apply { color = Color.rgb(60, 70, 85); textSize = 11f; isAntiAlias = true; textAlign = Paint.Align.CENTER }

            for ((index, uriStr) in uris.withIndex()) {
                if (index >= 4) break // Máximo 4 fotos por página en cuadrícula limpia
                try {
                    val uri = Uri.parse(uriStr)
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()

                    if (bitmap != null) {
                        val verticalBitmap = rotarVerticalSiEsHorizontal(bitmap)

                        val col = index % 2
                        val row = index / 2
                        val cellX = startX + col * (colWidth + colGap)
                        val cellY = photoY + row * (rowHeight + rowGap)

                        // Marco blanco de la foto
                        canvas.drawRoundRect(cellX, cellY, cellX + colWidth, cellY + rowHeight - 20f, 8f, 8f, photoBox)
                        canvas.drawRoundRect(cellX, cellY, cellX + colWidth, cellY + rowHeight - 20f, 8f, 8f, photoBorder)

                        // Escalado interno de la imagen
                        val imgMaxWidth = colWidth - 16f
                        val imgMaxHeight = rowHeight - 36f
                        val scale = minOf(imgMaxWidth / verticalBitmap.width, imgMaxHeight / verticalBitmap.height)
                        val drawWidth = verticalBitmap.width * scale
                        val drawHeight = verticalBitmap.height * scale

                        val imgX = cellX + (colWidth - drawWidth) / 2f
                        val imgY = cellY + 8f + (imgMaxHeight - drawHeight) / 2f

                        val scaledBitmap = Bitmap.createScaledBitmap(verticalBitmap, drawWidth.toInt(), drawHeight.toInt(), true)
                        canvas.drawBitmap(scaledBitmap, imgX, imgY, bitmapPaint)

                        // Pie de foto
                        canvas.drawText("Registro Fotográfico ${index + 1}", cellX + colWidth / 2f, cellY + rowHeight - 4f, captionPaint)
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
