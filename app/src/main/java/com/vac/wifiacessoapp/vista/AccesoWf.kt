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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.vac.wifiacessoapp.viewmodel.WifiViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccesoWf(viewModel: WifiViewModel) {
    val contexto = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val wifiActivo by viewModel.wifiActivo.collectAsState()
    val ubicacionActiva by viewModel.ubicacionActiva.collectAsState()
    val listaRedes by viewModel.listaRedes.collectAsState()
    val redConectada by viewModel.redConectada.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var redSeleccionada by remember { mutableStateOf<com.vac.wifiacessoapp.modelo.RedWifi?>(null) }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var contrasenaIngresada by remember { mutableStateOf("") }

    val launcherPermisos = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        if (permisos.values.all { it }) {
            viewModel.actualizarEstadoWifi()
        }
    }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(
            LifecycleEventObserver { _, evento ->
                if (evento == Lifecycle.Event.ON_RESUME) {
                    viewModel.actualizarEstadoWifi()
                }
            }
        )
    }

    LaunchedEffect(wifiActivo, ubicacionActiva) {
        if (!wifiActivo) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Wi-Fi desactivado. Actívalo para escanear.",
                    actionLabel = "Ajustes"
                )
            }
        } else if (!ubicacionActiva) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Ubicación desactivada. Actívala para encontrar redes.",
                    actionLabel = "Ajustes"
                )
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFF1976D2))
                .padding(16.dp)
        ) {
            Text(
                text = "Redes Wi-Fi",
                fontSize = 24.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val permisosNecesarios = arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.NEARBY_WIFI_DEVICES
                    )
                    val faltanPermisos = permisosNecesarios.any {
                        ContextCompat.checkSelfPermission(contexto, it) != PackageManager.PERMISSION_GRANTED
                    }
                    if (faltanPermisos) {
                        launcherPermisos.launch(permisosNecesarios)
                    } else if (!wifiActivo) {
                        viewModel.abrirAjustesWifi(contexto)
                    } else if (!ubicacionActiva) {
                        viewModel.abrirAjustesUbicacion(contexto)
                    } else {
                        viewModel.escanearRedes()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text("Escanear redes", color = Color.Black)
            }

            Spacer(modifier = Modifier.height(16.dp))

            redConectada?.let { red ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0D47A1))
                        .padding(16.dp)
                ) {
                    Text("Conectado a:", color = Color.LightGray)
                    Text(red.ssid, color = Color.White)
                    Text("Señal: ${red.nivelSenal} dBm", color = Color.White)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (listaRedes.isEmpty()) {
                Text(
                    text = "No hay redes disponibles.",
                    color = Color.White
                )
            } else {
                LazyColumn {
                    items(listaRedes) { red ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1565C0))
                                .padding(12.dp)
                        ) {
                            Text(text = "SSID: ${red.ssid}", color = Color.White)
                            Text(
                                text = if (red.protegida) "🔒 Red protegida" else "🔓 Red abierta",
                                color = Color.LightGray
                            )
                            Text(text = "Nivel de señal: ${red.nivelSenal} dBm", color = Color.White)

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    redSeleccionada = red
                                    if (red.protegida) {
                                        mostrarDialogo = true
                                    } else {
                                        viewModel.conectarARedAbierta(red.ssid)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                            ) {
                                Text("Conectar", color = Color.Black)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        if (mostrarDialogo && redSeleccionada != null) {
            AlertDialog(
                onDismissRequest = { mostrarDialogo = false },
                title = { Text("Conectar a ${redSeleccionada!!.ssid}") },
                text = {
                    OutlinedTextField(
                        value = contrasenaIngresada,
                        onValueChange = { contrasenaIngresada = it },
                        label = { Text("Contraseña") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (contrasenaIngresada.isBlank()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Introduce una contraseña válida.")
                            }
                        } else {
                            viewModel.conectarARedProtegida(redSeleccionada!!.ssid, contrasenaIngresada)
                            mostrarDialogo = false
                            contrasenaIngresada = ""
                        }
                    }) {
                        Text("Conectar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        mostrarDialogo = false
                        contrasenaIngresada = ""
                    }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
