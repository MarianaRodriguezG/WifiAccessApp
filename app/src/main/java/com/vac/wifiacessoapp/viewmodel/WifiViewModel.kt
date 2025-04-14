package com.vac.wifiacessoapp.viewmodel

import android.app.Application
import android.content.Context
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class WifiViewModel(application: Application) : AndroidViewModel(application) {

    private val contexto: Context = application.applicationContext
    private val wifiManager = contexto.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val _listaRedes = MutableStateFlow<List<ScanResult>>(emptyList())
    val listaRedes = _listaRedes.asStateFlow()

    private val _escaneando = MutableStateFlow(false)
    val escaneando = _escaneando.asStateFlow()

    fun escanearRedes() {
        viewModelScope.launch {
            _escaneando.value = true

            if (!wifiManager.isWifiEnabled) {
                Log.d("WifiViewModel", "Wi-Fi desactivado. Activando...")
                wifiManager.isWifiEnabled = true
            }

            val inicio = wifiManager.startScan()
            if (inicio) {
                Log.d("WifiViewModel", "Escaneo iniciado correctamente")
            } else {
                Log.e("WifiViewModel", "Error al iniciar escaneo de redes")
            }

            // Esperar un poco a que el escaneo devuelva resultados
            delay(2000)

            val resultados = wifiManager.scanResults
            _listaRedes.value = resultados

            Log.d("WifiViewModel", "Se encontraron ${resultados.size} redes Wi-Fi")
            resultados.forEach {
                Log.d("WifiViewModel", "SSID: ${it.SSID}, Nivel: ${it.level}, Seguridad: ${it.capabilities}")
            }

            _escaneando.value = false
        }
    }
}
