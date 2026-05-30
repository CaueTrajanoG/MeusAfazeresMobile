package com.example.meusafazeres

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.meusafazeres.ui.navigation.NavServiceLogin
import com.example.meusafazeres.ui.theme.MeusAfazeresTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContent {
            MeusAfazeresTheme {
                NavServiceLogin()
            }
        }
    }
}