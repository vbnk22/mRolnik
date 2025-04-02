package com.example.mrolnik.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
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
                    Text(
                        text = "${animal.species} - ${animal.numberOfAnimals} pcs",
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                items(animals) { animal ->
                    Text(
                        text = "Brak zwierząt",
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
    SnackbarHost(hostState = snackbarHostState, modifier = Modifier.fillMaxSize())
}