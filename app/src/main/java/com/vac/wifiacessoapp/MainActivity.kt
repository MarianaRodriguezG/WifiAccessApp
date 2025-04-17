package com.vac.wifiacessoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.vac.wifiacessoapp.ui.theme.WifiAcessoAppTheme
import com.vac.wifiacessoapp.vista.AccesoWf

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WifiAcessoAppTheme {
                AccesoWf()
            }
        }
    }
}
