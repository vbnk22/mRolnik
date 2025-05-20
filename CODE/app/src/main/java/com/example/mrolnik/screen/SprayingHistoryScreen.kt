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
//import androidx.compose.material.icons.Icons
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
import com.example.mrolnik.service.SprayingService
import com.example.mrolnik.viewmodel.LocalSharedViewModel
import com.example.mrolnik.model.Spraying
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

data class sprayingInputField(val label: String, val value: String)
val sprayingService = SprayingService()

@Composable
fun SprayingHistoryScreen(navController: NavController) {
    val sharedFruitTreeViewModel = LocalSharedViewModel.current
    val fruitTreeState = sharedFruitTreeViewModel.selectedFruitTree.collectAsState()
    val currentFruitTree = fruitTreeState.value

    val backIcon = painterResource(R.drawable.baseline_arrow_back)
    val addIcon = painterResource(id = R.drawable.baseline_add)

    var expandedIndex by remember { mutableStateOf<Int?>(null) }

    var showAddSprayingDialog by remember { mutableStateOf(false) }


    var sprayings by remember { mutableStateOf(emptyList<Spraying>()) }

    // LISTA inputFieldów dla dodawania zasobu
    val sprayingsInputField = listOf(
        sprayingInputField("Nazwa oprysku", ""),
        sprayingInputField("Data oprysku", ""),
        sprayingInputField("Ilość oprysków", ""),
    )

    // Lista z wartościami
    var inputSprayingsFieldValues by remember { mutableStateOf(sprayingsInputField.associateWith { it.value }) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)){
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
                text = "${currentFruitTree?.plantName}",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center
            )

        }

        Button(
            onClick = { showAddSprayingDialog = true },
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Icon(
                painter = addIcon,
                contentDescription = "ADD",
                modifier = Modifier.size(24.dp)
            )
        }

        LaunchedEffect(Unit) {
            sprayings = sprayingService.getAllSprayingByFruitTreeId(currentFruitTree)
        }

        LazyColumn {

            items(sprayings) { spraying ->
                SprayingItem(
                    spraying = spraying,
                    isExpanded = expandedIndex == sprayings.indexOf(spraying),
                    onClick = {
                        expandedIndex =
                            if (expandedIndex == sprayings.indexOf(spraying)) null else sprayings.indexOf(
                                spraying
                            )
                    },
                )
            }
        }



        if(showAddSprayingDialog){
            CustomModalDialog(
                onDismiss = { showAddSprayingDialog = false },
                title = "Dodaj oprysk",
                onConfirm = {
                    val fieldValues = inputSprayingsFieldValues.mapKeys { it.key.label }

                    val name = fieldValues["Nazwa oprysku"] ?: ""
                    val date = fieldValues["Data oprysku"] ?: ""
                    val quantity = fieldValues["Ilość oprysków"]?.toDoubleOrNull() ?: 0.0

                    val spraying = Spraying(name, LocalDate.parse(date).toString(), quantity)

                    CoroutineScope(Dispatchers.IO).launch {
                        sprayingService.assignSprayingToFruitTree(spraying, currentFruitTree)
                        sprayings = sprayingService.getAllSprayingByFruitTreeId(currentFruitTree)
                    }
                    showAddSprayingDialog = false
                },
                content = {
                    sprayingsInputField.forEach { inputField ->
                        TextField(
                            value = inputSprayingsFieldValues[inputField] ?: "",
                            onValueChange = { newValue ->
                                inputSprayingsFieldValues = inputSprayingsFieldValues.toMutableMap().apply {
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
fun SprayingCultivationHistoryScreen(navController: NavController){
    val sharedCultivationViewModel = LocalSharedViewModel.current
    val cultivationState = sharedCultivationViewModel.selectedCultivation.collectAsState()
    val currentCultivation = cultivationState.value

    val backIcon = painterResource(R.drawable.baseline_arrow_back)
    val addIcon = painterResource(id = R.drawable.baseline_add)

    var expandedIndex by remember { mutableStateOf<Int?>(null) }

    var showAddSprayingDialog by remember { mutableStateOf(false) }


    var sprayings by remember { mutableStateOf(emptyList<Spraying>()) }

    // LISTA inputFieldów dla dodawania zasobu
    val sprayingsInputField = listOf(
        sprayingInputField("Nazwa oprysku", ""),
        sprayingInputField("Data oprysku", ""),
        sprayingInputField("Ilość oprysków", ""),
    )

    // Lista z wartościami
    var inputSprayingsFieldValues by remember { mutableStateOf(sprayingsInputField.associateWith { it.value }) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)){
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
            onClick = { showAddSprayingDialog = true },
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Icon(
                painter = addIcon,
                contentDescription = "ADD",
                modifier = Modifier.size(24.dp)
            )
        }



        LaunchedEffect(Unit) {
            sprayings = sprayingService.getAllSprayingByCultivationId(currentCultivation)
        }

        LazyColumn {

            items(sprayings) { spraying ->
                SprayingItem(
                    spraying = spraying,
                    isExpanded = expandedIndex == sprayings.indexOf(spraying),
                    onClick = {
                        expandedIndex =
                            if (expandedIndex == sprayings.indexOf(spraying)) null else sprayings.indexOf(
                                spraying
                            )
                    },
                )
            }
        }



        if(showAddSprayingDialog){
            CustomModalDialog(
                onDismiss = { showAddSprayingDialog = false },
                title = "Dodaj oprysk",
                onConfirm = {
                    val fieldValues = inputSprayingsFieldValues.mapKeys { it.key.label }

                    val name = fieldValues["Nazwa oprysku"] ?: ""
                    val date = fieldValues["Data oprysku"] ?: ""
                    val quantity = fieldValues["Ilość oprysków"]?.toDoubleOrNull() ?: 0.0

                    val spraying = Spraying(name, LocalDate.parse(date).toString(), quantity)

                    CoroutineScope(Dispatchers.IO).launch {
                        sprayingService.assignSprayingToCultivation(spraying, currentCultivation)
                        sprayings = sprayingService.getAllSprayingByCultivationId(currentCultivation)
                    }
                    showAddSprayingDialog = false
                },
                content = {
                    sprayingsInputField.forEach { inputField ->
                        TextField(
                            value = inputSprayingsFieldValues[inputField] ?: "",
                            onValueChange = { newValue ->
                                inputSprayingsFieldValues = inputSprayingsFieldValues.toMutableMap().apply {
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
fun SprayingItem(
    spraying: Spraying,
    isExpanded: Boolean,
    onClick: () -> Unit,
) {
    var showEditSprayingDialog by remember { mutableStateOf(false) }


    // LISTA inputFieldów dla dodawania zasobu
    val sprayingsInputField = listOf(
        sprayingInputField("Nazwa oprysku", spraying.sprayingName),
        sprayingInputField("Data oprysku", spraying.sprayingDate),
        sprayingInputField("Ilość oprysków", spraying.sprayingQuantity.toString()),
    )

    // Lista z wartościami
    var inputSprayingsFieldValues by remember { mutableStateOf(sprayingsInputField.associateWith { it.value }) }

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
                    text = spraying.sprayingName,
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
                Text("Nazwa oprysku: ${spraying.sprayingName}")
                Text("Data oprysku: ${spraying.sprayingDate}")
                Text("Jakość oprysku: ${spraying.sprayingQuantity}")

                Spacer(modifier = Modifier.height(8.dp))

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = { showEditSprayingDialog = true }) {
                        Text("Edytuj")
                    }

                    Button(onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            // TODO odswiezenie listy po usunieciu
                            sprayingService.deleteSpraying(spraying)
                        }
                    }) {
                        Text("Usuń")
                    }
                }
            }
            if (showEditSprayingDialog) {
                CustomModalDialog(
                    onDismiss = { showEditSprayingDialog = false },
                    title = "Edytuj: ${spraying.sprayingName}",
                    onConfirm = {
                        val fieldValues = inputSprayingsFieldValues.mapKeys { it.key.label }
                        val name = fieldValues["Nazwa oprysku"] ?: ""
                        val date = fieldValues["Data oprysku"] ?: ""
                        val quantity = fieldValues["Ilość oprysków"]?.toDoubleOrNull() ?: 0.0
                        spraying.sprayingName = name
                        spraying.sprayingDate = date
                        spraying.sprayingQuantity = quantity

                        CoroutineScope(Dispatchers.IO).launch {
                            sprayingService.updateSpraying(spraying)
                        }

                        showEditSprayingDialog = false },
                    content = {
                        sprayingsInputField.forEach { inputField ->
                            TextField(
                                value = inputSprayingsFieldValues[inputField] ?: "",
                                onValueChange = { newValue ->
                                    inputSprayingsFieldValues = inputSprayingsFieldValues.toMutableMap().apply {
                                        this[inputField] = newValue
                                    } },
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