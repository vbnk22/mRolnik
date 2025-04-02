package com.example.mrolnik.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.example.mrolnik.model.Animal
import com.example.mrolnik.service.WarehouseService
import com.example.mrolnik.model.Warehouse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun WarehouseManagementScreen() {
    var showForm by remember { mutableStateOf(false) }
    var selectedWarehouse by remember { mutableStateOf<String?>(null) }
    var warehouseName by remember { mutableStateOf("") }
    var warehouseResources by remember { mutableStateOf(mapOf<String, List<String>>()) }
    var warehouses by remember { mutableStateOf(emptyList<Warehouse>()) }

    var newWarehouse by remember { mutableStateOf("") }
    var warehouse: Warehouse
    var warehouseService = WarehouseService()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (selectedWarehouse == null) {
            OutlinedTextField(
                value = warehouseName,
                onValueChange = { warehouseName = it },
                label = { Text("Warehouse Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        if (warehouseName.isNotBlank()) {
                            warehouse = Warehouse(warehouseName)
                            warehouseService.addWarehouse(warehouse)
                            warehouseService.addWarehouseIdToAssociationTable()
                            newWarehouse = ""
                            showForm = false
                        }
                    }
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Add Warehouse")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("List of Warehouses:", style = MaterialTheme.typography.headlineSmall)
            LaunchedEffect(Unit) {
                val fetchedWarehouses = withContext(Dispatchers.IO) {
                    warehouseService.getAllByUserId()
                }
                warehouses = fetchedWarehouses
            }
            LazyColumn {
                if (warehouses.isNotEmpty()) {
                    items(warehouses) { warehouse ->
                        Text(
                            text = warehouse.warehouseName,
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    items(warehouses) { warehouse ->
                        Text(
                            text = "Brak magazynów",
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
//        } else {
//            WarehouseDetailScreen(
//                warehouseName = selectedWarehouse!!,
//                resources = warehouseResources[selectedWarehouse] ?: listOf(),
//                onAddResource = { resource ->
//                    warehouseResources = warehouseResources.toMutableMap().apply {
//                        this[selectedWarehouse!!] = (this[selectedWarehouse] ?: listOf()) + resource
//                    }
//                },
//                onBack = { selectedWarehouse = null }
//            )
//        }
        }
    }}
//
//@Composable
//fun WarehouseDetailScreen(warehouseName: String, resources: List<String>, onAddResource: (String) -> Unit, onBack: () -> Unit) {
//    var showResourceForm by remember { mutableStateOf(false) }
//
//    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
//        Text("Warehouse: $warehouseName", style = MaterialTheme.typography.headlineSmall)
//
//        Spacer(modifier = Modifier.height(16.dp))
//        Text("Resources in this warehouse:", style = MaterialTheme.typography.bodyLarge)
//        Column {
//            resources.forEach { resource ->
//                Text(text = resource, modifier = Modifier.padding(4.dp))
//            }
//        }
//
//        Button(
//            onClick = { showResourceForm = !showResourceForm },
//            modifier = Modifier.padding(top = 8.dp)
//        ) {
//            Text(if (showResourceForm) "Hide Resource Form" else "Add Resource")
//        }
//
//        if (showResourceForm) {
//            ResourceForm(onAddResource)
//        }
//
//        Button(
//            onClick = onBack,
//            modifier = Modifier.padding(top = 8.dp)
//        ) {
//            Text("Back to Warehouses")
//        }
//    }
//}
//
//@Composable
//fun ResourceForm(onAddResource: (String) -> Unit) {
//    var resourceName by remember { mutableStateOf("") }
//    var quantity by remember { mutableStateOf("") }
//    var unitMeasure by remember { mutableStateOf("") }
//
//    Column(modifier = Modifier.padding(16.dp)) {
//        OutlinedTextField(
//            value = resourceName,
//            onValueChange = { resourceName = it },
//            label = { Text("Resource Name") },
//            modifier = Modifier.fillMaxWidth()
//        )
//        OutlinedTextField(
//            value = quantity,
//            onValueChange = { quantity = it },
//            label = { Text("Quantity") },
//            modifier = Modifier.fillMaxWidth()
//        )
//        OutlinedTextField(
//            value = unitMeasure,
//            onValueChange = { unitMeasure = it },
//            label = { Text("Unit Measure") },
//            modifier = Modifier.fillMaxWidth()
//        )
//        Button(
//            onClick = {
//                if (resourceName.isNotBlank() && quantity.isNotBlank() && unitMeasure.isNotBlank()) {
//                    onAddResource("$resourceName - $quantity $unitMeasure")
//                    resourceName = ""
//                    quantity = ""
//                    unitMeasure = ""
//                }
//            },
//            modifier = Modifier.padding(top = 8.dp)
//        ) {
//            Text("Save Resource")
//        }
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun PreviewForms() {
//    WarehouseManagementScreen()
//}