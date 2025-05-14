package com.example.mrolnik.screen

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavController
import com.example.mrolnik.viewmodel.LocalSharedViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mrolnik.R
import com.example.mrolnik.model.Repair
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.mrolnik.model.Resource
import com.example.mrolnik.service.VehicleService
import com.example.mrolnik.service.WarehouseService
import kotlinx.datetime.LocalDate

data class resourcesInputField(val label: String, val value: String)

@Composable
fun ResourcesManagementScreen(navController: NavController) {
    val sharedWarehouseViewModel = LocalSharedViewModel.current
    val warehouseState = sharedWarehouseViewModel.selectedWarehouse.collectAsState()

    val currentWarehouse = warehouseState.value

    var showAddRecourcesDialog by remember { mutableStateOf(false) }
    val backIcon = painterResource(R.drawable.baseline_arrow_back)
    val addIcon = painterResource(id = R.drawable.baseline_add)

    var resources by remember { mutableStateOf(emptyList<Resource>()) }

    // LISTA inputFieldów dla dodawania zasobu
    val resourcesInputField = listOf(
        resourcesInputField("Nazwa", ""), resourcesInputField("Ilość", ""), resourcesInputField("Jednostka", "")
    )

    // Lista z wartościami
    var inputResourcesFieldValues by remember { mutableStateOf(resourcesInputField.associateWith { it.value }) }

    val warehouseService = WarehouseService()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
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
                text = "Zarządzanie zasobami",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center
            )

        }

        Button(
            onClick = { showAddRecourcesDialog = true },
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Icon(
                painter = addIcon,
                contentDescription = "ADD",
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Zasoby w ${currentWarehouse?.warehouseName}:", style = MaterialTheme.typography.headlineSmall)
        LaunchedEffect(Unit) {
            resources = warehouseService.getAllResourcesByWarehouseId(currentWarehouse)
        }

        LazyColumn {
            if (resources.isNotEmpty()) {
                items(resources) { resource ->
                    ResourceRow(resource, navController)
                }
            } else {
                items(resources) { resource ->
                    Text(
                        text = "Brak zasobów",
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        if(showAddRecourcesDialog){
            CustomModalDialog(
                onDismiss = { showAddRecourcesDialog = false },
                title = "Dodaj zasoby",
                onConfirm = {
                    val fieldValues = inputResourcesFieldValues.mapKeys { it.key.label }

                    val name = fieldValues["Nazwa"] ?: ""
                    val quantity = fieldValues["Ilość"]?.toDoubleOrNull() ?: 0.0
                    val unitMeasures = fieldValues["Jednostka"] ?: ""

                    val resource = Resource(name, quantity, unitMeasures)

                    CoroutineScope(Dispatchers.IO).launch {
                        warehouseService.assignResourceToWarehouse(resource, currentWarehouse)
                        resources = warehouseService.getAllResourcesByWarehouseId(currentWarehouse)
                    }
                    showAddRecourcesDialog = false
                },
                content = {
                    resourcesInputField.forEach { inputField ->
                        TextField(
                            value = inputResourcesFieldValues[inputField] ?: "",
                            onValueChange = { newValue ->
                                inputResourcesFieldValues = inputResourcesFieldValues.toMutableMap().apply {
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



@Composable
fun ResourceRow(resource: Resource, navController: NavController) {

    var showEditDialog by remember { mutableStateOf(false) }
    var showMoreInformationDialog by remember { mutableStateOf(false) }
    val editIcon = painterResource(id = R.drawable.baseline_edit)
    val deleteIcon = painterResource(id = R.drawable.baseline_delete)
    val infoIcon    = painterResource(id = R.drawable.baseline_info)

    val inputFields = listOf(
        resourcesInputField("Nazwa", resource.name),
        resourcesInputField("Ilość", resource.quantity.toString()),
        resourcesInputField("Jednostka", resource.unitMeasures)

    )

    var inputFieldValues by remember { mutableStateOf(inputFields.associateWith { it.value }) }


    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = resource.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = { showEditDialog = true },
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
                        warehouseService.deleteResource(resource)
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
                onClick = {showMoreInformationDialog = true},
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Icon(
                    painter = infoIcon,
                    contentDescription = "INFO",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (showEditDialog) {
            CustomModalDialog(
                onDismiss = { showEditDialog = false },
                title = "Edytuj: ${resource.name}",
                onConfirm = {
                    // TODO wyswietlic informacje dla uzytkownika o blednym wpisaniu nazwy

                    val fieldValues = inputFieldValues.mapKeys { it.key.label }
                    val name = fieldValues["Nazwa"] ?: ""
                    val quantity = fieldValues["Ilość"]?.toDoubleOrNull() ?: 0.0
                    val unitMeasures = fieldValues["Jednostka"] ?: ""
                    resource.name = name
                    resource.quantity = quantity
                    resource.unitMeasures = unitMeasures

                    CoroutineScope(Dispatchers.IO).launch {
                        warehouseService.updateResource(resource)
                    }

                    showEditDialog = false
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

        if(showMoreInformationDialog) {
            InfoModalDialog(
                onDismiss = { showMoreInformationDialog = false },
                title = "Informacje o zasobie"
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(text = "Nazwa:", fontWeight = FontWeight.Bold)
                    Text(text = resource.name, modifier = Modifier.padding(bottom = 8.dp))

                    Text(text = "Ilość:", fontWeight = FontWeight.Bold)
                    Text(text = resource.quantity.toString(), modifier = Modifier.padding(bottom = 8.dp))

                    Text(text = "Jednoska:", fontWeight = FontWeight.Bold)
                    Text(text = resource.unitMeasures)
                }
            }
        }
    }

}