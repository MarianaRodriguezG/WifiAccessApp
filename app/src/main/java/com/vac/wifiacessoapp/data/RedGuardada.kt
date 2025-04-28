package com.vac.wifiacessoapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "redes_guardadas")
data class RedGuardada(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ssid: String,
    val tipoSeguridad: String,
    val nivelSenal: Int,
    val fechaConexion: Long = System.currentTimeMillis()
)
