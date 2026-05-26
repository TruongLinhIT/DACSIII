package com.example.dacsiii_v2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.dacsiii_v2.ui.auth.AuthNavGraph
import com.example.dacsiii_v2.ui.theme.DACSIIIV2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DACSIIIV2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AuthNavGraph(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
