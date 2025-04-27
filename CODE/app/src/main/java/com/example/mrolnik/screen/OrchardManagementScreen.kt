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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mrolnik.R
import com.example.mrolnik.model.Orchard
import com.example.mrolnik.service.OrchardService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

var orchardService = OrchardService()

@Composable
fun OrchardManagementScreen(navController: NavController) {
    var showForm by remember { mutableStateOf(false) }
    var selectedOrchard by remember { mutableStateOf<String?>(null) }
    var orchardName by remember { mutableStateOf("") }
    var orchardFruitTrees by remember { mutableStateOf(mapOf<String, List<String>>()) }
    var orchard: Orchard
    var orchards by remember { mutableStateOf(emptyList<Orchard>()) }
    val backIcon = painterResource(R.drawable.baseline_arrow_back)
    val addIcon = painterResource(id = R.drawable.baseline_add)


    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        if (selectedOrchard == null) {//TODO: nie wiem po co to, warto przemyślec czy to potrzebne, else do tego jest zakomentowany
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
                    text = "Zarządzanie sadami",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center
                )
            }
            Button(
                onClick = { showForm = true },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(
                    painter = addIcon,
                    contentDescription = "ADD",
                    modifier = Modifier.size(24.dp)
                )
            }
            if (showForm) {
                OutlinedTextField(
                    value = orchardName,
                    onValueChange = { orchardName = it },
                    label = { Text("Nazwa sadu") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            if (orchardName.isNotBlank()) {
                                orchard = Orchard(orchardName)
                                orchardService.addOrchard(orchard)
                                orchardService.addOrchardIdToAssociationTable()
                                orchardFruitTrees = orchardFruitTrees + (orchardName to listOf())
                                orchardName = ""
                                val fetchedOrchards = withContext(Dispatchers.IO) {
                                    orchardService.getAllByUserId()
                                }
                                orchards = fetchedOrchards
                                showForm = false
                            }
                        }

                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Dodaj sad")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("List of Orchards:", style = MaterialTheme.typography.headlineSmall)
            LaunchedEffect(Unit) {
                val fetchedOrchards = withContext(Dispatchers.IO) {
                    orchardService.getAllByUserId()
                }
                orchards = fetchedOrchards
            }
            LazyColumn {
                if (orchards.isNotEmpty()) {
                    items(orchards) { orchard ->
                        OrchardRow(orchard)
                    }
                } else {
                    //TODO: handle empty
//                    items(orchards) { orchard ->
//                        Text(
//                            text = "Brak sadów",
//                            modifier = Modifier.fillMaxWidth().padding(8.dp),
//                            style = MaterialTheme.typography.bodyLarge
//                        )
//                    }
                }
            }
        }
//         else {
//            OrchardDetailScreen(
//                orchardName = selectedOrchard!!,
//                resources = orchardFruitTrees[selectedOrchard] ?: listOf(),
//                onAddFruitTree = { resource ->
//                    orchardFruitTrees = orchardFruitTrees.toMutableMap().apply {
//                        this[selectedOrchard!!] = (this[selectedOrchard] ?: listOf()) + resource
//                    }
//                },
//                onBack = { selectedOrchard = null }
//            )
//        }
    }
}

data class orchardInputField(val label: String, val value: String)

@Composable
fun OrchardRow(orchard:Orchard) {
    var showDialog by remember { mutableStateOf(false) }

    val inputFields = listOf(
        orchardInputField("Nazwa", orchard.orchardName),
    )

    var inputFieldValues by remember { mutableStateOf(inputFields.associateWith { it.value }) }
    val editIcon = painterResource(id = R.drawable.baseline_edit)
    val deleteIcon = painterResource(id = R.drawable.baseline_delete)
    val infoIcon = painterResource(id = R.drawable.baseline_info)

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = orchard.orchardName,
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
                    // TODO odswiezyc liste sadów po usunięciu
                    CoroutineScope(Dispatchers.IO).launch {
                        orchardService.deleteOrchard(orchard)
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
                title = "Edytuj: ${orchard.orchardName}",
                onConfirm = {
                    // TODO przy debugowaniu wszystko jest dobrze, po odpaleniu aplpikacji wyrzuca błąd
                    //  po wpisaniu pustego znaku
                    var newOrchardName = ""
                    if (orchard.orchardName.isNotBlank()) {
                        newOrchardName =
                            inputFieldValues.getValue(orchardInputField("Nazwa", orchard.orchardName))
                    }
                    if (newOrchardName.isNotBlank()) {
                        orchard.orchardName = newOrchardName
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        orchardService.updateOrchard(orchard)
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
@Composable
fun ListIsEmpty(){
    Text("Nie masz jeszcze żadnych pól")
}
    //@Composable
//fun OrchardDetailScreen(orchardName: String, resources: List<String>, onAddFruitTree: (String) -> Unit, onBack: () -> Unit) {
//    var showFruitTreeForm by remember { mutableStateOf(false) }
//
//    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
//        Text("Orchard: $orchardName", style = MaterialTheme.typography.headlineSmall)
//
//        Spacer(modifier = Modifier.height(16.dp))
//        Text("Fruit Trees in this orchard:", style = MaterialTheme.typography.bodyLarge)
//        Column {
//            resources.forEach { resource ->
//                Text(text = resource, modifier = Modifier.padding(4.dp))
//            }
//        }
//
//        Button(
//            onClick = { showFruitTreeForm = !showFruitTreeForm },
//            modifier = Modifier.padding(top = 8.dp)
//        ) {
//            Text(if (showFruitTreeForm) "Hide Tree Form" else "Add Fruit Tree")
//        }
//
//        if (showFruitTreeForm) {
//            FruitTreeForm(onAddFruitTree)
//        }
//
//        Button(
//            onClick = onBack,
//            modifier = Modifier.padding(top = 8.dp)
//        ) {
//            Text("Back to Orchards")
//        }
//    }
//}

//@Composable
//fun FruitTreeForm(onAddFruitTree: (String) -> Unit) {
//    var plantName by remember { mutableStateOf("") }
//    var harvestDate by remember { mutableStateOf("") }
//    var sprayingQty by remember { mutableStateOf("") }
//
//    Column(modifier = Modifier.padding(16.dp)) {
//        OutlinedTextField(
//            value = plantName,
//            onValueChange = { plantName = it },
//            label = { Text("Plant Name") },
//            modifier = Modifier.fillMaxWidth()
//        )
//        OutlinedTextField(
//            value = harvestDate,
//            onValueChange = { harvestDate = it },
//            label = { Text("Planned Harvest Date") },
//            modifier = Modifier.fillMaxWidth()
//        )
//        OutlinedTextField(
//            value = sprayingQty,
//            onValueChange = { sprayingQty = it },
//            label = { Text("Used Spraying Quantity") },
//            modifier = Modifier.fillMaxWidth()
//        )
//        Button(
//            onClick = {
//                if (plantName.isNotBlank() && harvestDate.isNotBlank() && sprayingQty.isNotBlank()) {
//                    onAddFruitTree("$plantName - $harvestDate - $sprayingQty L")
//                    plantName = ""
//                    harvestDate = ""
//                    sprayingQty = ""
//                }
//            },
//            modifier = Modifier.padding(top = 8.dp)
//        ) {
//            Text("Save Tree Data")
//        }
//    }


//@Preview(showBackground = true)
//@Composable
//fun PreviewOrchardManagement() {
//    OrchardManagementScreen(navController )
//}
