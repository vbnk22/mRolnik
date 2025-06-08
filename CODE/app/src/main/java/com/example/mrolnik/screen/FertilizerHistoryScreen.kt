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
import com.example.mrolnik.model.Fertilizer
import com.example.mrolnik.service.FertilizerService
import com.example.mrolnik.viewmodel.LocalSharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

data class fertilizerInputField(val label: String, val value: String)

val fertilizerService = FertilizerService()

@Composable
fun FertilizerHistoryScreen(navController: NavController) {
    val cultivationViewModel = LocalSharedViewModel.current
    val selectedCultivationState = cultivationViewModel.selectedCultivation.collectAsState()
    val currentCultivation = selectedCultivationState.value

    var showForm by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var fertilizers by remember { mutableStateOf(emptyList<Fertilizer>()) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val fertilizersInputField = listOf(
        fertilizerInputField("Nazwa nawozu", ""),
        fertilizerInputField("Data nawożenia", ""),
        fertilizerInputField("Ilość nawozu", "")
    )

    var inputFertilizerFieldValues by remember {
        mutableStateOf(fertilizersInputField.associateWith { it.value })
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
                    text = "Historia nawożenia - ${currentCultivation?.plantName}",
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
                    contentDescription = if (showForm) "Anuluj" else "Dodaj nawóz",
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (showForm) "Anuluj" else "Dodaj nawóz",
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
                            text = "Dodaj nowy nawóz",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        fertilizersInputField.forEach { inputField ->
                            OutlinedTextField(
                                value = inputFertilizerFieldValues[inputField] ?: "",
                                onValueChange = { newValue ->
                                    inputFertilizerFieldValues = inputFertilizerFieldValues.toMutableMap().apply {
                                        this[inputField] = newValue
                                    }
                                },
                                label = { Text(inputField.label) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when (inputField.label) {
                                            "Nazwa nawozu" -> Icons.Default.Agriculture
                                            "Data nawożenia" -> Icons.Default.CalendarToday
                                            "Ilość nawozu" -> Icons.Default.Scale
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

                                val fieldValues = inputFertilizerFieldValues.mapKeys { it.key.label }
                                val name = fieldValues["Nazwa nawozu"] ?: ""
                                val date = fieldValues["Data nawożenia"] ?: ""
                                val quantityStr = fieldValues["Ilość nawozu"] ?: ""

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
                                            message = "Ilość nawozu musi być liczbą",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    return@Button
                                }

                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val fertilizer = Fertilizer(name, LocalDate.parse(date).toString(), quantity)
                                        fertilizerService.assignFertilizerToCultivation(fertilizer, currentCultivation)

                                        val fetchedFertilizers = fertilizerService.getAllFertilizersByCultivationId(currentCultivation)

                                        withContext(Dispatchers.Main) {
                                            fertilizers = fetchedFertilizers
                                            inputFertilizerFieldValues = fertilizersInputField.associateWith { "" }
                                            showForm = false
                                            snackbarHostState.showSnackbar(
                                                message = "Nawóz został dodany",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            snackbarHostState.showSnackbar(
                                                message = "Błąd podczas dodawania nawozu",
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
                                text = "Dodaj nawóz",
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
                text = "Historia nawożenia",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2E7D32),
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Lista nawozów
            LaunchedEffect(Unit) {
                fertilizers = fertilizerService.getAllFertilizersByCultivationId(currentCultivation)
            }

            if (fertilizers.isEmpty()) {
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
                            imageVector = Icons.Default.Agriculture,
                            contentDescription = "Brak nawozów",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF9E9E9E)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Brak nawozów",
                            fontSize = 16.sp,
                            color = Color(0xFF757575),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Dodaj pierwszy nawóz klikając przycisk powyżej",
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
                    items(fertilizers) { fertilizer ->
                        FertilizerRow(
                            fertilizer = fertilizer,
                            onFertilizerUpdated = {
                                // Odśwież listę po aktualizacji
                                CoroutineScope(Dispatchers.IO).launch {
                                    val fetchedFertilizers = fertilizerService.getAllFertilizersByCultivationId(currentCultivation)
                                    withContext(Dispatchers.Main) {
                                        fertilizers = fetchedFertilizers
                                    }
                                }
                            },
                            onFertilizerDeleted = {
                                // Odśwież listę po usunięciu
                                CoroutineScope(Dispatchers.IO).launch {
                                    val fetchedFertilizers = fertilizerService.getAllFertilizersByCultivationId(currentCultivation)
                                    withContext(Dispatchers.Main) {
                                        fertilizers = fetchedFertilizers
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
fun FertilizerRow(
    fertilizer: Fertilizer,
    onFertilizerUpdated: () -> Unit,
    onFertilizerDeleted: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Lista pól, które chcemy edytować
    val inputFields = listOf(
        fertilizerInputField("Nazwa nawozu", fertilizer.fertilizerName),
        fertilizerInputField("Data nawożenia", fertilizer.fertilizingDate),
        fertilizerInputField("Ilość nawozu", fertilizer.fertilizerQuantity.toString())
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
                        text = fertilizer.fertilizerName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        text = "Data: ${fertilizer.fertilizingDate}",
                        fontSize = 14.sp,
                        color = Color(0xFF757575),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Text(
                        text = "Ilość: ${fertilizer.fertilizerQuantity} kg",
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
                                    fertilizerService.deleteFertilizer(fertilizer)
                                    withContext(Dispatchers.Main) {
                                        onFertilizerDeleted()
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        snackbarHostState.showSnackbar(
                                            message = "Błąd podczas usuwania nawozu",
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
            title = "Edytuj: ${fertilizer.fertilizerName}",
            onConfirm = {
                val fieldValues = inputFieldValues.mapKeys { it.key.label }
                val name = fieldValues["Nazwa nawozu"] ?: ""
                val date = fieldValues["Data nawożenia"] ?: ""
                val quantityStr = fieldValues["Ilość nawozu"] ?: ""

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
                            message = "Ilość nawozu musi być liczbą",
                            duration = SnackbarDuration.Short
                        )
                    }
                    return@CustomModalDialog
                }

                fertilizer.fertilizerName = name
                fertilizer.fertilizingDate = date
                fertilizer.fertilizerQuantity = quantity

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        fertilizerService.updateFertilizer(fertilizer)
                        withContext(Dispatchers.Main) {
                            onFertilizerUpdated()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            snackbarHostState.showSnackbar(
                                message = "Błąd podczas aktualizacji nawozu",
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