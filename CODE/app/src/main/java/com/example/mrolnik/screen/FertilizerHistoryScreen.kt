package com.example.mrolnik.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.example.mrolnik.R
import com.example.mrolnik.viewmodel.LocalSharedViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ExpandLess
//import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
//import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class Fertilizer(
    val date: String,
    val name: String,
    val quantity: Double
)

data class fertilizerInputField(val label: String, val value: String)

@Composable
fun FertilizerHistoryScreen(navController: NavController) {
    val cultivationViewModel = LocalSharedViewModel.current
    val selectedCultivationState = cultivationViewModel.selectedCultivation.collectAsState()
    val currentCultivation = selectedCultivationState.value

    val backIcon = painterResource(R.drawable.baseline_arrow_back)
    val addIcon = painterResource(id = R.drawable.baseline_add)

    var expandedIndex by remember { mutableStateOf<Int?>(null) }
    var showAddFertilizerDialog by remember { mutableStateOf(false) }

    val fertilizers by remember {
        mutableStateOf(
            listOf(
                Fertilizer("2024-03-10", "Saletra wapniowa", 5.0),
                Fertilizer("2024-04-15", "Azofoska", 4.5),
                Fertilizer("2024-05-01", "Obornik granulowany", 10.0)
            )
        )
    }

    val fertilizersInputField = listOf(
        fertilizerInputField("Nazwa nawozu", ""),
        fertilizerInputField("Data nawożenia", ""),
        fertilizerInputField("Ilość nawozu", "")
    )

    var inputFertilizerFieldValues by remember {
        mutableStateOf(fertilizersInputField.associateWith { it.value })
    }

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
                text = "${currentCultivation?.plantName}",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center
            )
        }

        Button(
            onClick = { showAddFertilizerDialog = true },
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Icon(
                painter = addIcon,
                contentDescription = "Dodaj nawóz",
                modifier = Modifier.size(24.dp)
            )
        }

        LazyColumn {
            items(fertilizers) { fertilizer ->
                FertilizerItem(
                    fertilizer = fertilizer,
                    isExpanded = expandedIndex == fertilizers.indexOf(fertilizer),
                    onClick = {
                        expandedIndex = if (expandedIndex == fertilizers.indexOf(fertilizer)) null else fertilizers.indexOf(fertilizer)
                    }
                )
            }
        }

        if (showAddFertilizerDialog) {
            CustomModalDialog(
                onDismiss = { showAddFertilizerDialog = false },
                title = "Dodaj nawóz",
                onConfirm = {
                    // TODO: Dodanie nawozu do pola
                },
                content = {
                    fertilizersInputField.forEach { inputField ->
                        TextField(
                            value = inputFertilizerFieldValues[inputField] ?: "",
                            onValueChange = { newValue ->
                                inputFertilizerFieldValues = inputFertilizerFieldValues.toMutableMap().apply {
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
fun FertilizerItem(
    fertilizer: Fertilizer,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }

    val inputFields = listOf(
        fertilizerInputField("Nazwa nawozu", fertilizer.name),
        fertilizerInputField("Data nawożenia", fertilizer.date),
        fertilizerInputField("Ilość nawozu (kg)", fertilizer.quantity.toString())
    )

    var inputValues by remember {
        mutableStateOf(inputFields.associateWith { it.value })
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = fertilizer.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
//                Icon(
//                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
//                    contentDescription = null
//                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Nazwa nawozu: ${fertilizer.name}")
                Text("Data nawożenia: ${fertilizer.date}")
                Text("Ilość nawozu: ${fertilizer.quantity} kg")

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = { showEditDialog = true }) {
                        Text("Edytuj")
                    }
                    Button(onClick = { /* TODO: obsługa usuwania nawozu */ }) {
                        Text("Usuń")
                    }
                }
            }

            if (showEditDialog) {
                CustomModalDialog(
                    onDismiss = { showEditDialog = false },
                    title = "Edytuj: ${fertilizer.name}",
                    onConfirm = {
                        // TODO: obsługa edycji nawozu
                        showEditDialog = false
                    },
                    content = {
                        inputFields.forEach { inputField ->
                            TextField(
                                value = inputValues[inputField] ?: "",
                                onValueChange = { newValue ->
                                    inputValues = inputValues.toMutableMap().apply {
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