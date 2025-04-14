package com.vac.wifiacessoapp.vista

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vac.wifiacessoapp.modelo.RedWifi
import com.vac.wifiacessoapp.viewmodel.WifiViewModel

@Composable
fun AccesoWf() {
    val contexto = LocalContext.current
    val wifiViewModel: WifiViewModel = viewModel()

    var mostrarDialogo by remember { mutableStateOf(false) }
    var redSeleccionada by remember { mutableStateOf<RedWifi?>(null) }
    var contrasena by remember { mutableStateOf("") }

    val listaRedes: List<RedWifi> by wifiViewModel.listaRedes.collectAsState()
    val escaneando by wifiViewModel.escaneando.collectAsState()

    val redConectada by rememberUpdatedState(newValue = wifiViewModel.obtenerRedConectada())

    val permisos = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val lanzadorPermisos = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultados ->
        val concedidos = resultados.values.all { it }
        if (concedidos) {
            wifiViewModel.escanearRedes()
        }
    }

    if (mostrarDialogo && redSeleccionada != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            confirmButton = {
                TextButton(onClick = {
                    wifiViewModel.conectarARedProtegida(redSeleccionada!!.ssid, contrasena)
                    mostrarDialogo = false
                    contrasena = ""
                }) {
                    Text("Conectar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialogo = false
                    contrasena = ""
                }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Conexión protegida") },
            text = {
                Column {
                    Text("Introduce la contraseña para ${redSeleccionada!!.ssid}")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = contrasena,
                        onValueChange = { contrasena = it },
                        label = { Text("Contraseña") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1976D2))
            .padding(16.dp)
    ) {
        Text(
            text = "Redes Wi-Fi",
            fontSize = 24.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = {
                val todosConcedidos = permisos.all {
                    ContextCompat.checkSelfPermission(contexto, it) == PackageManager.PERMISSION_GRANTED
                }
                if (todosConcedidos) {
                    wifiViewModel.escanearRedes()
                } else {
                    lanzadorPermisos.launch(permisos)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Escanear redes")
        }

        Spacer(modifier = Modifier.height(16.dp))

        redConectada?.let {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D47A1))
                    .padding(16.dp)
            ) {
                Text(text = "Conectado a:", color = Color.LightGray, fontSize = 14.sp)
                Text(text = it.ssid, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(text = "Señal: ${it.nivelSenal} dBm", color = Color.White, fontSize = 14.sp)

                // Nuevos datos (opcional: puedes crear más campos en RedWifi si los quieres permanentes)
                val infoConexion = wifiViewModel.obtenerInfoConexion()

                infoConexion.forEach { (clave, valor) ->
                    Text(text = "$clave: $valor", color = Color.White, fontSize = 14.sp)
                }

            }

            Spacer(modifier = Modifier.height(16.dp))
        }


        when {
            escaneando -> {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.padding(16.dp))
            }
            listaRedes.isEmpty() -> {
                Text(
                    text = "No hay redes disponibles",
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
            }
            else -> {
                LazyColumn {
                    items(listaRedes) { red ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .background(Color(0xFF0D47A1))
                                .padding(12.dp)
                        ) {
                            Text(text = "SSID: ${red.ssid}", color = Color.White)
                            Text(
                                text = if (red.protegida) "🔒 Red protegida" else "🔓 Red abierta",
                                color = Color.LightGray
                            )
                            Text(text = "Nivel de señal: ${red.nivelSenal} dBm", color = Color.White)

                            if (!red.protegida) {
                                Button(
                                    onClick = {
                                        wifiViewModel.conectarARedAbierta(red.ssid)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                                ) {
                                    Text("Conectar", color = Color.Black)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        redSeleccionada = red
                                        mostrarDialogo = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                                ) {
                                    Text("Conectar", color = Color.Black)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
