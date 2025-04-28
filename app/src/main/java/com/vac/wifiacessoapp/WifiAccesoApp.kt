package com.vac.wifiacessoapp

import android.app.Application
import androidx.room.Room
import com.vac.wifiacessoapp.data.AppDatabase

class WifiAcessoApp : Application() {

    companion object {
        lateinit var database: AppDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "redes_wifi_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}
