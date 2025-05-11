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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mrolnik.R
import com.example.mrolnik.service.WarehouseService
import com.example.mrolnik.model.Warehouse
import com.example.mrolnik.viewmodel.LocalSharedViewModel
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
    var warehouses by remember { mutableStateOf(emptyList<Warehouse>()) }

    var newWarehouse by remember { mutableStateOf("") }
    var warehouse: Warehouse

    val backIcon = painterResource(R.drawable.baseline_arrow_back)
    val addIcon = painterResource(id = R.drawable.baseline_add)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        if (selectedWarehouse == null) {//TODO: nie wiem po co to, warto przemyślec czy to potrzebne, else do tego jest zakomentowany
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        painter = backIcon,
                        contentDescription = "Wróć",
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "Zarządzanie magazynami",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center
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
                        WarehouseRow(warehouse, navController)
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
    }
}

data class warehouseInputField(val label: String, val value: String)

@Composable
fun WarehouseRow(warehouse: Warehouse, navController: NavController) {
    val sharedViewModel = LocalSharedViewModel.current
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
                onClick = {
                    sharedViewModel.selectWarehouse(warehouse)
                    navController.navigate("resources") },
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
