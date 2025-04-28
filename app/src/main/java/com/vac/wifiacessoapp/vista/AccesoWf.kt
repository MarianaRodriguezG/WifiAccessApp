package com.vac.wifiacessoapp.vista

import android.annotation.SuppressLint
import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.vac.wifiacessoapp.viewmodel.WifiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun AccesoWf(wifiViewModel: WifiViewModel) {
    val listaRedes by wifiViewModel.listaRedes.collectAsState()
    val escaneando by wifiViewModel.escaneando.collectAsState()
    val wifiActivo by wifiViewModel.wifiActivo.collectAsState()
    val redConectada by wifiViewModel.redConectada.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val contexto = LocalContext.current

    var mostrarDialogoContrasena by remember { mutableStateOf(false) }
    var redSeleccionada by remember { mutableStateOf<com.vac.wifiacessoapp.modelo.RedWifi?>(null) }
    var contrasena by remember { mutableStateOf("") }

    val permisosLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            wifiViewModel.escanearRedes()
        }
    }

    LaunchedEffect(wifiActivo) {
        if (wifiActivo) {
            wifiViewModel.escanearRedes()
        } else {
            wifiViewModel.limpiarRedes()
        }
    }

    if (!wifiActivo) {
        LaunchedEffect(snackbarHostState) {
            snackbarHostState.showSnackbar("Activa el Wi-Fi para escanear redes")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    if (!wifiActivo) {
                        wifiViewModel.abrirAjustesWifi(contexto)
                    } else {
                        permisosLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = if (wifiActivo) "Escanear redes" else "Activar Wi-Fi")
            }

            Spacer(modifier = Modifier.height(16.dp))

            redConectada?.let { red ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Conectado a:", style = MaterialTheme.typography.bodyMedium)
                        Text(text = red.ssid, style = MaterialTheme.typography.titleMedium)
                        Text(text = "Señal: ${red.nivelSenal} dBm", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (escaneando) {
                Text(text = "Buscando redes...")
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(listaRedes) { red ->
                        TarjetaRed(
                            red = red,
                            onClick = { redClick ->
                                if (redClick.protegida) {
                                    redSeleccionada = redClick
                                    mostrarDialogoContrasena = true
                                } else {
                                    // Aquí pondrías la conexión automática a red abierta si quieres
                                }
                            }
                        )
                    }
                }
            }

            if (mostrarDialogoContrasena && redSeleccionada != null) {
                AlertDialog(
                    onDismissRequest = { mostrarDialogoContrasena = false },
                    title = { Text(text = "Conectarse a ${redSeleccionada?.ssid}") },
                    text = {
                        TextField(
                            value = contrasena,
                            onValueChange = { contrasena = it },
                            label = { Text("Contraseña") },
                            singleLine = true
                        )
                    },
                    confirmButton = {
                        Button(onClick = {
                            mostrarDialogoContrasena = false
                            // Lógica de conexión usando la contraseña
                            // TODO: conectarARedProtegida(redSeleccionada!!.ssid, contrasena)
                        }) {
                            Text(text = "Conectar")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { mostrarDialogoContrasena = false }) {
                            Text(text = "Cancelar")
                        }
                    }
                )
            }
        }
    }
}
