package com.vac.wifiacessoapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface RedGuardadaDao {

    @Insert
    suspend fun insertarRed(red: RedGuardada)

    @Query("SELECT * FROM redes_guardadas ORDER BY fechaConexion DESC")
    fun obtenerTodas(): Flow<List<RedGuardada>>

    @Delete
    suspend fun eliminarRed(red: RedGuardada)

    @Query("DELETE FROM redes_guardadas")
    suspend fun eliminarTodas()
}
