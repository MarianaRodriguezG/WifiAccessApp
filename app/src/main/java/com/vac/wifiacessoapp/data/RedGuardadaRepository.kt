package com.vac.wifiacessoapp.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RedGuardadaRepository(private val dao: RedGuardadaDao) {

    suspend fun guardarRed(red: RedGuardada) {
        withContext(Dispatchers.IO) {
            dao.insertarRed(red)
        }
    }

    suspend fun obtenerHistorial(): List<RedGuardada> {
        return withContext(Dispatchers.IO) {
            dao.obtenerHistorial()
        }
    }

    suspend fun buscarRed(ssid: String): RedGuardada? {
        return withContext(Dispatchers.IO) {
            dao.buscarPorSsid(ssid)
        }
    }

    suspend fun eliminarRed(red: RedGuardada) {
        withContext(Dispatchers.IO) {
            dao.eliminarRed(red)
        }
    }

    suspend fun borrarTodo() {
        withContext(Dispatchers.IO) {
            dao.borrarTodo()
        }
    }
}
