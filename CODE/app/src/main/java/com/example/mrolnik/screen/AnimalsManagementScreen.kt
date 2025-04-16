package com.example.mrolnik.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mrolnik.R
import com.example.mrolnik.model.Animal
import com.example.mrolnik.service.AnimalService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


var animalService = AnimalService()

@Composable
fun AnimalsManagementScreen(navController: NavController) {
    var showForm by remember { mutableStateOf(false) }
    var newAnimal by remember { mutableStateOf("") }
    var newAnimalCount by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    var animal: Animal
    var animals by remember { mutableStateOf(emptyList<Animal>()) }
    val backIcon = painterResource(R.drawable.baseline_arrow_back)
    val addIcon = painterResource(id = R.drawable.baseline_add)



    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
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
                text = "Zarządzanie zwierzętami",
                style = MaterialTheme.typography.headlineSmall
            )
        }
        Button(onClick = { showForm = true }) {
            Icon(
                painter = addIcon,
                contentDescription = "ADD",
                modifier = Modifier.size(24.dp)
            )
        }

        if (showForm) {
            OutlinedTextField(
                value = newAnimal,
                onValueChange = { newAnimal = it },
                label = { Text("Nazwa zwierzęcia") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            OutlinedTextField(
                value = newAnimalCount,
                onValueChange = { newAnimalCount = it.filter { it.isDigit() } },
                label = { Text("Liczba zwierząt") },
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
                Text("Dodaj zwierzę")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Twoje zwierzęta:", style = MaterialTheme.typography.headlineSmall)
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
                //TODO: handle empty
//                items(animals) { animal ->
//                    Text(
//                        text = "Brak zwierząt",
//                        modifier = Modifier.fillMaxWidth().padding(8.dp),
//                        style = MaterialTheme.typography.bodyLarge
//                    )
//                }
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
    val editIcon = painterResource(id = R.drawable.baseline_edit)
    val deleteIcon = painterResource(id = R.drawable.baseline_delete)

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${animal.species}: ${animal.numberOfAnimals}",
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
                onClick = {
                    // TODO odswiezyc liste zwierząt po usunięciu
                    CoroutineScope(Dispatchers.IO).launch {
                        animalService.deleteAnimal(animal)
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
        }

        if (showDialog) {
            CustomModalDialog(
                onDismiss = { showDialog = false },
                title = "Edytuj: ${animal.species}",
                onConfirm = {
                    // TODO przy debugowaniu wszystko jest dobrze, po odpaleniu aplpikacji wyrzuca błąd
                    //  po wpisaniu pustego znaku
                    val newAnimalSpecies = inputFieldValues.getValue(animalInputField("Gatunek", animal.species))
                    val newNumberOfAnimal = inputFieldValues.getValue(animalInputField("Liczba zwierząt", animal.numberOfAnimals.toString()))
                    if (newAnimalSpecies.isNotBlank()) {
                        animal.species = newAnimalSpecies
                    }
                    if (newNumberOfAnimal.isNotBlank()) {
                        animal.numberOfAnimals = newNumberOfAnimal.toInt()
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        animalService.updateAnimal(animal)
                    }

                    // TODO wyswietlic informacje dla uzytkownika o blednym wpisaniu nazwy
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
