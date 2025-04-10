package com.example.mrolnik.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mrolnik.R
import com.example.mrolnik.model.Vehicle
import com.example.mrolnik.service.VehicleService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun VehicleManagementScreen(navController: NavController) {
    var showForm by remember { mutableStateOf(false) }
    var vehicleName by remember { mutableStateOf("") }
    var vehicleCondition by remember { mutableStateOf("") }
    var vehicle: Vehicle
    var vehicleService = VehicleService()
    var vehicles by remember { mutableStateOf(emptyList<Vehicle>()) }
    val addIcon = painterResource(id = R.drawable.baseline_add)
    val backIcon = painterResource(R.drawable.baseline_arrow_back)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Górny pasek z przyciskiem cofania
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
                text = "Zarządzanie pojazdami",
                style = MaterialTheme.typography.headlineSmall
            )
        }
        Button(onClick = { showForm = true }) {
            Icon(
                painter = addIcon,
                contentDescription = "ADD",
                modifier = Modifier.size(24.dp) // dopasuj rozmiar
            )
        }

        if (showForm) {
            OutlinedTextField(
                value = vehicleName,
                onValueChange = { vehicleName = it },
                label = { Text("Nazwa pojazdu") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            OutlinedTextField(
                value = vehicleCondition,
                onValueChange = { vehicleCondition = it },
                label = { Text("Stan techniczny pojazdu") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        if (vehicleName.isNotBlank() && vehicleCondition.isNotBlank()) {
                            vehicle = Vehicle(vehicleName, vehicleCondition)
                            vehicleService.addVehicle(vehicle)
                            vehicleService.addVehicleIdToAssociationTable()
                            vehicleName = ""
                            vehicleCondition = ""
                            val fetchedVehicles = withContext(Dispatchers.IO) {
                                vehicleService.getAllByUserId()
                            }
                            vehicles = fetchedVehicles
                            showForm = false
                        }
                    }
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Dodaj pojazd")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Twoje pojazdy:", style = MaterialTheme.typography.headlineSmall)
        LaunchedEffect(Unit) {
            val fetchedVehicles = withContext(Dispatchers.IO) {
                vehicleService.getAllByUserId()
            }
            vehicles = fetchedVehicles
        }
        LazyColumn {
            if (vehicles.isNotEmpty()) {
                items(vehicles) { vehicle ->
                    VehicleRow(vehicle)
//                    Text(
//                        text = "${vehicle.vehicleName} - ${vehicle.technicalCondition}",
//                        modifier = Modifier.fillMaxWidth().padding(8.dp),
//                        style = MaterialTheme.typography.bodyLarge
//                    )
                }
            } else {
                //TODO: handle empty
//                items(vehicles) { vehicle ->
//                    Text(
//                        text = "Brak pojazdów",
//                        modifier = Modifier.fillMaxWidth().padding(8.dp),
//                        style = MaterialTheme.typography.bodyLarge
//                    )
//                }
            }
        }
    }
}
@Composable
fun VehicleRow(vehicle: Vehicle) {
    var showDialog by remember { mutableStateOf(false) }

    // Lista pól, które chcemy edytować, oparta na obiekcie Animal
    val inputFields = listOf(
        warehouseInputField("Nazwa", vehicle.vehicleName),
    )

    // Stan dla dynamicznie tworzonych inputów
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
                text = vehicle.vehicleName,
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
                onClick = { /* TODO: Handle delete */ },
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
                title = "Edytuj: ${vehicle.vehicleName}",
                onConfirm = {
                    // TODO: zrobić edycje w bazie danych :> kolejność pól powinna być w zmiennej inputFields a nowe dane w inputFieldValues oraz odpowiednio zrzutować na typ
                    inputFieldValues.forEach { (key, value) ->
                        println(value)
                    }
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
