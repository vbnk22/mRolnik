package com.example.mrolnik.screen

import android.util.Log
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.example.mrolnik.R
import com.example.mrolnik.model.Animal
import com.example.mrolnik.model.Field
import com.example.mrolnik.model.Orchard
import com.example.mrolnik.service.FieldService
import com.example.mrolnik.viewmodel.LocalSharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

var fieldService = FieldService()

@Composable
fun FieldManagementScreen(navController: NavController) {
    var showForm by remember { mutableStateOf(false) }
    var selectedField by remember { mutableStateOf<String?>(null) }
    var fieldName by remember { mutableStateOf("") }
    var fieldCultivations by remember { mutableStateOf(mapOf<String, List<String>>()) }
    var field: Field
    var fields by remember { mutableStateOf(emptyList<Field>()) }
    val backIcon = painterResource(R.drawable.baseline_arrow_back)
    val addIcon = painterResource(id = R.drawable.baseline_add)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        if (selectedField == null) {//TODO: nie wiem po co to, warto przemyślec czy to potrzebne, else do tego jest zakomentowany
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
                    text = "Zarządzanie polami",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center
                )
            }
            Button(
                onClick = {
                    showForm = true
                },
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
                    value = fieldName,
                    onValueChange = { fieldName = it },
                    label = { Text("Nazwa pola") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            if (fieldName.isNotBlank()) {
                                field = Field(fieldName)
                                fieldService.addField(field)
                                fieldService.addFieldIdToAssociationTable()
                                fieldCultivations = fieldCultivations + (fieldName to listOf())
                                fieldName = ""
                                val fetchedFields = withContext(Dispatchers.IO) {
                                    fieldService.getAllByUserId()
                                }
                                fields = fetchedFields
                                showForm = false
                            }
                        }

                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Dodaj pole")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Twoje pola:", style = MaterialTheme.typography.headlineSmall)
            LaunchedEffect(Unit) {
                val fetchedFields = withContext(Dispatchers.IO) {
                    fieldService.getAllByUserId()
                }
                fields = fetchedFields
            }
            LazyColumn {
                if (fields.isNotEmpty()) {
                    items(fields) { field ->
                        FieldRow(field, navController)
                    }
                } else {
                    //TODO: handle empty
//                    items(fields) { field ->
//                        Text(
//                            text = "Brak pól",
//                            modifier = Modifier.fillMaxWidth().padding(8.dp),
//                            style = MaterialTheme.typography.bodyLarge
//                        )
//                    }
                }
            }
        }
//        else {
//            FieldDetailScreen(
//                fieldName = selectedField!!,
//                cultivations = fieldCultivations[selectedField] ?: listOf(),
//                onAddCultivation = { cultivation ->
//                    fieldCultivations = fieldCultivations.toMutableMap().apply {
//                        this[selectedField!!] = (this[selectedField] ?: listOf()) + cultivation
//                    }
//                },
//                onBack = { selectedField = null }
//            )
//        }
    }
}

data class fieldInputField(val label: String, val value: String)

@Composable
fun FieldRow(field:Field, navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }

    val inputFields = listOf(
        fieldInputField("Nazwa", field.fieldName),
    )
    val sharedViewModel = LocalSharedViewModel.current
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
                text = field.fieldName,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {showDialog = true},
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
                    // TODO odswiezyc liste pól po usunięciu
                    CoroutineScope(Dispatchers.IO).launch {
                        fieldService.deleteField(field)
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
                onClick = { sharedViewModel.selectField(field)
                    navController.navigate("cultivations") },
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
                title = "Edytuj: ${field.fieldName}",
                onConfirm = {
                    // Po kazdej kolejnej edycji mapa inputFieldValues sie powieksza, a wartosc w kluczu to jeszcze nie zmieniona nazwa.
                    // Ten sam obiekt w wartosci przechowuje zmieniona nazwe pola.
//                    inputFieldValues.forEach { (key, value) ->
//                        if (key.value == field.fieldName){
//                            field.fieldName = value;
//                        }
//                    }
                    // Uproszczona wersja tego co wyżej, i tak pobieramy zawsze ostatni rekord w mapie jako Set i
                    // do fieldName przypisujemy jego wartość.
//                    if (inputFieldValues.entries.last().value.isNotBlank()) {
//                        field.fieldName = inputFieldValues.entries.last().value
//                    }
                    // TODO przy debugowaniu wszystko jest dobrze, po odpaleniu aplpikacji wyrzuca błąd
                    //  po wpisaniu pustego znaku
                    var newFieldName = ""
                    if (field.fieldName.isNotBlank()) {
                        newFieldName =
                            inputFieldValues.getValue(fieldInputField("Nazwa", field.fieldName))
                    }
                    if (newFieldName.isNotBlank()) {
                        field.fieldName = newFieldName
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        fieldService.updateField(field)
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
fun FieldDetailScreen(fieldName: String, cultivations: List<String>, onAddCultivation: (String) -> Unit, onBack: () -> Unit) {
    var showCultivationForm by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Field: $fieldName", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))
        Text("Cultivations in this field:", style = MaterialTheme.typography.bodyLarge)
        Column {
            cultivations.forEach { cultivation ->
                Text(text = cultivation, modifier = Modifier.padding(4.dp))
            }
        }

        Button(
            onClick = { showCultivationForm = !showCultivationForm },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(if (showCultivationForm) "Hide Cultivation Form" else "Add Cultivation")
        }

        if (showCultivationForm) {
            CultivationForm(onAddCultivation)
        }

        Button(
            onClick = onBack,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Back to Fields")
        }
    }
}

@Composable
fun CultivationForm(onAddCultivation: (String) -> Unit) {
    var plantName by remember { mutableStateOf("") }
    var sowingDate by remember { mutableStateOf("") }
    var harvestDate by remember { mutableStateOf("") }
    var fertilizerQty by remember { mutableStateOf("") }
    var sprayingQty by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = plantName,
            onValueChange = { plantName = it },
            label = { Text("Plant Name") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = sowingDate,
            onValueChange = { sowingDate = it },
            label = { Text("Sowing Date") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = harvestDate,
            onValueChange = { harvestDate = it },
            label = { Text("Planned Harvest Date") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = fertilizerQty,
            onValueChange = { fertilizerQty = it },
            label = { Text("Used Fertilizer Quantity") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = sprayingQty,
            onValueChange = { sprayingQty = it },
            label = { Text("Used Spraying Quantity") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                if (plantName.isNotBlank() && sowingDate.isNotBlank() && harvestDate.isNotBlank() && fertilizerQty.isNotBlank() && sprayingQty.isNotBlank()) {
                    onAddCultivation("$plantName - $sowingDate - $harvestDate - Fertilizer: $fertilizerQty kg - Spraying: $sprayingQty L")
                    plantName = ""
                    sowingDate = ""
                    harvestDate = ""
                    fertilizerQty = ""
                    sprayingQty = ""
                }
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Save Cultivation Data")
        }
    }
}
