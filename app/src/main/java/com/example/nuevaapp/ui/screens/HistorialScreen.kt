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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.example.nuevaapp.model.AppDatabase
import com.example.nuevaapp.model.Equipo
import com.example.nuevaapp.model.Medicion
import com.example.nuevaapp.viewmodel.HistorialViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream

@Composable
fun HistorialScreen(
    viewModel: HistorialViewModel,
    userRole: com.example.nuevaapp.viewmodel.UserRole? = null,
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
            val effectiveRole = userRole ?: state.userRole
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.mediciones, key = { it.id }) { medicion ->
                    HistorialItem(
                        medicion = medicion,
                        equipos = state.equipos,
                        userRole = effectiveRole,
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
    userRole: com.example.nuevaapp.viewmodel.UserRole? = null,
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
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
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
                        text = "Cliente: ${eq?.clienteNombre ?: "N/A"} (ID: ${eq?.clienteId ?: 0})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
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
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Obs: ${medicion.observaciones}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = { generarYCompartirPdf(context, medicion, equipoNombre) }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Generar y Compartir PDF",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    if (userRole == com.example.nuevaapp.viewmodel.UserRole.ADMIN || userRole == com.example.nuevaapp.viewmodel.UserRole.TECNICO) {
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

            if (medicion.fotoUri.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                val uris = medicion.fotoUri.split(",").filter { it.isNotBlank() }.take(4)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uris.forEach { uriStr ->
                        AsyncImage(
                            model = Uri.parse(uriStr),
                            contentDescription = "Miniatura foto",
                            modifier = Modifier
                                .size(56.dp)
                                .padding(2.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
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

// Función auxiliar para dibujar etiquetas a la izquierda en negrita y valores normales a la derecha
private fun drawField(canvas: android.graphics.Canvas, x: Float, y: Float, label: String, value: String, boldPaint: Paint, normalPaint: Paint) {
    canvas.drawText(label, x, y, boldPaint)
    val labelWidth = boldPaint.measureText(label)
    canvas.drawText(value, x + labelWidth, y, normalPaint)
}

// Función auxiliar para generar un PDF de 2 hojas con campos a la izquierda en negrita y valores normales en la hoja 1
private fun generarYCompartirPdf(context: Context, medicion: Medicion, equipoNombre: String) {
    try {
        // Obtener historial de mediciones y datos del equipo desde la BD
        val db = AppDatabase.getDatabase(context)
        val historialEquipo = runBlocking(Dispatchers.IO) {
            db.medicionDao().obtenerMedicionesPorEquipoSync(medicion.equipoId)
        }
        val equipoObj = runBlocking(Dispatchers.IO) {
            db.equipoDao().obtenerEquipoPorId(medicion.equipoId)
        }
        val equipoUbicacion = equipoObj?.ubicacion ?: "N/A"

        // Calcular diferencia con la medida anterior
        val currentIndex = historialEquipo.indexOfFirst { it.id == medicion.id }
        val diferenciaText = if (currentIndex != -1 && currentIndex + 1 < historialEquipo.size) {
            val anterior = historialEquipo[currentIndex + 1]
            val diff = medicion.valor - anterior.valor
            val sign = if (diff >= 0) "+" else ""
            "$sign$diff ${medicion.unidad} (Anterior: ${anterior.valor} ${medicion.unidad})"
        } else {
            "N/A (Primera medición)"
        }

        val pdfDocument = PdfDocument()

        // ==========================================
        // HOJA 1: REPORTE PRINCIPAL
        // ==========================================
        val pageInfo1 = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page1 = pdfDocument.startPage(pageInfo1)
        val canvas1 = page1.canvas

        // Colores corporativos del mockup
        val goldColor = Color.rgb(245, 197, 24)
        val darkBgColor = Color.rgb(10, 14, 23)
        val cardBgColor = Color.rgb(248, 250, 252)
        val cardBorderColor = Color.rgb(122, 193, 235)
        val valueBoxBgColor = Color.rgb(219, 234, 254)

        val bgPaint = Paint().apply { color = darkBgColor }
        val goldLinePaint = Paint().apply { color = goldColor; strokeWidth = 4f }

        // --- 1. BANNER DE ENCABEZADO ---
        canvas1.drawRect(0f, 0f, 595f, 105f, bgPaint)
        canvas1.drawLine(0f, 105f, 595f, 105f, goldLinePaint)

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
        canvas1.drawCircle(logoX, logoY, 20f, outerRingPaint)
        canvas1.drawCircle(logoX, logoY, 14f, innerRingPaint)

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
        canvas1.drawPath(boltPath, boltPaint)

        // Texto Induztek
        val induztekPaint = Paint().apply { color = Color.WHITE; textSize = 24f; isFakeBoldText = true; isAntiAlias = true }
        val tekPaint = Paint().apply { color = Color.rgb(122, 193, 235); textSize = 24f; isFakeBoldText = true; isAntiAlias = true }
        val subLogoPaint = Paint().apply { color = Color.rgb(160, 171, 192); textSize = 9f; isAntiAlias = true }

        val textStartX = logoX + 32f
        canvas1.drawText("Induz", textStartX, logoY + 6f, induztekPaint)
        val induzWidth = induztekPaint.measureText("Induz")
        canvas1.drawText("tek", textStartX + induzWidth, logoY + 6f, tekPaint)
        canvas1.drawText("SERVICIOS ELÉCTRICOS", textStartX, logoY + 22f, subLogoPaint)

        // Título derecho: REPORTE DE MEDICIÓN
        val headerTitlePaint = Paint().apply { color = Color.WHITE; textSize = 18f; isFakeBoldText = true; isAntiAlias = true }
        val headerMedicionPaint = Paint().apply { color = goldColor; textSize = 22f; isFakeBoldText = true; isAntiAlias = true }

        canvas1.drawText("REPORTE DE", 420f, logoY - 4f, headerTitlePaint)
        canvas1.drawText("MEDICIÓN", 420f, logoY + 20f, headerMedicionPaint)

        // --- 2. TARJETA PRINCIPAL DE DATOS ---
        val cardLeft = 40f
        val cardTop = 115f
        val cardRight = 555f
        val cardBottom = 420f

        val cardBg = Paint().apply { color = cardBgColor; isAntiAlias = true }
        val cardBorder = Paint().apply { color = cardBorderColor; style = Paint.Style.STROKE; strokeWidth = 2f; isAntiAlias = true }

        canvas1.drawRoundRect(cardLeft, cardTop, cardRight, cardBottom, 12f, 12f, cardBg)
        canvas1.drawRoundRect(cardLeft, cardTop, cardRight, cardBottom, 12f, 12f, cardBorder)

        val darkText = Paint().apply { color = Color.rgb(20, 25, 35); textSize = 13f; isAntiAlias = true }
        val boldDark = Paint().apply { color = Color.rgb(20, 25, 35); textSize = 13f; isFakeBoldText = true; isAntiAlias = true }

        var textY = cardTop + 30f
        drawField(canvas1, cardLeft + 25f, textY, "ID de Registro: ", "#${medicion.id}", boldDark, darkText)
        textY += 25f
        drawField(canvas1, cardLeft + 25f, textY, "Fecha de Registro: ", medicion.fecha, boldDark, darkText)
        textY += 25f
        drawField(canvas1, cardLeft + 25f, textY, "Equipo: ", "$equipoNombre (ID: ${medicion.equipoId})", boldDark, darkText)
        textY += 25f
        drawField(canvas1, cardLeft + 25f, textY, "Ubicación: ", equipoUbicacion, boldDark, darkText)
        textY += 30f

        // Caja destacada de Valor Medido
        val boxLeft = cardLeft + 20f
        val boxTop = textY - 5f
        val boxRight = cardRight - 20f
        val boxBottom = boxTop + 40f

        val boxBg = Paint().apply { color = valueBoxBgColor; isAntiAlias = true }
        val boxBorder = Paint().apply { color = goldColor; style = Paint.Style.STROKE; strokeWidth = 2f; isAntiAlias = true }

        canvas1.drawRoundRect(boxLeft, boxTop, boxRight, boxBottom, 8f, 8f, boxBg)
        canvas1.drawRoundRect(boxLeft, boxTop, boxRight, boxBottom, 8f, 8f, boxBorder)

        val valueTextPaint = Paint().apply { color = Color.rgb(15, 23, 42); textSize = 16f; isFakeBoldText = true; isAntiAlias = true }
        canvas1.drawText("Valor Medido: ${medicion.valor} ${medicion.unidad}", boxLeft + 20f, boxTop + 26f, valueTextPaint)

        textY = boxBottom + 28f
        drawField(canvas1, cardLeft + 25f, textY, "Diferencia última medida: ", diferenciaText, boldDark, darkText)
        textY += 25f
        drawField(canvas1, cardLeft + 25f, textY, "Técnico Responsable: ", "${medicion.tecnicoNombre} (ID: ${medicion.usuarioId})", boldDark, darkText)
        textY += 25f
        val obsText = if (medicion.observaciones.isNotBlank()) medicion.observaciones else "Sin observaciones"
        drawField(canvas1, cardLeft + 25f, textY, "Observaciones: ", obsText, boldDark, darkText)

        // --- 3. SECCIÓN DE FOTOGRAFÍAS ADJUNTAS ---
        val uris = medicion.fotoUri.split(",").filter { it.isNotBlank() }
        var photoY = cardBottom + 25f

        val sectionTitle = Paint().apply { color = Color.rgb(20, 25, 35); textSize = 15f; isFakeBoldText = true; isAntiAlias = true }
        canvas1.drawText("Fotografías Adjuntas (${uris.size})", 40f, photoY, sectionTitle)
        photoY += 12f

        if (uris.isNotEmpty()) {
            val startX = 40f
            val colWidth = 245f
            val colGap = 25f
            val rowHeight = 145f
            val rowGap = 15f

            val bitmapPaint = Paint().apply { isFilterBitmap = true; isDither = true }
            val photoBox = Paint().apply { color = Color.WHITE; isAntiAlias = true }
            val photoBorder = Paint().apply { color = cardBorderColor; style = Paint.Style.STROKE; strokeWidth = 2f; isAntiAlias = true }
            val captionPaint = Paint().apply { color = Color.rgb(60, 70, 85); textSize = 10f; isAntiAlias = true; textAlign = Paint.Align.CENTER }

            for ((index, uriStr) in uris.withIndex()) {
                if (index >= 4) break
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

                        canvas1.drawRoundRect(cellX, cellY, cellX + colWidth, cellY + rowHeight - 18f, 8f, 8f, photoBox)
                        canvas1.drawRoundRect(cellX, cellY, cellX + colWidth, cellY + rowHeight - 18f, 8f, 8f, photoBorder)

                        val imgMaxWidth = colWidth - 16f
                        val imgMaxHeight = rowHeight - 34f
                        val scale = minOf(imgMaxWidth / verticalBitmap.width, imgMaxHeight / verticalBitmap.height)
                        val drawWidth = verticalBitmap.width * scale
                        val drawHeight = verticalBitmap.height * scale

                        val imgX = cellX + (colWidth - drawWidth) / 2f
                        val imgY = cellY + 6f + (imgMaxHeight - drawHeight) / 2f

                        val scaledBitmap = Bitmap.createScaledBitmap(verticalBitmap, drawWidth.toInt(), drawHeight.toInt(), true)
                        canvas1.drawBitmap(scaledBitmap, imgX, imgY, bitmapPaint)

                        canvas1.drawText("Registro Fotográfico ${index + 1}", cellX + colWidth / 2f, cellY + rowHeight - 4f, captionPaint)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        pdfDocument.finishPage(page1)


        // ==========================================
        // HOJA 2: HISTÓRICO DE MEDICIONES DEL EQUIPO
        // ==========================================
        val pageInfo2 = PdfDocument.PageInfo.Builder(595, 842, 2).create()
        val page2 = pdfDocument.startPage(pageInfo2)
        val canvas2 = page2.canvas

        // Banner superior hoja 2
        canvas2.drawRect(0f, 0f, 595f, 75f, bgPaint)
        canvas2.drawLine(0f, 75f, 595f, 75f, goldLinePaint)

        val page2TitlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 16f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val equipoRightPaint = Paint().apply {
            color = goldColor
            textSize = 13f
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }

        canvas2.drawText("HISTÓRICO DE MEDICIONES", 40f, 45f, page2TitlePaint)
        canvas2.drawText("EQUIPO: $equipoNombre", 555f, 45f, equipoRightPaint)

        var y2 = 110f
        canvas2.drawText("Listado de todas las mediciones realizadas al equipo:", 40f, y2, darkText)
        y2 += 25f

        // Tabla Encabezados
        val thBg = Paint().apply { color = valueBoxBgColor; isAntiAlias = true }
        canvas2.drawRect(40f, y2 - 15f, 555f, y2 + 12f, thBg)

        val thPaint = Paint().apply { color = Color.rgb(15, 23, 42); textSize = 11.5f; isFakeBoldText = true; isAntiAlias = true }
        canvas2.drawText("ID", 50f, y2, thPaint)
        canvas2.drawText("Fecha", 90f, y2, thPaint)
        canvas2.drawText("Valor Medido", 265f, y2, thPaint)
        canvas2.drawText("Técnico", 385f, y2, thPaint)
        y2 += 30f

        val rowDark = Paint().apply { color = Color.rgb(20, 25, 35); textSize = 11f; isAntiAlias = true }
        val rowBold = Paint().apply { color = Color.rgb(20, 25, 35); textSize = 11f; isFakeBoldText = true; isAntiAlias = true }

        for (item in historialEquipo) {
            if (y2 > 780f) break
            val isCurrent = item.id == medicion.id
            val rowBg = Paint().apply { color = if (isCurrent) Color.rgb(254, 243, 199) else Color.rgb(248, 250, 252); isAntiAlias = true }
            canvas2.drawRect(40f, y2 - 12f, 555f, y2 + 14f, rowBg)

            canvas2.drawText("#${item.id}", 50f, y2, if (isCurrent) rowBold else rowDark)
            canvas2.drawText(item.fecha, 90f, y2, rowDark)
            canvas2.drawText("${item.valor} ${item.unidad}", 265f, y2, rowBold)
            canvas2.drawText(item.tecnicoNombre.take(25), 385f, y2, rowDark)
            y2 += 30f
        }

        pdfDocument.finishPage(page2)

        // Guardar y compartir
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
