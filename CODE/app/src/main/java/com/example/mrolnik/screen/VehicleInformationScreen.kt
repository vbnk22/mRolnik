package com.example.mrolnik.screen

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
import androidx.compose.material3.Button
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
import com.example.mrolnik.model.Combustion
import com.example.mrolnik.service.CombustionService
import com.example.mrolnik.viewmodel.LocalSharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

val combustionService = CombustionService()

data class combustionInputField(val label: String, val value: String)

@Composable
fun VehicleInformationScreen(navController: NavController) {
    val sharedVehicleViewModel = LocalSharedViewModel.current
    val vehicleState = sharedVehicleViewModel.selectedVehicle.collectAsState()
    val currentVehicle = vehicleState.value

    val backIcon = painterResource(R.drawable.baseline_arrow_back)
    val addIcon = painterResource(id = R.drawable.baseline_add)

    var showEditInformationDialog by remember { mutableStateOf(false) }
    var showAddInformationDialog by remember { mutableStateOf(false) }
    var vehicleCombustion by remember { mutableStateOf<Combustion?>(null) }
    var refreshTrigger by remember { mutableStateOf(0) }

    // LISTA inputFieldów dla dodawania zasobu
    val combustionsInputField = listOf(
        combustionInputField("Ilość paliwa", ""),
        combustionInputField("Przebieg", ""),
        combustionInputField("Data pomiaru", "")
    )

    // Lista z wartościami
    var inputCombustionsFieldValues by remember { mutableStateOf(combustionsInputField.associateWith { it.value }) }

    fun refreshCombustion() {
        CoroutineScope(Dispatchers.IO).launch {

            withContext(Dispatchers.Main) {
                vehicleCombustion = combustionService.getCombustionByVehicleId(currentVehicle)
            }
        }
    }

    // TODO: tutaj możesz dać przycisk który tworzy dla tego pojazdu rekord w tabeli combusution a potem jeżeli jest to wyświetlić informacje
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
                text = "Dodatkowe informacje",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center
            )

        }

        Spacer(modifier = Modifier.height(16.dp))



        LaunchedEffect(currentVehicle, refreshTrigger) {
            //TODO: DOdaj fetchowanie z tabeli combustion na chwile obecną te dane są na sztywno
            refreshCombustion()
        }

        LazyColumn {
            if(vehicleCombustion != null) {

                    item {
                        if (currentVehicle != null) {
                            Text(
                                text = "Pojazd: ${currentVehicle.vehicleName}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Stan techniczny: ${currentVehicle.technicalCondition}")
                            Text("Ilość paliwa: ${vehicleCombustion!!.amountOfFuel} l")
                            Text("Przebieg: ${vehicleCombustion!!.mileage} km")
                            Text("Data pomiaru: ${vehicleCombustion!!.measurementDate}")
                        } else {
                            Text("Nie wybrano pojazdu.")
                        }
                    }

                    item{
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = {
                                    showEditInformationDialog = true
                                }
                            ) {
                                Text("Edytuj")
                            }

                            Button(
                                onClick = {
                                    // TODO: Obsłuż usuwanie danych
                                    CoroutineScope(Dispatchers.IO).launch {
                                        combustionService.deleteCombustion(vehicleCombustion!!)
                                        withContext(Dispatchers.Main) {
                                            refreshTrigger++
                                        }
                                   }
                                    refreshCombustion()
                                }
                            ) {
                                Text("Usuń")
                            }
                        }
                    }
            }else {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (showAddInformationDialog) {
                                combustionsInputField.forEach { inputField ->
                                    TextField(
                                        value = inputCombustionsFieldValues[inputField] ?: "",
                                        onValueChange = { newValue ->
                                            inputCombustionsFieldValues =
                                                inputCombustionsFieldValues.toMutableMap().apply {
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

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(onClick = {
                                if (showAddInformationDialog) {
                                    val fieldValues =
                                        inputCombustionsFieldValues.mapKeys { it.key.label }

                                    val mileage = fieldValues["Przebieg"]?.toDoubleOrNull() ?: 0.0
                                    val amountOfFuel =
                                        fieldValues["Ilość paliwa"]?.toDoubleOrNull() ?: 0.0
                                    val measurementDate = fieldValues["Data pomiaru"] ?: ""

                                    val combustion = Combustion(measurementDate, amountOfFuel, mileage)

                                    CoroutineScope(Dispatchers.IO).launch {
                                        combustionService.assignVehicleToCombustion(
                                            combustion,
                                            currentVehicle
                                        )
                                        withContext(Dispatchers.Main) {
                                            showAddInformationDialog = false
                                            refreshTrigger++
                                        }
                                    }
                                } else {
                                    showAddInformationDialog = true
                                }
                            }) {
                                Text("Dodaj informację")
                            }
                        }
                    }
            }
        }

            if (showEditInformationDialog) {
            CustomModalDialog(
                onDismiss = { showEditInformationDialog = false },
                title = "Edytuj: ${currentVehicle?.vehicleName}",
                onConfirm = {
                    // TODO: zaimplementuj edycje w bazie danych potem możesz tylko odświeżyć pobranie danych
                    val fieldValues =
                        inputCombustionsFieldValues.mapKeys { it.key.label }

                    val mileage = fieldValues["Przebieg"]?.toDoubleOrNull() ?: 0.0
                    val amountOfFuel =
                        fieldValues["Ilość paliwa"]?.toDoubleOrNull() ?: 0.0
                    val measurementDate = fieldValues["Data pomiaru"] ?: ""

                    vehicleCombustion?.mileage = mileage
                    vehicleCombustion?.measurementDate = measurementDate
                    vehicleCombustion?.amountOfFuel = amountOfFuel

                    CoroutineScope(Dispatchers.IO).launch {
                        combustionService.updateCombustion(vehicleCombustion!!)
                        withContext(Dispatchers.Main) {
                            showEditInformationDialog = false
                            refreshTrigger++
                        }
                    }
                },
                content = {
                    combustionsInputField.forEach { inputField ->
                        TextField(
                            value = inputCombustionsFieldValues[inputField] ?: "",
                            onValueChange = { newValue ->
                                inputCombustionsFieldValues = inputCombustionsFieldValues.toMutableMap().apply {
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