package com.example.nuevaapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nuevaapp.viewmodel.AuthState
import com.example.nuevaapp.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onLoginSuccess()
            viewModel.resetState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo Corporativo Induztek
        InduztekLogo()

        Spacer(modifier = Modifier.height(40.dp))

        // Campo de Correo con Ícono
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                viewModel.resetState()
            },
            label = { Text("Correo Electrónico") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon", tint = MaterialTheme.colorScheme.primary) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de Contraseña con Ícono
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                viewModel.resetState()
            },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock Icon", tint = MaterialTheme.colorScheme.primary) },
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        )

        // Mostrar mensaje de error si existe
        if (authState is AuthState.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = (authState as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón con estado de carga
        Button(
            onClick = { viewModel.iniciarSesion(email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = Color(0xFF0A0E17)
            ),
            enabled = authState !is AuthState.Loading
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color(0xFF0A0E17),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Iniciar Sesión", fontSize = MaterialTheme.typography.titleMedium.fontSize, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Tarjeta con cuentas de prueba para cada perfil
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Perfiles de prueba (Clave: 123456):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.height(6.dp))
                Text("• ADMIN: admin@induztek.cl", style = MaterialTheme.typography.bodySmall)
                Text("• TECNICO: tecnico@induztek.cl", style = MaterialTheme.typography.bodySmall)
                Text("• CLIENTE: cliente@induztek.cl", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun InduztekLogo() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(68.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
                    val radius = size.width / 2f - 4f

                    drawCircle(
                        color = Color(0xFF7AC1EB),
                        radius = radius,
                        center = center,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
                    )
                    drawCircle(
                        color = Color(0xFFF5C518),
                        radius = radius * 0.75f,
                        center = center,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f)
                    )
                }
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = "Rayo",
                    tint = Color(0xFFF5C518),
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "Induz",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "tek",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7AC1EB)
                    )
                }
                Text(
                    text = "SERVICIOS ELÉCTRICOS",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 2.sp,
                    color = Color(0xFFA0ABC0)
                )
            }
        }
    }
}
