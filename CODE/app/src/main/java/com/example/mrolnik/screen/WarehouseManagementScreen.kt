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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mrolnik.R
import com.example.mrolnik.service.WarehouseService
import com.example.mrolnik.model.Warehouse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

var warehouseService = WarehouseService()

@Composable
fun WarehouseManagementScreen(navController: NavController) {
    var showForm by remember { mutableStateOf(false) }
    var selectedWarehouse by remember { mutableStateOf<String?>(null) }
    var warehouseName by remember { mutableStateOf("") }
    var warehouseResources by remember { mutableStateOf(mapOf<String, List<String>>()) }
    var warehouses by remember { mutableStateOf(emptyList<Warehouse>()) }

    var newWarehouse by remember { mutableStateOf("") }
    var warehouse: Warehouse

    val backIcon = painterResource(R.drawable.baseline_arrow_back)
    val addIcon = painterResource(id = R.drawable.baseline_add)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        if (selectedWarehouse == null) {//TODO: nie wiem po co to, warto przemyślec czy to potrzebne, else do tego jest zakomentowany
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        painter = backIcon,
                        contentDescription = "Wróć",
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Zarządzanie magazynami",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            Button(
                onClick = {
                    showForm = true
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(
                    painter = addIcon,
                    contentDescription = "ADD_WAREHOUSE",
                    modifier = Modifier.size(24.dp)
                )
            }
            if (showForm) {
                OutlinedTextField(
                    value = warehouseName,
                    onValueChange = { warehouseName = it },
                    label = { Text("Nazwa magazynu") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            if (warehouseName.isNotBlank()) {
                                warehouse = Warehouse(warehouseName)
                                warehouseService.addWarehouse(warehouse)
                                warehouseService.addWarehouseIdToAssociationTable()

                                val fetchedWarehouses = withContext(Dispatchers.IO) {
                                    warehouseService.getAllByUserId()
                                }
                                warehouses = fetchedWarehouses

                                warehouseName = ""
                                showForm = false
                            }
                        }
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Dodaj magazyn")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Twoje magazyny:", style = MaterialTheme.typography.headlineSmall)
            LaunchedEffect(Unit) {
                val fetchedWarehouses = withContext(Dispatchers.IO) {
                    warehouseService.getAllByUserId()
                }
                warehouses = fetchedWarehouses
            }
            LazyColumn {
                if (warehouses.isNotEmpty()) {
                    items(warehouses) { warehouse ->
                        WarehouseRow(warehouse)
                    }
                } else {
                    //TODO: handle empty

                    items(warehouses) { warehouse ->
                        Text(
                            text = "Brak magazynów",
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
//        else {
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
}

data class warehouseInputField(val label: String, val value: String)

@Composable
fun WarehouseRow(warehouse: Warehouse) {
    var showDialog by remember { mutableStateOf(false) }

        val inputFields = listOf(
        warehouseInputField("Nazwa", warehouse.warehouseName),
    )

    var inputFieldValues by remember { mutableStateOf(inputFields.associateWith { it.value }) }
    val editIcon = painterResource(id = R.drawable.baseline_edit)
    val deleteIcon = painterResource(id = R.drawable.baseline_delete)
    val infoIcon    = painterResource(id = R.drawable.baseline_info)

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = warehouse.warehouseName,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = { showDialog = true },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    painter = editIcon,
                    contentDescription = "EDIT",
                    modifier = Modifier.size(24.dp)
                )
            }
            Button(
                onClick = {
                    // TODO odswiezyc liste magazynów po usunięciu
                    CoroutineScope(Dispatchers.IO).launch {
                        warehouseService.deleteWarehouse(warehouse)
                    }
                },
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Icon(
                    painter = deleteIcon,
                    contentDescription = "DELETE",
                    modifier = Modifier.size(24.dp)
                )
            }
            Button(
                onClick = { /* TODO: Przejscie do zasobów w magazynie */ },
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Icon(
                    painter = infoIcon,
                    contentDescription = "INFO",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (showDialog) {
            CustomModalDialog(
                onDismiss = { showDialog = false },
                title = "Edytuj: ${warehouse.warehouseName}",
                onConfirm = {
                    // TODO przy debugowaniu wszystko jest dobrze, po odpaleniu aplpikacji wyrzuca błąd
                    //  po wpisaniu pustego znaku
                    var newWarehouseName = ""
                    if (warehouse.warehouseName.isNotBlank()) {
                        newWarehouseName =
                            inputFieldValues.getValue(warehouseInputField("Nazwa", warehouse.warehouseName))
                    }
                    if (newWarehouseName.isNotBlank()) {
                        warehouse.warehouseName = newWarehouseName
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        warehouseService.updateWarehouse(warehouse)
                    }

                    // TODO wyswietlic informacje dla uzytkownika o blednym wpisaniu nazwy
                    showDialog = false
                },
                content = {
                    inputFields.forEach { inputField ->
                        TextField(
                            value = inputFieldValues[inputField] ?: "",
                            onValueChange = { newValue ->
                                inputFieldValues = inputFieldValues.toMutableMap().apply {
                                    this[inputField] = newValue
                                }
                            },
                            label = { Text(inputField.label) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Wpisz ${inputField.label}") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            )
        }
    }
}


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