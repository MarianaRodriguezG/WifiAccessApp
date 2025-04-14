// ✅ AccesoWf.kt actualizado
package com.vac.wifiacessoapp.vista

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
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
import kotlinx.coroutines.delay

@Composable
fun AccesoWf() {
    val contexto = LocalContext.current
    val wifiViewModel: WifiViewModel = viewModel()

    var mostrarDialogo by remember { mutableStateOf(false) }
    var redSeleccionada by remember { mutableStateOf<RedWifi?>(null) }
    var contrasena by remember { mutableStateOf("") }

    val listaRedes by wifiViewModel.listaRedes.collectAsState()
    val escaneando by wifiViewModel.escaneando.collectAsState()
    val redConectada by rememberUpdatedState(wifiViewModel.obtenerRedConectada())
    val errorConexion by wifiViewModel.errorConexion.collectAsState()
    var wifiDisponible by remember { mutableStateOf(wifiViewModel.estaWifiActivado()) }

    LaunchedEffect(Unit) {
        while (true) {
            wifiDisponible = wifiViewModel.estaWifiActivado()
            delay(1500)
        }
    }

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

    val lanzadorAjustes = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { }

    if (mostrarDialogo && redSeleccionada != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            confirmButton = {
                TextButton(onClick = {
                    wifiViewModel.conectarARedProtegida(redSeleccionada!!.ssid, contrasena)
                }) {
                    Text("Conectar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialogo = false
                    contrasena = ""
                    wifiViewModel.limpiarError()
                }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Conexión protegida") },
            text = {
                Column {
                    Text("Introduce la contraseña para ${redSeleccionada!!.ssid}")
                    Spacer(modifier = Modifier.height(8.dp))
                    if (errorConexion != null) {
                        Text(
                            text = errorConexion ?: "",
                            color = Color.Red,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
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

        if (!wifiDisponible) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFD32F2F))
                    .padding(12.dp)
            ) {
                Text(
                    text = "Wi-Fi desactivado",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                        lanzadorAjustes.launch(intent)
                    },
                    modifier = Modifier.padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text("Ir a ajustes de Wi-Fi", color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (wifiDisponible) {
            redConectada?.let {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0D47A1))
                        .padding(16.dp)
                ) {
                    Text("Conectado a:", color = Color.LightGray)
                    Text(it.ssid, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Señal: ${it.nivelSenal} dBm", color = Color.White)

                    val infoConexion = wifiViewModel.obtenerInfoConexion()
                    infoConexion.forEach { (clave, valor) ->
                        Text("$clave: $valor", color = Color.White, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            when {
                escaneando -> {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.padding(16.dp))
                }
                listaRedes.isEmpty() -> {
                    Text("No hay redes disponibles", color = Color.White, modifier = Modifier.padding(16.dp))
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
                                Text("SSID: ${red.ssid}", color = Color.White)
                                Text(
                                    text = if (red.protegida) "🔒 Red protegida" else "🔓 Red abierta",
                                    color = Color.LightGray
                                )
                                Text("Nivel de señal: ${red.nivelSenal} dBm", color = Color.White)

                                if (!red.protegida) {
                                    Button(
                                        onClick = { wifiViewModel.conectarARedAbierta(red.ssid) },
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
}
