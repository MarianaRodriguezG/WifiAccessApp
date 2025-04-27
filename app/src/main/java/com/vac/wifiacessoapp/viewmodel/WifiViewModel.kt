package com.vac.wifiacessoapp.viewmodel

import android.Manifest
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.text.format.Formatter
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vac.wifiacessoapp.modelo.RedWifi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WifiViewModel(application: Application) : AndroidViewModel(application) {

    private val contexto: Context = application.applicationContext
    private val wifiManager = contexto.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val _listaRedes = MutableStateFlow<List<RedWifi>>(emptyList())
    val listaRedes = _listaRedes.asStateFlow()

    private val _escaneando = MutableStateFlow(false)
    val escaneando = _escaneando.asStateFlow()

    private val _errorConexion = MutableStateFlow<String?>(null)
    val errorConexion = _errorConexion.asStateFlow()

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION])
    fun escanearRedes() {
        viewModelScope.launch {
            _escaneando.value = true

            if (!wifiManager.isWifiEnabled) {
                Log.d("WifiViewModel", "Wi-Fi desactivado. No se puede escanear.")
                _escaneando.value = false
                return@launch
            }

            try {
                val inicio = wifiManager.startScan()
                if (inicio) {
                    Log.d("WifiViewModel", "Escaneo iniciado correctamente")
                } else {
                    Log.e("WifiViewModel", "Error al iniciar escaneo de redes")
                }
            } catch (e: SecurityException) {
                Log.e("WifiViewModel", "Permiso insuficiente para escanear redes", e)
                _errorConexion.value = "No se pudo escanear redes por falta de permisos."
                _escaneando.value = false
                return@launch
            }

            delay(2000)

            val resultados = try {
                wifiManager.scanResults
            } catch (e: SecurityException) {
                emptyList()
            }

            val redesConvertidas = resultados.map { scan ->
                RedWifi(
                    ssid = scan.SSID.orEmpty(),
                    nivelSenal = scan.level,
                    protegida = scan.capabilities.contains("WPA") || scan.capabilities.contains("WEP"),
                    tipoSeguridad = scan.capabilities
                )
            }

            _listaRedes.value = redesConvertidas
            Log.d("WifiViewModel", "${redesConvertidas.size} redes encontradas")
            _escaneando.value = false
        }
    }

    fun conectarARedProtegida(ssid: String, password: String) {
        val specifier = android.net.wifi.WifiNetworkSpecifier.Builder()
            .setSsid(ssid)
            .setWpa2Passphrase(password)
            .build()

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .setNetworkSpecifier(specifier)
            .build()

        val connectivityManager = contexto.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        connectivityManager.requestNetwork(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                connectivityManager.bindProcessToNetwork(network)
                Log.d("WifiViewModel", "Conectado a red protegida: $ssid")
                _errorConexion.value = null
            }

            override fun onUnavailable() {
                Log.e("WifiViewModel", "No se pudo conectar a: $ssid")
                _errorConexion.value = "No se pudo conectar a la red. Verifica la contraseña o intenta con otra red."
            }
        })
    }

    fun conectarARedAbierta(ssid: String) {
        val specifier = android.net.wifi.WifiNetworkSpecifier.Builder()
            .setSsid(ssid)
            .build()

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .setNetworkSpecifier(specifier)
            .build()

        val connectivityManager = contexto.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        connectivityManager.requestNetwork(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                connectivityManager.bindProcessToNetwork(network)
                Log.d("WifiViewModel", "Conectado a red abierta: $ssid")
                _errorConexion.value = null
            }

            override fun onUnavailable() {
                Log.e("WifiViewModel", "No se pudo conectar a red abierta: $ssid")
                _errorConexion.value = "No se pudo conectar a la red abierta."
            }
        })
    }

    fun obtenerRedConectada(): RedWifi? {
        val info = wifiManager.connectionInfo ?: return null
        val ssidLimpio = info.ssid.removeSurrounding("\"")
        return RedWifi(
            ssid = ssidLimpio,
            nivelSenal = info.rssi,
            protegida = true,
            tipoSeguridad = "Desconocido"
        )
    }

    fun obtenerInfoConexion(): Map<String, String> {
        val info = wifiManager.connectionInfo ?: return emptyMap()
        return mapOf(
            "IP" to Formatter.formatIpAddress(info.ipAddress),
            "BSSID" to (info.bssid ?: "Desconocido"),
            "Frecuencia" to "${info.frequency} MHz",
            "Velocidad" to "${info.linkSpeed} Mbps"
        )
    }

    fun estaWifiActivado(): Boolean {
        return wifiManager.isWifiEnabled
    }

    fun limpiarError() {
        _errorConexion.value = null
    }
    fun activarWifi(contexto: Context) {
        val intent = android.content.Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)
        intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        contexto.startActivity(intent)
    }
}
