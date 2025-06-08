package com.example.mrolnik.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.mrolnik.model.Combustion
import com.example.mrolnik.service.CombustionService
import com.example.mrolnik.viewmodel.LocalSharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

val combustionService = CombustionService()

data class combustionInputField(val label: String, val value: String)

@Composable
fun VehicleInformationScreen(navController: NavController) {
    val sharedVehicleViewModel = LocalSharedViewModel.current
    val vehicleState = sharedVehicleViewModel.selectedVehicle.collectAsState()
    val currentVehicle = vehicleState.value
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var showEditInformationDialog by remember { mutableStateOf(false) }
    var showAddForm by remember { mutableStateOf(false) }
    var vehicleCombustion by remember { mutableStateOf<Combustion?>(null) }
    var refreshTrigger by remember { mutableStateOf(0) }

    // LISTA inputFieldów dla dodawania zasobu
    val combustionsInputField = listOf(
        combustionInputField("Ilość paliwa", ""),
        combustionInputField("Przebieg", ""),
        combustionInputField("Data pomiaru", "")
    )

    // Lista z wartościami
    var inputCombustionsFieldValues by remember { mutableStateOf(combustionsInputField.associateWith { it.value }) }

    fun refreshCombustion() {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                vehicleCombustion = combustionService.getCombustionByVehicleId(currentVehicle)
            }
        }
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
                    text = "Dodatkowe informacje",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )
            }

            // Informacje o pojeździe
            if (currentVehicle != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = "Pojazd",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = currentVehicle.vehicleName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                        Text(
                            text = "Stan techniczny: ${currentVehicle.technicalCondition}",
                            fontSize = 14.sp,
                            color = Color(0xFF757575),
                            modifier = Modifier.padding(start = 36.dp)
                        )
                    }
                }
            }

            LaunchedEffect(currentVehicle, refreshTrigger) {
                refreshCombustion()
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (vehicleCombustion != null) {
                    item {
                        // Karta z informacjami o spalaniu
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = "Informacje o spalaniu",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                InfoRow(
                                    icon = Icons.Default.LocalGasStation,
                                    label = "Ilość paliwa",
                                    value = "${vehicleCombustion!!.amountOfFuel} l"
                                )

                                InfoRow(
                                    icon = Icons.Default.Speed,
                                    label = "Przebieg",
                                    value = "${vehicleCombustion!!.mileage} km"
                                )

                                InfoRow(
                                    icon = Icons.Default.DateRange,
                                    label = "Data pomiaru",
                                    value = vehicleCombustion!!.measurementDate
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Button(
                                        onClick = { showEditInformationDialog = true },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF4CAF50)
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        elevation = ButtonDefaults.buttonElevation(
                                            defaultElevation = 4.dp
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edytuj",
                                            modifier = Modifier.size(16.dp),
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Edytuj",
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            CoroutineScope(Dispatchers.IO).launch {
                                                try {
                                                    combustionService.deleteCombustion(vehicleCombustion!!)
                                                    withContext(Dispatchers.Main) {
                                                        refreshTrigger++
                                                        snackbarHostState.showSnackbar(
                                                            message = "Informacje zostały usunięte",
                                                            duration = SnackbarDuration.Short
                                                        )
                                                    }
                                                } catch (e: Exception) {
                                                    withContext(Dispatchers.Main) {
                                                        snackbarHostState.showSnackbar(
                                                            message = "Błąd podczas usuwania informacji",
                                                            duration = SnackbarDuration.Short
                                                        )
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFF44336)
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        elevation = ButtonDefaults.buttonElevation(
                                            defaultElevation = 4.dp
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Usuń",
                                            modifier = Modifier.size(16.dp),
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Usuń",
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    item {
                        // Przycisk dodawania informacji
                        Button(
                            onClick = { showAddForm = !showAddForm },
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
                                imageVector = if (showAddForm) Icons.Default.Close else Icons.Default.Add,
                                contentDescription = if (showAddForm) "Anuluj" else "Dodaj informacje",
                                modifier = Modifier.size(20.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (showAddForm) "Anuluj" else "Dodaj informacje o spalaniu",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }

                    if (showAddForm) {
                        item {
                            // Formularz dodawania
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "Dodaj informacje o spalaniu",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF2E7D32),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )

                                    combustionsInputField.forEach { inputField ->
                                        OutlinedTextField(
                                            value = inputCombustionsFieldValues[inputField] ?: "",
                                            onValueChange = { newValue ->
                                                inputCombustionsFieldValues =
                                                    inputCombustionsFieldValues.toMutableMap().apply {
                                                        this[inputField] = newValue
                                                    }
                                            },
                                            label = { Text(inputField.label) },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = when (inputField.label) {
                                                        "Ilość paliwa" -> Icons.Default.LocalGasStation
                                                        "Przebieg" -> Icons.Default.Speed
                                                        "Data pomiaru" -> Icons.Default.DateRange
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

                                            val fieldValues =
                                                inputCombustionsFieldValues.mapKeys { it.key.label }

                                            val mileageStr = fieldValues["Przebieg"] ?: ""
                                            val amountOfFuelStr = fieldValues["Ilość paliwa"] ?: ""
                                            val measurementDate = fieldValues["Data pomiaru"] ?: ""

                                            if (mileageStr.isBlank() || amountOfFuelStr.isBlank() || measurementDate.isBlank()) {
                                                CoroutineScope(Dispatchers.Main).launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = "Wszystkie pola muszą być wypełnione",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                }
                                                return@Button
                                            }

                                            val mileage = mileageStr.toDoubleOrNull()
                                            val amountOfFuel = amountOfFuelStr.toDoubleOrNull()

                                            if (mileage == null || amountOfFuel == null) {
                                                CoroutineScope(Dispatchers.Main).launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = "Przebieg i ilość paliwa muszą być liczbami",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                }
                                                return@Button
                                            }

                                            val combustion = Combustion(measurementDate, amountOfFuel, mileage)

                                            CoroutineScope(Dispatchers.IO).launch {
                                                try {
                                                    combustionService.assignVehicleToCombustion(
                                                        combustion,
                                                        currentVehicle
                                                    )
                                                    withContext(Dispatchers.Main) {
                                                        showAddForm = false
                                                        refreshTrigger++
                                                        // Resetuj wartości formularza
                                                        inputCombustionsFieldValues = combustionsInputField.associateWith { "" }
                                                        snackbarHostState.showSnackbar(
                                                            message = "Informacje zostały dodane",
                                                            duration = SnackbarDuration.Short
                                                        )
                                                    }
                                                } catch (e: Exception) {
                                                    withContext(Dispatchers.Main) {
                                                        snackbarHostState.showSnackbar(
                                                            message = "Błąd podczas dodawania informacji",
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
                                            text = "Dodaj informacje",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (!showAddForm) {
                        item {
                            // Placeholder gdy brak danych
                            Card(
                                modifier = Modifier.fillMaxWidth(),
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
                                        imageVector = Icons.Default.LocalGasStation,
                                        contentDescription = "Brak informacji",
                                        modifier = Modifier.size(48.dp),
                                        tint = Color(0xFF9E9E9E)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Brak informacji o spalaniu",
                                        fontSize = 16.sp,
                                        color = Color(0xFF757575),
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "Dodaj informacje o spalaniu klikając przycisk powyżej",
                                        fontSize = 14.sp,
                                        color = Color(0xFF9E9E9E),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
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

    // Dialog edycji
    if (showEditInformationDialog) {
        // Ustaw aktualne wartości w inputach przed pokazaniem dialogu
        LaunchedEffect(showEditInformationDialog) {
            if (vehicleCombustion != null) {
                inputCombustionsFieldValues = mapOf(
                    combustionInputField("Ilość paliwa", "") to vehicleCombustion!!.amountOfFuel.toString(),
                    combustionInputField("Przebieg", "") to vehicleCombustion!!.mileage.toString(),
                    combustionInputField("Data pomiaru", "") to vehicleCombustion!!.measurementDate
                )
            }
        }

        CustomModalDialog(
            onDismiss = { showEditInformationDialog = false },
            title = "Edytuj informacje o spalaniu",
            onConfirm = {
                val fieldValues = inputCombustionsFieldValues.mapKeys { it.key.label }

                val mileageStr = fieldValues["Przebieg"] ?: ""
                val amountOfFuelStr = fieldValues["Ilość paliwa"] ?: ""
                val measurementDate = fieldValues["Data pomiaru"] ?: ""

                if (mileageStr.isBlank() || amountOfFuelStr.isBlank() || measurementDate.isBlank()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        snackbarHostState.showSnackbar(
                            message = "Wszystkie pola muszą być wypełnione",
                            duration = SnackbarDuration.Short
                        )
                    }
                    return@CustomModalDialog
                }

                val mileage = mileageStr.toDoubleOrNull()
                val amountOfFuel = amountOfFuelStr.toDoubleOrNull()

                if (mileage == null || amountOfFuel == null) {
                    CoroutineScope(Dispatchers.Main).launch {
                        snackbarHostState.showSnackbar(
                            message = "Przebieg i ilość paliwa muszą być liczbami",
                            duration = SnackbarDuration.Short
                        )
                    }
                    return@CustomModalDialog
                }

                vehicleCombustion?.mileage = mileage
                vehicleCombustion?.measurementDate = measurementDate
                vehicleCombustion?.amountOfFuel = amountOfFuel

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        combustionService.updateCombustion(vehicleCombustion!!)
                        withContext(Dispatchers.Main) {
                            showEditInformationDialog = false
                            refreshTrigger++
                            snackbarHostState.showSnackbar(
                                message = "Informacje zostały zaktualizowane",
                                duration = SnackbarDuration.Short
                            )
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            snackbarHostState.showSnackbar(
                                message = "Błąd podczas aktualizacji informacji",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                }
            },
            content = {
                combustionsInputField.forEach { inputField ->
                    OutlinedTextField(
                        value = inputCombustionsFieldValues[inputField] ?: "",
                        onValueChange = { newValue ->
                            inputCombustionsFieldValues = inputCombustionsFieldValues.toMutableMap().apply {
                                this[inputField] = newValue
                            }
                        },
                        label = { Text(inputField.label) },
                        leadingIcon = {
                            Icon(
                                imageVector = when (inputField.label) {
                                    "Ilość paliwa" -> Icons.Default.LocalGasStation
                                    "Przebieg" -> Icons.Default.Speed
                                    "Data pomiaru" -> Icons.Default.DateRange
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
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        )
    }
}

@Composable
fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF757575),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 16.sp,
                color = Color(0xFF2E7D32),
                fontWeight = FontWeight.Medium
            )
        }
    }
}