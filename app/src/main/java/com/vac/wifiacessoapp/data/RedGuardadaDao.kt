package com.vac.wifiacessoapp.data

import androidx.room.*
import androidx.room.OnConflictStrategy

@Dao
interface RedGuardadaDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarRed(red: RedGuardada)

    @Query("SELECT * FROM redes_guardadas ORDER BY fechaConexion DESC")
    suspend fun obtenerHistorial(): List<RedGuardada>

    @Query("SELECT * FROM redes_guardadas WHERE ssid = :ssid LIMIT 1")
    suspend fun buscarPorSsid(ssid: String): RedGuardada?

    @Delete
    suspend fun eliminarRed(red: RedGuardada)

    @Query("DELETE FROM redes_guardadas")
    suspend fun borrarTodo()
}
