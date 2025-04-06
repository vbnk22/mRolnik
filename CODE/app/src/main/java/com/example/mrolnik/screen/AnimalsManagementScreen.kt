package com.example.mrolnik.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mrolnik.model.Animal
import com.example.mrolnik.service.AnimalService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AnimalsManagementScreen(navController: NavController) {
    var showForm by remember { mutableStateOf(false) }
    var newAnimal by remember { mutableStateOf("") }
    var newAnimalCount by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    var animal: Animal
    var animalService = AnimalService()
    var animals by remember { mutableStateOf(emptyList<Animal>()) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = { showForm = true }) {
            Text("Add Animal")
        }

        if (showForm) {
            OutlinedTextField(
                value = newAnimal,
                onValueChange = { newAnimal = it },
                label = { Text("Animal Name") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            OutlinedTextField(
                value = newAnimalCount,
                onValueChange = { newAnimalCount = it.filter { it.isDigit() } },
                label = { Text("Animal Count") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        if (newAnimal.isNotBlank() && newAnimalCount.isNotBlank()) {
                            animal = Animal(newAnimal,newAnimalCount.toInt())
                            animalService.addAnimal(animal)
                            animalService.addAnimalIdToAssociationTable()
                            val fetchedAnimals = withContext(Dispatchers.IO) {
                                animalService.getAllByUserId()
                            }
                            animals = fetchedAnimals
                            newAnimal = ""
                            newAnimalCount = ""
                            showForm = false
                        }
                    }
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Save Animal")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Animal List:", style = MaterialTheme.typography.headlineSmall)
        LaunchedEffect(Unit) {
            val fetchedAnimals = withContext(Dispatchers.IO) {
                animalService.getAllByUserId()
            }
            animals = fetchedAnimals
        }
        LazyColumn {
            if (animals.isNotEmpty()) {
                items(animals) { animal ->
                    AnimalRow(animal = animal)
                }
            } else {
                item {
                    Text(
                        text = "Brak zwierząt",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
    SnackbarHost(hostState = snackbarHostState, modifier = Modifier.fillMaxSize())
}

data class animalInputField(val label: String, val value: String)

@Composable
fun AnimalRow(animal: Animal) {
    var showDialog by remember { mutableStateOf(false) }

    // Lista pól, które chcemy edytować, oparta na obiekcie Animal
    val inputFields = listOf(
        animalInputField("Gatunek", animal.species),
        animalInputField("Liczba zwierząt", animal.numberOfAnimals.toString()),
    )

    // Stan dla dynamicznie tworzonych inputów
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
                text = "${animal.species} - ${animal.numberOfAnimals}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = { showDialog = true },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Edit")
            }
            Button(
                onClick = { /* TODO: Handle delete */ },
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Text("Delete")
            }
        }

        if (showDialog) {
            CustomModalDialog(
                onDismiss = { showDialog = false },
                title = "Edytuj: ${animal.species}",
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
