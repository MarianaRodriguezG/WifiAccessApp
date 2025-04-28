package com.vac.wifiacessoapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vac.wifiacessoapp.data.RedGuardadaDao
import com.vac.wifiacessoapp.data.RedGuardada

@Database(
    entities = [RedGuardada::class],
    version = 1,
    exportSchema = false //Agregado para eliminar el warning de exportSchema
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun redGuardadaDao(): RedGuardadaDao
}
