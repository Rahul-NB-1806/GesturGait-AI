package com.example.gesturgaitai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.gesturgaitai.core.InferenceEngine
import com.example.gesturgaitai.core.OfflineStorage
import com.example.gesturgaitai.navigation.AppNavigation
import com.example.gesturgaitai.ui.theme.GesturGaitAITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        OfflineStorage.init(this)
        InferenceEngine.initialize(this)
        enableEdgeToEdge()
        setContent {
            GesturGaitAITheme {
                AppNavigation()
            }
        }
    }
}
