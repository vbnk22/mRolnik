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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
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
import com.example.mrolnik.model.Cultivation
import com.example.mrolnik.viewmodel.LocalSharedViewModel

data class cultivationInputField(val label: String, val value: String)

@Composable
fun CultivationsScreen(navController: NavController) {
        val sharedFieldViewModel = LocalSharedViewModel.current
        val fieldState = sharedFieldViewModel.selectedField.collectAsState()
        val currentField = fieldState.value // Aktualne pole do ID

        val backIcon = painterResource(R.drawable.baseline_arrow_back)
        val addIcon = painterResource(id = R.drawable.baseline_add)

        var expandedIndex by remember { mutableStateOf<Int?>(null) }


        var showAddCultivationDialog by remember { mutableStateOf(false) }

        val cultivations by remember {
            mutableStateOf(
                listOf(
                    Cultivation("Pomidor", "2024-08-15", 1.5,3.0 ),
                    Cultivation("Ogórek", "2024-07-20", 2.0, 2.5),
                    Cultivation("Papryka", "2024-09-10", 1.8, 2.0),
                    Cultivation("Sałata", "2024-06-30", 1.2, 1.8),
                )
            )
        }

        // TODO: LISTA inputFieldów dla dodawania zasobu
        val cultivationsInputField = listOf(
            cultivationInputField("Nazwa", ""),
            cultivationInputField("Planowany zbiór", ""),
            cultivationInputField("Ilość oprysków", ""),
            cultivationInputField("Ilość nawozu", "")
        )

        // TODO: Lista z wartościami
        var inputCultivationsFieldValues by remember { mutableStateOf(cultivationsInputField.associateWith { it.value }) }

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
                    text = "Zasiewy na polu ${currentField?.fieldName}",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center
                )

            }

            Button(
                onClick = { showAddCultivationDialog = true },
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
                //TODO: Fetchowanie danych o drzewkach
            }

            LazyColumn {
                items(cultivations) { cultivation ->
                    CultivationItem(
                        cultivation = cultivation,
                        isExpanded = expandedIndex == cultivations.indexOf(cultivation),
                        onClick = {
                            expandedIndex =
                                if (expandedIndex == cultivations.indexOf(cultivation)) null else cultivations.indexOf(
                                    cultivation
                                )
                        },
                        navController
                    )
                }
            }

            if (showAddCultivationDialog) {
                CustomModalDialog(
                    onDismiss = { showAddCultivationDialog = false },
                    title = "Dodaj uprawę",
                    onConfirm = {
                        // TODO: zrobić dodawanie naprawy możesz użyć currentVehicle.vehicleId
                        // Jest zrobione tak jak w edycjach za pomocą CustomDialog wiec chyba możesz przekopiować i pozmieniać niektóre elementy
                    },
                    content = {
                        cultivationsInputField.forEach { inputField ->
                            TextField(
                                value = inputCultivationsFieldValues[inputField] ?: "",
                                onValueChange = { newValue ->
                                    inputCultivationsFieldValues =
                                        inputCultivationsFieldValues.toMutableMap().apply {
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
fun CultivationItem(
    cultivation: Cultivation,
    isExpanded: Boolean,
    onClick: () -> Unit,
    navController: NavController
) {
    var showCultivationDialog by remember { mutableStateOf(false) }
    val sharedViewModel = LocalSharedViewModel.current

    // TODO: LISTA inputFieldów dla dodawania zasobu
    val cultivationInputField = listOf(
        cultivationInputField("Nazwa", cultivation.plantName),
        cultivationInputField("Planowany zbiór", cultivation.plannedHarvestDate),
        cultivationInputField("Jakość opryskiwacza", cultivation.usedFertilizerlizerQuantity.toString()),
    )

    // TODO: Lista z wartościami
    var inputcultivationInputField by remember { mutableStateOf(cultivationInputField.associateWith { it.value }) }

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
                    text = cultivation.plantName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Nazwa: ${cultivation.plantName}")
                Text("Data planowanych zbiorów: ${cultivation.plannedHarvestDate}")
                Text("Jakość użytego spryskiwacza: ${cultivation.usedSprayingQuantity}")
                Text("Jakość użytego nawozu: ${cultivation.usedFertilizerlizerQuantity}")

                Spacer(modifier = Modifier.height(8.dp))

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        sharedViewModel.selectCultivation(cultivation)
                        navController.navigate("fertilizerHistory")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sprawdź historię nawozów")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        sharedViewModel.selectCultivation(cultivation)
                        navController.navigate("sprayingHistory")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sprawdź historię oprysków")
                }

                Spacer(modifier = Modifier.height(8.dp))


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = { showCultivationDialog = true }) {
                        Text("Edytuj")
                    }

                    Button(onClick = { /* TODO: Logika usuwania */ }) {
                        Text("Usuń")
                    }
                }
            }
            if (showCultivationDialog) {
                CustomModalDialog(
                    onDismiss = { showCultivationDialog = false },
                    title = "Edytuj: ${cultivation.plantName}",
                    onConfirm = {
                        // TODO implementacja edycji danych

                        showCultivationDialog = false
                    },
                    content = {
                        cultivationInputField.forEach { inputField ->
                            TextField(
                                value = inputcultivationInputField[inputField] ?: "",
                                onValueChange = { newValue ->
                                    inputcultivationInputField =
                                        inputcultivationInputField.toMutableMap().apply {
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


