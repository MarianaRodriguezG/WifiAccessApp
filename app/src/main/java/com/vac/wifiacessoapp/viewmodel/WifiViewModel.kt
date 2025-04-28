package com.vac.wifiacessoapp.viewmodel

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.*
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.provider.Settings
import android.text.format.Formatter
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import com.vac.wifiacessoapp.modelo.RedWifi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class WifiViewModel(application: Application) : AndroidViewModel(application) {

    private val contexto: Context = application.applicationContext
    private val wifiManager = contexto.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val _listaRedes = MutableStateFlow<List<RedWifi>>(emptyList())
    val listaRedes = _listaRedes.asStateFlow()

    private val _escaneando = MutableStateFlow(false)
    val escaneando = _escaneando.asStateFlow()

    private val _wifiActivo = MutableStateFlow(false)
    val wifiActivo = _wifiActivo.asStateFlow()

    private val _ubicacionActiva = MutableStateFlow(false)
    val ubicacionActiva = _ubicacionActiva.asStateFlow()

    private val _redConectada = MutableStateFlow<RedWifi?>(null)
    val redConectada = _redConectada.asStateFlow()

    init {
        actualizarEstadoWifi()
    }

    fun actualizarEstadoWifi() {
        _wifiActivo.value = wifiManager.isWifiEnabled

        val locationManager = contexto.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        _ubicacionActiva.value = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true

        if (_wifiActivo.value) {
            actualizarRedConectada()
        } else {
            _redConectada.value = null
            _listaRedes.value = emptyList()
        }
    }

    fun limpiarRedes() {
        _listaRedes.value = emptyList()
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION])
    fun escanearRedes() {
        if (!wifiManager.isWifiEnabled) {
            _escaneando.value = false
            return
        }

        try {
            wifiManager.startScan()
            _escaneando.value = true

            // No delay aquí porque el RecyclerView se va a actualizar automáticamente
            val resultados = wifiManager.scanResults

            _listaRedes.value = resultados.map { scan ->
                RedWifi(
                    ssid = scan.SSID.orEmpty(),
                    nivelSenal = scan.level,
                    protegida = scan.capabilities.contains("WPA") || scan.capabilities.contains("WEP"),
                    tipoSeguridad = scan.capabilities
                )
            }
        } catch (e: SecurityException) {
            Log.e("WifiViewModel", "Error al escanear redes: ${e.localizedMessage}")
            _escaneando.value = false
        }
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
                connectivityManager.bindProcessToNetwork(network)
                actualizarRedConectada()
            }

            override fun onUnavailable() {
                Log.e("WifiViewModel", "No se pudo conectar a red protegida: $ssid")
            }
        })
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
                connectivityManager.bindProcessToNetwork(network)
                actualizarRedConectada()
            }

            override fun onUnavailable() {
                Log.e("WifiViewModel", "No se pudo conectar a red abierta: $ssid")
            }
        })
    }

    private fun actualizarRedConectada() {
        val info = wifiManager.connectionInfo
        if (info != null && info.networkId != -1) {
            _redConectada.value = RedWifi(
                ssid = info.ssid.removeSurrounding("\""),
                nivelSenal = info.rssi,
                protegida = true,
                tipoSeguridad = "Desconocido"
            )
        }
    }

    fun abrirAjustesWifi(contexto: Context) {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        contexto.startActivity(intent)
    }

    fun abrirAjustesUbicacion(contexto: Context) {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        contexto.startActivity(intent)
    }
}
