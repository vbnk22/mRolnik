package com.example.mrolnik.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mrolnik.model.Spraying
import com.example.mrolnik.service.SprayingService
import com.example.mrolnik.viewmodel.LocalSharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

data class sprayingInputField(val label: String, val value: String)

val sprayingService = SprayingService()

@Composable
fun SprayingHistoryScreen(navController: NavController) {
    val sharedFruitTreeViewModel = LocalSharedViewModel.current
    val fruitTreeState = sharedFruitTreeViewModel.selectedFruitTree.collectAsState()
    val currentFruitTree = fruitTreeState.value

    var showForm by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var sprayings by remember { mutableStateOf(emptyList<Spraying>()) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val sprayingsInputField = listOf(
        sprayingInputField("Nazwa oprysku", ""),
        sprayingInputField("Data oprysku", ""),
        sprayingInputField("Ilość oprysków", "")
    )

    var inputSprayingsFieldValues by remember {
        mutableStateOf(sprayingsInputField.associateWith { it.value })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header z przyciskiem powrotu i tytułem
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Wróć",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Historia oprysków - ${currentFruitTree?.plantName}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )
            }

            // Przycisk dodawania
            Button(
                onClick = { showForm = !showForm },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Icon(
                    imageVector = if (showForm) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = if (showForm) "Anuluj" else "Dodaj oprysk",
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (showForm) "Anuluj" else "Dodaj oprysk",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }

            // Formularz dodawania
            if (showForm) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Dodaj nowy oprysk",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        sprayingsInputField.forEach { inputField ->
                            OutlinedTextField(
                                value = inputSprayingsFieldValues[inputField] ?: "",
                                onValueChange = { newValue ->
                                    inputSprayingsFieldValues = inputSprayingsFieldValues.toMutableMap().apply {
                                        this[inputField] = newValue
                                    }
                                },
                                label = { Text(inputField.label) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when (inputField.label) {
                                            "Nazwa oprysku" -> Icons.Default.WaterDrop
                                            "Data oprysku" -> Icons.Default.CalendarToday
                                            "Ilość oprysków" -> Icons.Default.Scale
                                            else -> Icons.Default.Info
                                        },
                                        contentDescription = inputField.label,
                                        tint = Color(0xFF4CAF50)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Wpisz ${inputField.label}") },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF4CAF50),
                                    focusedLabelColor = Color(0xFF4CAF50)
                                )
                            )
                        }

                        Button(
                            onClick = {
                                keyboardController?.hide()
                                focusManager.clearFocus()

                                val fieldValues = inputSprayingsFieldValues.mapKeys { it.key.label }
                                val name = fieldValues["Nazwa oprysku"] ?: ""
                                val date = fieldValues["Data oprysku"] ?: ""
                                val quantityStr = fieldValues["Ilość oprysków"] ?: ""

                                if (name.isBlank() || date.isBlank() || quantityStr.isBlank()) {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Wszystkie pola muszą być wypełnione",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    return@Button
                                }

                                val quantity = quantityStr.toDoubleOrNull()
                                if (quantity == null) {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Ilość oprysków musi być liczbą",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    return@Button
                                }

                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val spraying = Spraying(name, LocalDate.parse(date).toString(), quantity)
                                        sprayingService.assignSprayingToFruitTree(spraying, currentFruitTree)

                                        val fetchedSprayings = sprayingService.getAllSprayingByFruitTreeId(currentFruitTree)

                                        withContext(Dispatchers.Main) {
                                            sprayings = fetchedSprayings
                                            inputSprayingsFieldValues = sprayingsInputField.associateWith { "" }
                                            showForm = false
                                            snackbarHostState.showSnackbar(
                                                message = "Oprysk został dodany",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            snackbarHostState.showSnackbar(
                                                message = "Błąd podczas dodawania oprysku",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp
                            )
                        ) {
                            Text(
                                text = "Dodaj oprysk",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Tytuł listy
            Text(
                text = "Historia oprysków",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2E7D32),
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Lista oprysków
            LaunchedEffect(Unit) {
                sprayings = sprayingService.getAllSprayingByFruitTreeId(currentFruitTree)
            }

            if (sprayings.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = "Brak oprysków",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF9E9E9E)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Brak oprysków",
                            fontSize = 16.sp,
                            color = Color(0xFF757575),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Dodaj pierwszy oprysk klikając przycisk powyżej",
                            fontSize = 14.sp,
                            color = Color(0xFF9E9E9E),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sprayings) { spraying ->
                        SprayingRow(
                            spraying = spraying,
                            onSprayingUpdated = {
                                // Odśwież listę po aktualizacji
                                CoroutineScope(Dispatchers.IO).launch {
                                    val fetchedSprayings = sprayingService.getAllSprayingByFruitTreeId(currentFruitTree)
                                    withContext(Dispatchers.Main) {
                                        sprayings = fetchedSprayings
                                    }
                                }
                            },
                            onSprayingDeleted = {
                                // Odśwież listę po usunięciu
                                CoroutineScope(Dispatchers.IO).launch {
                                    val fetchedSprayings = sprayingService.getAllSprayingByFruitTreeId(currentFruitTree)
                                    withContext(Dispatchers.Main) {
                                        sprayings = fetchedSprayings
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) { snackbarData ->
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2E7D32)
                )
            ) {
                Text(
                    text = snackbarData.visuals.message,
                    modifier = Modifier.padding(16.dp),
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun SprayingRow(
    spraying: Spraying,
    onSprayingUpdated: () -> Unit,
    onSprayingDeleted: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Lista pól, które chcemy edytować
    val inputFields = listOf(
        sprayingInputField("Nazwa oprysku", spraying.sprayingName),
        sprayingInputField("Data oprysku", spraying.sprayingDate),
        sprayingInputField("Ilość oprysków", spraying.sprayingQuantity.toString())
    )

    // Stan dla dynamicznie tworzonych inputów
    var inputFieldValues by remember { mutableStateOf(inputFields.associateWith { it.value }) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = spraying.sprayingName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        text = "Data: ${spraying.sprayingDate}",
                        fontSize = 14.sp,
                        color = Color(0xFF757575),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Text(
                        text = "Ilość: ${spraying.sprayingQuantity}",
                        fontSize = 14.sp,
                        color = Color(0xFF757575),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { showDialog = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edytuj",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    sprayingService.deleteSpraying(spraying)
                                    withContext(Dispatchers.Main) {
                                        onSprayingDeleted()
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        snackbarHostState.showSnackbar(
                                            message = "Błąd podczas usuwania oprysku",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Usuń",
                            tint = Color(0xFFF44336),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        CustomModalDialog(
            onDismiss = { showDialog = false },
            title = "Edytuj: ${spraying.sprayingName}",
            onConfirm = {
                val fieldValues = inputFieldValues.mapKeys { it.key.label }
                val name = fieldValues["Nazwa oprysku"] ?: ""
                val date = fieldValues["Data oprysku"] ?: ""
                val quantityStr = fieldValues["Ilość oprysków"] ?: ""

                if (name.isBlank() || date.isBlank() || quantityStr.isBlank()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        snackbarHostState.showSnackbar(
                            message = "Wszystkie pola muszą być wypełnione",
                            duration = SnackbarDuration.Short
                        )
                    }
                    return@CustomModalDialog
                }

                val quantity = quantityStr.toDoubleOrNull()
                if (quantity == null) {
                    CoroutineScope(Dispatchers.Main).launch {
                        snackbarHostState.showSnackbar(
                            message = "Ilość oprysków musi być liczbą",
                            duration = SnackbarDuration.Short
                        )
                    }
                    return@CustomModalDialog
                }

                spraying.sprayingName = name
                spraying.sprayingDate = date
                spraying.sprayingQuantity = quantity

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        sprayingService.updateSpraying(spraying)
                        withContext(Dispatchers.Main) {
                            onSprayingUpdated()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            snackbarHostState.showSnackbar(
                                message = "Błąd podczas aktualizacji oprysku",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                }
                showDialog = false
            },
            content = {
                inputFields.forEach { inputField ->
                    OutlinedTextField(
                        value = inputFieldValues[inputField] ?: "",
                        onValueChange = { newValue ->
                            inputFieldValues = inputFieldValues.toMutableMap().apply {
                                this[inputField] = newValue
                            }
                        },
                        label = { Text(inputField.label) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Wpisz ${inputField.label}") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4CAF50),
                            focusedLabelColor = Color(0xFF4CAF50)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        )
    }

}
@Composable
fun SprayingCultivationHistoryScreen(navController: NavController) {
    val sharedCultivationViewModel = LocalSharedViewModel.current
    val cultivationState = sharedCultivationViewModel.selectedCultivation.collectAsState()
    val currentCultivation = cultivationState.value

    var showForm by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var sprayings by remember { mutableStateOf(emptyList<Spraying>()) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val sprayingsInputField = listOf(
        sprayingInputField("Nazwa oprysku", ""),
        sprayingInputField("Data oprysku", ""),
        sprayingInputField("Ilość oprysków", "")
    )

    var inputSprayingsFieldValues by remember {
        mutableStateOf(sprayingsInputField.associateWith { it.value })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header z przyciskiem powrotu i tytułem
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Wróć",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Historia oprysków - ${currentCultivation?.plantName}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )
            }

            // Przycisk dodawania
            Button(
                onClick = { showForm = !showForm },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Icon(
                    imageVector = if (showForm) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = if (showForm) "Anuluj" else "Dodaj oprysk",
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (showForm) "Anuluj" else "Dodaj oprysk",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }

            // Formularz dodawania
            if (showForm) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Dodaj nowy oprysk",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        sprayingsInputField.forEach { inputField ->
                            OutlinedTextField(
                                value = inputSprayingsFieldValues[inputField] ?: "",
                                onValueChange = { newValue ->
                                    inputSprayingsFieldValues = inputSprayingsFieldValues.toMutableMap().apply {
                                        this[inputField] = newValue
                                    }
                                },
                                label = { Text(inputField.label) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when (inputField.label) {
                                            "Nazwa oprysku" -> Icons.Default.WaterDrop
                                            "Data oprysku" -> Icons.Default.CalendarToday
                                            "Ilość oprysków" -> Icons.Default.Scale
                                            else -> Icons.Default.Info
                                        },
                                        contentDescription = inputField.label,
                                        tint = Color(0xFF4CAF50)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Wpisz ${inputField.label}") },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF4CAF50),
                                    focusedLabelColor = Color(0xFF4CAF50)
                                )
                            )
                        }

                        Button(
                            onClick = {
                                keyboardController?.hide()
                                focusManager.clearFocus()

                                val fieldValues = inputSprayingsFieldValues.mapKeys { it.key.label }
                                val name = fieldValues["Nazwa oprysku"] ?: ""
                                val date = fieldValues["Data oprysku"] ?: ""
                                val quantityStr = fieldValues["Ilość oprysków"] ?: ""

                                if (name.isBlank() || date.isBlank() || quantityStr.isBlank()) {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Wszystkie pola muszą być wypełnione",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    return@Button
                                }

                                val quantity = quantityStr.toDoubleOrNull()
                                if (quantity == null) {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Ilość oprysków musi być liczbą",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    return@Button
                                }

                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val spraying = Spraying(name, LocalDate.parse(date).toString(), quantity)
                                        sprayingService.assignSprayingToCultivation(spraying, currentCultivation)

                                        val fetchedSprayings = sprayingService.getAllSprayingByCultivationId(currentCultivation)

                                        withContext(Dispatchers.Main) {
                                            sprayings = fetchedSprayings
                                            inputSprayingsFieldValues = sprayingsInputField.associateWith { "" }
                                            showForm = false
                                            snackbarHostState.showSnackbar(
                                                message = "Oprysk został dodany",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            snackbarHostState.showSnackbar(
                                                message = "Błąd podczas dodawania oprysku",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp
                            )
                        ) {
                            Text(
                                text = "Dodaj oprysk",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Tytuł listy
            Text(
                text = "Historia oprysków",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2E7D32),
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Lista oprysków
            LaunchedEffect(Unit) {
                sprayings = sprayingService.getAllSprayingByCultivationId(currentCultivation)
            }

            if (sprayings.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = "Brak oprysków",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF9E9E9E)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Brak oprysków",
                            fontSize = 16.sp,
                            color = Color(0xFF757575),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Dodaj pierwszy oprysk klikając przycisk powyżej",
                            fontSize = 14.sp,
                            color = Color(0xFF9E9E9E),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sprayings) { spraying ->
                        SprayingCultivationRow(
                            spraying = spraying,
                            onSprayingUpdated = {
                                // Odśwież listę po aktualizacji
                                CoroutineScope(Dispatchers.IO).launch {
                                    val fetchedSprayings = sprayingService.getAllSprayingByCultivationId(currentCultivation)
                                    withContext(Dispatchers.Main) {
                                        sprayings = fetchedSprayings
                                    }
                                }
                            },
                            onSprayingDeleted = {
                                // Odśwież listę po usunięciu
                                CoroutineScope(Dispatchers.IO).launch {
                                    val fetchedSprayings = sprayingService.getAllSprayingByCultivationId(currentCultivation)
                                    withContext(Dispatchers.Main) {
                                        sprayings = fetchedSprayings
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) { snackbarData ->
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2E7D32)
                )
            ) {
                Text(
                    text = snackbarData.visuals.message,
                    modifier = Modifier.padding(16.dp),
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun SprayingCultivationRow(
    spraying: Spraying,
    onSprayingUpdated: () -> Unit,
    onSprayingDeleted: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Lista pól, które chcemy edytować
    val inputFields = listOf(
        sprayingInputField("Nazwa oprysku", spraying.sprayingName),
        sprayingInputField("Data oprysku", spraying.sprayingDate),
        sprayingInputField("Ilość oprysków", spraying.sprayingQuantity.toString())
    )

    // Stan dla dynamicznie tworzonych inputów
    var inputFieldValues by remember { mutableStateOf(inputFields.associateWith { it.value }) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = spraying.sprayingName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        text = "Data: ${spraying.sprayingDate}",
                        fontSize = 14.sp,
                        color = Color(0xFF757575),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Text(
                        text = "Ilość: ${spraying.sprayingQuantity}",
                        fontSize = 14.sp,
                        color = Color(0xFF757575),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { showDialog = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edytuj",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    sprayingService.deleteSpraying(spraying)
                                    withContext(Dispatchers.Main) {
                                        onSprayingDeleted()
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        snackbarHostState.showSnackbar(
                                            message = "Błąd podczas usuwania oprysku",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Usuń",
                            tint = Color(0xFFF44336),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        CustomModalDialog(
            onDismiss = { showDialog = false },
            title = "Edytuj: ${spraying.sprayingName}",
            onConfirm = {
                val fieldValues = inputFieldValues.mapKeys { it.key.label }
                val name = fieldValues["Nazwa oprysku"] ?: ""
                val date = fieldValues["Data oprysku"] ?: ""
                val quantityStr = fieldValues["Ilość oprysków"] ?: ""

                if (name.isBlank() || date.isBlank() || quantityStr.isBlank()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        snackbarHostState.showSnackbar(
                            message = "Wszystkie pola muszą być wypełnione",
                            duration = SnackbarDuration.Short
                        )
                    }
                    return@CustomModalDialog
                }

                val quantity = quantityStr.toDoubleOrNull()
                if (quantity == null) {
                    CoroutineScope(Dispatchers.Main).launch {
                        snackbarHostState.showSnackbar(
                            message = "Ilość oprysków musi być liczbą",
                            duration = SnackbarDuration.Short
                        )
                    }
                    return@CustomModalDialog
                }

                spraying.sprayingName = name
                spraying.sprayingDate = date
                spraying.sprayingQuantity = quantity

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        sprayingService.updateSpraying(spraying)
                        withContext(Dispatchers.Main) {
                            onSprayingUpdated()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            snackbarHostState.showSnackbar(
                                message = "Błąd podczas aktualizacji oprysku",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                }
                showDialog = false
            },
            content = {
                inputFields.forEach { inputField ->
                    OutlinedTextField(
                        value = inputFieldValues[inputField] ?: "",
                        onValueChange = { newValue ->
                            inputFieldValues = inputFieldValues.toMutableMap().apply {
                                this[inputField] = newValue
                            }
                        },
                        label = { Text(inputField.label) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Wpisz ${inputField.label}") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4CAF50),
                            focusedLabelColor = Color(0xFF4CAF50)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        )
    }
}