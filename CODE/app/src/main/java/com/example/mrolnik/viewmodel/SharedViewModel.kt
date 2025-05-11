package com.example.mrolnik.viewmodel

import androidx.lifecycle.ViewModel
import com.example.mrolnik.model.Vehicle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.mrolnik.model.Warehouse

class SharedViewModel : ViewModel() {
    private val _selectedWarehouse = MutableStateFlow<Warehouse?>(null)
    val selectedWarehouse: StateFlow<Warehouse?> = _selectedWarehouse

    private val _selectedVehicle = MutableStateFlow<Vehicle?>(null)
    val selectedVehicle: StateFlow<Vehicle?> = _selectedVehicle

    fun selectWarehouse(warehouse: Warehouse) {
        _selectedWarehouse.value = warehouse
    }

    fun selectVehicle(vehicle: Vehicle) {
        _selectedVehicle.value = vehicle
    }
}