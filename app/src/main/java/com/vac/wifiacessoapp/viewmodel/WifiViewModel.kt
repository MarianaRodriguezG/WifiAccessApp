package com.vac.wifiacessoapp.viewmodel

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.provider.Settings
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vac.wifiacessoapp.modelo.RedWifi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WifiViewModel(private val context: Context) : ViewModel() {

    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    // Estado de escaneo
    private val _escaneando = MutableStateFlow(false)
    val escaneando: StateFlow<Boolean> = _escaneando

    // Lista de redes disponibles
    private val _listaRedes = MutableStateFlow<List<RedWifi>>(emptyList())
    val listaRedes: StateFlow<List<RedWifi>> = _listaRedes

    // Estado del WiFi
    private val _wifiActivo = MutableStateFlow(wifiManager.isWifiEnabled)
    val wifiActivo: StateFlow<Boolean> = _wifiActivo

    // Red actualmente conectada
    private val _redConectada = MutableStateFlow<RedWifi?>(null)
    val redConectada: StateFlow<RedWifi?> = _redConectada

    // Escanear redes WiFi disponibles
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun escanearRedes() {
        viewModelScope.launch {
            _escaneando.value = true
            val resultados = wifiManager.scanResults ?: emptyList()
            val redes = resultados.map { scanResultToRedWifi(it) }
            _listaRedes.value = redes
            _escaneando.value = false
        }
    }

    // Limpiar la lista de redes cuando el WiFi está apagado
    fun limpiarRedes() {
        _listaRedes.value = emptyList()
    }

    // Método para abrir los ajustes Wi-Fi
    fun abrirAjustesWifi(context: Context) {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    // Actualizar estado del Wi-Fi (hacerlo público)
    fun actualizarEstadoWifi() {
        _wifiActivo.value = wifiManager.isWifiEnabled
        obtenerRedConectada()
    }

    // Obtener la red conectada actual
    fun obtenerRedConectada() {
        val info = wifiManager.connectionInfo
        val ssid = info.ssid?.replace("\"", "") ?: ""
        val nivel = WifiManager.calculateSignalLevel(info.rssi, 100)

        if (ssid.isNotBlank() && ssid != "<unknown ssid>") {
            _redConectada.value = RedWifi(
                ssid = ssid,
                nivelSenal = nivel,
                protegida = true, // Suponemos que si estás conectado, es protegida
                tipoSeguridad = "WPA/WPA2" // Asumimos tipo seguro por defecto
            )
        } else {
            _redConectada.value = null
        }
    }

    // Helper: convertir ScanResult a RedWifi
    private fun scanResultToRedWifi(scan: ScanResult): RedWifi {
        val tipoSeguridad = when {
            scan.capabilities.contains("WPA") -> "WPA"
            scan.capabilities.contains("WEP") -> "WEP"
            else -> "Abierta"
        }
        return RedWifi(
            ssid = scan.SSID ?: "",
            nivelSenal = WifiManager.calculateSignalLevel(scan.level, 100),
            protegida = tipoSeguridad != "Abierta",
            tipoSeguridad = tipoSeguridad
        )
    }
}
