package com.vac.wifiacessoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.vac.wifiacessoapp.ui.theme.WifiAcessoAppTheme
import com.vac.wifiacessoapp.vista.AccesoWf
import com.vac.wifiacessoapp.viewmodel.WifiViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val wifiViewModel: WifiViewModel by viewModels()

        setContent {
            WifiAcessoAppTheme {
                AccesoWf(wifiViewModel = wifiViewModel)
            }
        }
    }
}
