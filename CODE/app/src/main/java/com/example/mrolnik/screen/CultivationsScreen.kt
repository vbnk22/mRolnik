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
import com.example.mrolnik.model.Cultivation
import com.example.mrolnik.service.CultivationService
import com.example.mrolnik.viewmodel.LocalSharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class cultivationInputField(val label: String, val value: String)
val cultivationService = CultivationService()

@Composable
fun CultivationsScreen(navController: NavController) {
    val sharedFieldViewModel = LocalSharedViewModel.current
    val fieldState = sharedFieldViewModel.selectedField.collectAsState()
    val currentField = fieldState.value

    var showForm by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var cultivations by remember { mutableStateOf(emptyList<Cultivation>()) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Lista inputFieldów dla dodawania uprawy
    val cultivationsInputField = listOf(
        cultivationInputField("Nazwa", ""),
        cultivationInputField("Data zasiewu", ""),
        cultivationInputField("Planowany zbiór", ""),
        cultivationInputField("Ilość oprysków", ""),
        cultivationInputField("Ilość nawozu", "")
    )

    // Lista z wartościami
    var inputCultivationsFieldValues by remember { mutableStateOf(cultivationsInputField.associateWith { it.value }) }

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
                    text = "Uprawy na polu ${currentField?.fieldName ?: ""}",
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
                    contentDescription = if (showForm) "Anuluj" else "Dodaj uprawę",
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (showForm) "Anuluj" else "Dodaj uprawę",
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
                            text = "Dodaj nową uprawę",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        cultivationsInputField.forEach { inputField ->
                            OutlinedTextField(
                                value = inputCultivationsFieldValues[inputField] ?: "",
                                onValueChange = { newValue ->
                                    inputCultivationsFieldValues = inputCultivationsFieldValues.toMutableMap().apply {
                                        this[inputField] = newValue
                                    }
                                },
                                label = { Text(inputField.label) },
                                leadingIcon = {
                                    val icon = when(inputField.label) {
                                        "Nazwa" -> Icons.Default.Grass
                                        "Data zasiewu" -> Icons.Default.CalendarToday
                                        "Planowany zbiór" -> Icons.Default.Schedule
                                        "Ilość oprysków" -> Icons.Default.Opacity
                                        "Ilość nawozu" -> Icons.Default.Eco
                                        else -> Icons.Default.Info
                                    }
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = inputField.label,
                                        tint = Color(0xFF4CAF50)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF4CAF50),
                                    focusedLabelColor = Color(0xFF4CAF50)
                                ),
                                placeholder = { Text("Wpisz ${inputField.label}") }
                            )
                        }

                        Button(
                            onClick = {
                                keyboardController?.hide()
                                focusManager.clearFocus()

                                val fieldValues = inputCultivationsFieldValues.mapKeys { it.key.label }
                                val plantName = fieldValues["Nazwa"] ?: ""
                                val sowingDate = fieldValues["Data zasiewu"] ?: ""
                                val plannedHarvestDate = fieldValues["Planowany zbiór"] ?: ""
                                val usedFertilizerQuantity = fieldValues["Ilość nawozu"]?.toDoubleOrNull() ?: 0.0
                                val usedSprayingQuantity = fieldValues["Ilość oprysków"]?.toDoubleOrNull() ?: 0.0

                                if (plantName.isBlank()) {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Nazwa uprawy nie może być pusta",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    return@Button
                                }

                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val cultivation = Cultivation(plantName, sowingDate, plannedHarvestDate, usedSprayingQuantity, usedFertilizerQuantity)
                                        cultivationService.assignCultivationToField(cultivation, currentField)

                                        val fetchedCultivations = cultivationService.getAllCultivationsByFieldId(currentField)

                                        withContext(Dispatchers.Main) {
                                            cultivations = fetchedCultivations
                                            inputCultivationsFieldValues = cultivationsInputField.associateWith { "" }
                                            showForm = false
                                            snackbarHostState.showSnackbar(
                                                message = "Uprawa została dodana",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            snackbarHostState.showSnackbar(
                                                message = "Błąd podczas dodawania uprawy",
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
                                text = "Dodaj uprawę",
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
                text = "Twoje uprawy",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2E7D32),
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Lista upraw
            LaunchedEffect(Unit) {
                val fetchedCultivations = withContext(Dispatchers.IO) {
                    cultivationService.getAllCultivationsByFieldId(currentField)
                }
                cultivations = fetchedCultivations
            }

            if (cultivations.isEmpty()) {
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
                            imageVector = Icons.Default.Grass,
                            contentDescription = "Brak upraw",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF9E9E9E)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Brak upraw",
                            fontSize = 16.sp,
                            color = Color(0xFF757575),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Dodaj pierwszą uprawę klikając przycisk powyżej",
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
                    items(cultivations) { cultivation ->
                        CultivationRow(
                            cultivation = cultivation,
                            navController = navController,
                            onCultivationUpdated = {
                                // Odśwież listę po aktualizacji
                                CoroutineScope(Dispatchers.IO).launch {
                                    val fetchedCultivations = cultivationService.getAllCultivationsByFieldId(currentField)
                                    withContext(Dispatchers.Main) {
                                        cultivations = fetchedCultivations
                                    }
                                }
                            },
                            onCultivationDeleted = {
                                // Odśwież listę po usunięciu
                                CoroutineScope(Dispatchers.IO).launch {
                                    val fetchedCultivations = cultivationService.getAllCultivationsByFieldId(currentField)
                                    withContext(Dispatchers.Main) {
                                        cultivations = fetchedCultivations
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
fun CultivationRow(
    cultivation: Cultivation,
    navController: NavController,
    onCultivationUpdated: () -> Unit,
    onCultivationDeleted: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val sharedViewModel = LocalSharedViewModel.current

    // Lista pól, które chcemy edytować
    val inputFields = listOf(
        cultivationInputField("Nazwa", cultivation.plantName),
        cultivationInputField("Data zasiewu", cultivation.sowingDate),
        cultivationInputField("Planowany zbiór", cultivation.plannedHarvestDate),
        cultivationInputField("Ilość oprysków", cultivation.usedSprayingQuantity.toString()),
        cultivationInputField("Ilość nawozu", cultivation.usedFertilizerQuantity.toString())
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
            // Główny wiersz z nazwą i przyciskami
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = cultivation.plantName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2E7D32)
                    )
                    if (!isExpanded) {
                        Text(
                            text = "Zasiew: ${cultivation.sowingDate}",
                            fontSize = 12.sp,
                            color = Color(0xFF757575),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Zwiń" else "Rozwiń",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                    }

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
                                    cultivationService.deleteCultivation(cultivation)
                                    withContext(Dispatchers.Main) {
                                        onCultivationDeleted()
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        snackbarHostState.showSnackbar(
                                            message = "Błąd podczas usuwania uprawy",
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

            // Rozszerzone informacje
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DetailRow("Data zasiewu", cultivation.sowingDate)
                        DetailRow("Planowany zbiór", cultivation.plannedHarvestDate)
                        DetailRow("Ilość oprysków", cultivation.usedSprayingQuantity.toString())
                        DetailRow("Ilość nawozu", cultivation.usedFertilizerQuantity.toString())
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Przyciski nawigacyjne
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            sharedViewModel.selectCultivation(cultivation)
                            navController.navigate("fertilizerHistory")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Historia nawozów", fontSize = 14.sp)
                    }

                    Button(
                        onClick = {
                            sharedViewModel.selectCultivation(cultivation)
                            navController.navigate("sprayingCultivationHistory")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF9C27B0)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Opacity,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Historia oprysków", fontSize = 14.sp)
                    }
                }
            }
        }
    }

    if (showDialog) {
        CustomModalDialog(
            onDismiss = { showDialog = false },
            title = "Edytuj: ${cultivation.plantName}",
            onConfirm = {
                val fieldValues = inputFieldValues.mapKeys { it.key.label }
                val plantName = fieldValues["Nazwa"] ?: ""
                val sowingDate = fieldValues["Data zasiewu"] ?: ""
                val plannedHarvestDate = fieldValues["Planowany zbiór"] ?: ""
                val usedFertilizerQuantity = fieldValues["Ilość nawozu"]?.toDoubleOrNull() ?: 0.0
                val usedSprayingQuantity = fieldValues["Ilość oprysków"]?.toDoubleOrNull() ?: 0.0

                if (plantName.isBlank()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        snackbarHostState.showSnackbar(
                            message = "Nazwa uprawy nie może być pusta",
                            duration = SnackbarDuration.Short
                        )
                    }
                    return@CustomModalDialog
                }

                cultivation.plantName = plantName
                cultivation.sowingDate = sowingDate
                cultivation.plannedHarvestDate = plannedHarvestDate
                cultivation.usedFertilizerQuantity = usedFertilizerQuantity
                cultivation.usedSprayingQuantity = usedSprayingQuantity

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        cultivationService.updateCultivation(cultivation)
                        withContext(Dispatchers.Main) {
                            onCultivationUpdated()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            snackbarHostState.showSnackbar(
                                message = "Błąd podczas aktualizacji uprawy",
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
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF424242)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color(0xFF757575)
        )
    }
}