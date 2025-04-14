package com.vac.wifiacessoapp.modelo

data class RedWifi(
    val ssid: String,
    val nivelSenal: Int,
    val protegida: Boolean,
    val tipoSeguridad: String
)
