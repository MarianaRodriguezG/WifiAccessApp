package com.vac.wifiacessoapp.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [RedGuardada::class],
    version = 1,
    exportSchema = false //Agregado para eliminar la advertencia de exportSchema
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun redGuardadaDao(): RedGuardadaDao
}
