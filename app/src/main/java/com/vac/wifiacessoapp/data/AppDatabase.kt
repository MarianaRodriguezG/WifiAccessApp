package com.vac.wifiacessoapp.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [RedGuardada::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun redGuardadaDao(): RedGuardadaDao
}
