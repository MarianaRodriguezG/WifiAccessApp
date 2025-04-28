package com.vac.wifiacessoapp.vista

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vac.wifiacessoapp.modelo.RedWifi

@Composable
// esta clase se utlizó en versiones anteriores a la migracion a ksp y material 3
fun TarjetaRed(red: RedWifi, onClick: (RedWifi) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        onClick = { onClick(red) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "SSID: ${red.ssid}")
            Text(
                text = if (red.protegida) "🔒 Red protegida" else "🔓 Red abierta",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(text = "Nivel de señal: ${red.nivelSenal} dBm")
        }
    }
}
