package com.example.mrolnik

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.example.mrolnik.MainApp
import com.example.mrolnik.viewmodel.LocalSharedViewModel
import com.example.mrolnik.viewmodel.SharedViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]

        setContent {
            CompositionLocalProvider(
                LocalSharedViewModel provides sharedViewModel
            ) {
                MainApp()
            }
        }
    }
}