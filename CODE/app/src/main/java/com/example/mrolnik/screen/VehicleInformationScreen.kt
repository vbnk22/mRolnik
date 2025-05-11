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
import com.example.mrolnik.viewmodel.LocalSharedViewModel

@Composable
fun VehicleInformationScreen(navController: NavController) {
    val sharedVehicleViewModel = LocalSharedViewModel.current
    val vehicleState = sharedVehicleViewModel.selectedVehicle.collectAsState()
    val currentVehicle = vehicleState.value

    val backIcon = painterResource(R.drawable.baseline_arrow_back)
    val addIcon = painterResource(id = R.drawable.baseline_add)

    var showEditInformationDialog by remember { mutableStateOf(false) }


    // Sztywne dane przykładowe
    val fuel = 45.6
    val mileage = 128000
    val measurementDate = "2024-11-20"

    var newFuelValueText by remember { mutableStateOf(fuel.toString()) }
    var newMileageValueText by remember { mutableStateOf(mileage.toString()) }
    var newMeasurementDate by remember { mutableStateOf(measurementDate) }

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



        LaunchedEffect(Unit) {
            //TODO: DOdaj fetchowanie z tabeli combustion na chwile obecną te dane są na sztywno
        }

        LazyColumn {
            item {
                if (currentVehicle != null) {
                    Text(
                        text = "Pojazd: ${currentVehicle.vehicleName}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Stan techniczny: ${currentVehicle.technicalCondition}")
                    Text("Ilość paliwa: ${fuel} l")
                    Text("Przebieg: ${mileage} km")
                    Text("Data pomiaru: $measurementDate")
                } else {
                    Text("Nie wybrano pojazdu.")
                }
            }
        }

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
                }
            ) {
                Text("Usuń")
            }
        }

        if (showEditInformationDialog) {
            CustomModalDialog(
                onDismiss = { showEditInformationDialog = false },
                title = "Edytuj: ${currentVehicle?.vehicleName}",
                onConfirm = {
                    // TODO: zaimplementuj edycje w bazie danych potem możesz tylko odświeżyć pobranie danych
                    showEditInformationDialog = false
                },
                content = {

                    TextField(
                        value = newFuelValueText,
                        onValueChange = { newFuelValueText = it },
                        label = { Text("Ilość paliwa") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        value = newMileageValueText,
                        onValueChange = { newMileageValueText = it },
                        label = { Text("Przebieg") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        value = newMeasurementDate,
                        onValueChange = { newMeasurementDate = it },
                        label = { Text("Data pomiaru") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            )
        }
    }
}