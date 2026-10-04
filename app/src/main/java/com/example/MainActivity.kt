package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.MediBridgeApp
import com.example.ui.theme.MediBridgeTheme
import com.example.ui.viewmodel.MediBridgeViewModel
import com.example.ui.viewmodel.MediBridgeViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: MediBridgeViewModel by viewModels {
        val app = application as MediBridgeApplication
        MediBridgeViewModelFactory(app.repository, app.aiService)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MediBridgeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MediBridgeApp(viewModel = viewModel)
                }
            }
        }
    }
}
