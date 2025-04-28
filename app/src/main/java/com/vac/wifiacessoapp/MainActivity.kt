package com.vac.wifiacessoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.vac.wifiacessoapp.ui.theme.WifiAcessoAppTheme
import com.vac.wifiacessoapp.viewmodel.WifiViewModel
import com.vac.wifiacessoapp.vista.AccesoWf

class MainActivity : ComponentActivity() {

    private val wifiViewModel: WifiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WifiAcessoAppTheme {
                AccesoWf(viewModel = wifiViewModel)
            }
        }
    }
}
