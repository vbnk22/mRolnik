package com.example.mrolnik.screen

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ExpandLess
//import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mrolnik.R
import com.example.mrolnik.model.Repair
import com.example.mrolnik.service.VehicleService
import com.example.mrolnik.viewmodel.LocalSharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

data class repairInputField(val label: String, val value: String)

@SuppressLint("NewApi")
@Composable
fun VehicleRepairHistoryScreen(navController: NavController) {
    val sharedVehicleViewModel = LocalSharedViewModel.current
    val vehicleState = sharedVehicleViewModel.selectedVehicle.collectAsState()
    val currentVehicle = vehicleState.value

    val backIcon = painterResource(R.drawable.baseline_arrow_back)
    val addIcon = painterResource(id = R.drawable.baseline_add)

    var showAddRepairDialog by remember { mutableStateOf(false) }

    // LISTA inputFieldów dla dodawania zasobu
    val repairsInputField = listOf(
        repairInputField("Opis", ""),
        repairInputField("Koszt", ""),
        repairInputField("Data naprawy", "")
    )

    // Lista z wartościami
    var inputRepairsFieldValues by remember { mutableStateOf(repairsInputField.associateWith { it.value }) }
    var repairs by remember { mutableStateOf(emptyList<Repair>()) }

    var expandedIndex by remember { mutableStateOf<Int?>(null) }

    val vehicleService = VehicleService()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
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
                text = "Historia napraw",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center
            )

        }

        Button(
            onClick = { showAddRepairDialog = true },
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Icon(
                painter = addIcon,
                contentDescription = "ADD",
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LaunchedEffect(Unit) {
            repairs = vehicleService.getAllRepairsByVehicleId(currentVehicle)
        }

        LazyColumn {
            items(repairs) { repair ->
                RepairItem(
                    repair = repair,
                    isExpanded = expandedIndex == repairs.indexOf(repair),
                    onClick = {
                        expandedIndex = if (expandedIndex == repairs.indexOf(repair)) null else repairs.indexOf(repair)
                    }
                )
            }
        }

        if(showAddRepairDialog){
            CustomModalDialog(
                onDismiss = { showAddRepairDialog = false },
                title = "Dodaj naprawę",
                onConfirm = {
                    val fieldValues = inputRepairsFieldValues.mapKeys { it.key.label }

                    val description = fieldValues["Opis"] ?: ""
                    val cost = fieldValues["Koszt"]?.toDoubleOrNull() ?: 0.0
                    val date = fieldValues["Data naprawy"] ?: ""
//                    val formatter = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")
//                    val parsedDate = java.time.LocalDate.parse(date, formatter)
//                    val repair = Repair(parsedDate.toString(), description, cost)
                    val repair = Repair(LocalDate.parse(date).toString(), description, cost)

                    CoroutineScope(Dispatchers.IO).launch {
                        vehicleService.assignRepairToVehicle(repair, currentVehicle)
                        repairs = vehicleService.getAllRepairsByVehicleId(currentVehicle)
                    }
                    showAddRepairDialog = false
                },
                content = {
                    repairsInputField.forEach { inputField ->
                        TextField(
                            value = inputRepairsFieldValues[inputField] ?: "",
                            onValueChange = { newValue ->
                                inputRepairsFieldValues = inputRepairsFieldValues.toMutableMap().apply {
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
fun RepairItem(
    repair: Repair,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    var showEditRepairDialog by remember { mutableStateOf(false) }

    // LISTA inputFieldów dla dodawania zasobu
    val repairsInputField = listOf(
        repairInputField("Opis", repair.description),
        repairInputField("Koszt", repair.cost.toString()),
        repairInputField("Data naprawy", repair.repairDate.toString())
    )

    // Lista z wartościami
    var inputRepairsFieldValues by remember { mutableStateOf(repairsInputField.associateWith { it.value }) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = repair.description,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                // TODO naprawic te ikonki
//                Icon(
//                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
//                    contentDescription = null
//                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Data naprawy: ${repair.repairDate}")
                Text("Koszt: ${repair.cost} PLN")
                //Text("Opis: ${repair.description}")

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { showEditRepairDialog = true}) {
                        Text("Edytuj")
                    }
                    Button(onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            // TODO odswiezenie listy po usunieciu
                            vehicleService.deleteRepair(repair)
                        }
                    }) {
                        Text("Usuń")
                    }
                }

                if (showEditRepairDialog) {
                    CustomModalDialog(
                        onDismiss = { showEditRepairDialog = false },
                        title = "Edytuj opis: ${repair.description}",
                        onConfirm = {
                            val fieldValues = inputRepairsFieldValues.mapKeys { it.key.label }
                            val description = fieldValues["Opis"] ?: ""
                            val cost = fieldValues["Koszt"]?.toDoubleOrNull() ?: 0.0
                            val date = fieldValues["Data naprawy"] ?: ""
                            repair.description = description
                            repair.cost = cost
                            repair.repairDate = date

                            CoroutineScope(Dispatchers.IO).launch {
                                vehicleService.updateRepair(repair)
                            }

                            showEditRepairDialog = false
                        },
                        content = {
                            repairsInputField.forEach { inputField ->
                                TextField(
                                    value = inputRepairsFieldValues[inputField] ?: "",
                                    onValueChange = { newValue ->
                                        inputRepairsFieldValues = inputRepairsFieldValues.toMutableMap().apply {
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
    }
}
