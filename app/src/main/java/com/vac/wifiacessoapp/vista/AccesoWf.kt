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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.vac.wifiacessoapp.viewmodel.WifiViewModel


@Composable
fun AccesoWf() {
    val contexto = LocalContext.current
    val wifiViewModel: WifiViewModel = viewModel()

    val listaRedes by wifiViewModel.listaRedes.collectAsState()
    val escaneando by wifiViewModel.escaneando.collectAsState()

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

        if (escaneando) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.padding(16.dp))
        } else if (listaRedes.isEmpty()) {
            Text(
                text = "No hay redes disponibles",
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn {
                items(listaRedes) { red ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(Color(0xFF0D47A1))
                            .padding(12.dp)
                    ) {
                        Text(text = "SSID: ${red.SSID}", color = Color.White)
                        Text(
                            text = if (red.capabilities.contains("WPA") || red.capabilities.contains("WEP"))
                                "🔒 Red protegida" else "🔓 Red abierta",
                            color = Color.LightGray
                        )
                        Text(text = "Nivel de señal: ${red.level} dBm", color = Color.White)
                    }
                }
            }
        }
    }
}
