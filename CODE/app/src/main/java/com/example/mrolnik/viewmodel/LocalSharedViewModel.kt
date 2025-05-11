package com.example.mrolnik.viewmodel

import androidx.compose.runtime.staticCompositionLocalOf

val LocalSharedViewModel = staticCompositionLocalOf<SharedViewModel> {
    error("SharedWarehouseViewModel not provided")
}