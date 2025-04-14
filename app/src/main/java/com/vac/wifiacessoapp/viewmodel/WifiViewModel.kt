package com.vac.wifiacessoapp.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vac.wifiacessoapp.modelo.RedWifi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.text.format.Formatter

class WifiViewModel(application: Application) : AndroidViewModel(application) {

    private val contexto: Context = application.applicationContext
    private val wifiManager: WifiManager = contexto.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val _listaRedes = MutableStateFlow<List<RedWifi>>(emptyList())
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

            delay(2000)

            val resultados = wifiManager.scanResults

            val redesConvertidas = resultados.map { scan ->
                RedWifi(
                    ssid = scan.SSID,
                    nivelSenal = scan.level,
                    protegida = scan.capabilities.contains("WPA") || scan.capabilities.contains("WEP"),
                    tipoSeguridad = scan.capabilities
                )
            }

            _listaRedes.value = redesConvertidas

            Log.d("WifiViewModel", "Se encontraron ${redesConvertidas.size} redes Wi-Fi")
            redesConvertidas.forEach {
                Log.d("WifiViewModel", "SSID: ${it.ssid}, Nivel: ${it.nivelSenal}, Protegida: ${it.protegida}")
            }

            _escaneando.value = false
        }
    }

    fun conectarARedAbierta(ssid: String) {
        val specifier = WifiNetworkSpecifier.Builder()
            .setSsid(ssid)
            .build()

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .setNetworkSpecifier(specifier)
            .build()

        val connectivityManager = contexto.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        connectivityManager.requestNetwork(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                connectivityManager.bindProcessToNetwork(network)
                Log.d("WifiViewModel", "Conectado a la red abierta: $ssid")
            }

            override fun onUnavailable() {
                super.onUnavailable()
                Log.e("WifiViewModel", "No se pudo conectar a la red: $ssid")
            }
        })
    }
    fun conectarARedProtegida(ssid: String, password: String) {
        val specifier = WifiNetworkSpecifier.Builder()
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
                super.onAvailable(network)
                connectivityManager.bindProcessToNetwork(network)
                Log.d("WifiViewModel", "Conectado a red protegida: $ssid")
            }

            override fun onUnavailable() {
                super.onUnavailable()
                Log.e("WifiViewModel", "No se pudo conectar a: $ssid")
            }
        })
    }
    fun obtenerRedConectada(): RedWifi? {
        val info = wifiManager.connectionInfo ?: return null
        val ssidLimpio = info.ssid.removeSurrounding("\"")
        val ip = android.text.format.Formatter.formatIpAddress(info.ipAddress)

        Log.d("WifiInfo", """
        SSID: $ssidLimpio
        BSSID: ${info.bssid}
        IP: $ip
        RSSI: ${info.rssi}
        LinkSpeed: ${info.linkSpeed} Mbps
        Frecuencia: ${info.frequency} MHz
    """.trimIndent())

        return RedWifi(
            ssid = ssidLimpio,
            nivelSenal = info.rssi,
            protegida = true,
            tipoSeguridad = "Desconocido" // opcional
        ).apply {
            // Puedes extender el modelo RedWifi para incluir más si gustas
        }
    }
    fun obtenerInfoConexion(): Map<String, String> {
        val info = wifiManager.connectionInfo ?: return emptyMap()

        val ip = android.text.format.Formatter.formatIpAddress(info.ipAddress)
        val bssid = info.bssid ?: "Desconocido"
        val frecuencia = "${info.frequency} MHz"
        val velocidad = "${info.linkSpeed} Mbps"

        return mapOf(
            "IP" to ip,
            "BSSID" to bssid,
            "Frecuencia" to frecuencia,
            "Velocidad" to velocidad
        )
    }





}
