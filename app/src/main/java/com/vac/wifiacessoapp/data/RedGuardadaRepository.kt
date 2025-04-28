package com.vac.wifiacessoapp.data

import kotlinx.coroutines.flow.Flow

class RedGuardadaRepository(private val dao: RedGuardadaDao) {

    val redesGuardadas: Flow<List<RedGuardada>> = dao.obtenerTodas()

    suspend fun insertar(red: RedGuardada) {
        dao.insertarRed(red)
    }

    suspend fun eliminar(red: RedGuardada) {
        dao.eliminarRed(red)
    }

    suspend fun eliminarTodas() {
        dao.eliminarTodas()
    }
}
