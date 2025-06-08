package com.example.mrolnik.screen

import android.annotation.SuppressLint
import android.util.Log
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
import com.example.mrolnik.model.Repair
import com.example.mrolnik.service.VehicleService
import com.example.mrolnik.viewmodel.LocalSharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

data class repairInputField(val label: String, val value: String)

@SuppressLint("NewApi")
@Composable
fun VehicleRepairHistoryScreen(navController: NavController) {
    val sharedVehicleViewModel = LocalSharedViewModel.current
    val vehicleState = sharedVehicleViewModel.selectedVehicle.collectAsState()
    val currentVehicle = vehicleState.value
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var showAddRepairDialog by remember { mutableStateOf(false) }
    var showAddForm by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }

    // LISTA inputFieldów dla dodawania zasobu
    val repairsInputField = listOf(
        repairInputField("Opis", ""),
        repairInputField("Koszt", ""),
        repairInputField("Data naprawy", "")
    )

    // Lista z wartościami
    var inputRepairsFieldValues by remember { mutableStateOf(repairsInputField.associateWith { it.value }) }
    var repairs by remember { mutableStateOf(emptyList<Repair>()) }
    var expandedIndex by remember { mutableStateOf<Int?>(null) }

    val vehicleService = VehicleService()

    fun refreshRepairs() {
        CoroutineScope(Dispatchers.IO).launch {
            val updatedRepairs = vehicleService.getAllRepairsByVehicleId(currentVehicle)
            withContext(Dispatchers.Main) {
                repairs = updatedRepairs
            }
        }
    }

    LaunchedEffect(currentVehicle, refreshTrigger) {
        refreshRepairs()
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
                    text = "Historia napraw",
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

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (repairs.isNotEmpty()) {
                    items(repairs) { repair ->
                        RepairItem(
                            repair = repair,
                            isExpanded = expandedIndex == repairs.indexOf(repair),
                            onClick = {
                                expandedIndex = if (expandedIndex == repairs.indexOf(repair)) null else repairs.indexOf(repair)
                            },
                            onEdit = { updatedRepair ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        vehicleService.updateRepair(updatedRepair)
                                        withContext(Dispatchers.Main) {
                                            refreshTrigger++
                                            snackbarHostState.showSnackbar(
                                                message = "Naprawa została zaktualizowana",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            snackbarHostState.showSnackbar(
                                                message = "Błąd podczas aktualizacji naprawy",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                }
                            },
                            onDelete = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        vehicleService.deleteRepair(repair)
                                        withContext(Dispatchers.Main) {
                                            refreshTrigger++
                                            snackbarHostState.showSnackbar(
                                                message = "Naprawa została usunięta",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            snackbarHostState.showSnackbar(
                                                message = "Błąd podczas usuwania naprawy",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                }
                            },
                            snackbarHostState = snackbarHostState
                        )
                    }
                } else {
                    item {
                        // Przycisk dodawania naprawy
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
                                contentDescription = if (showAddForm) "Anuluj" else "Dodaj naprawę",
                                modifier = Modifier.size(20.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (showAddForm) "Anuluj" else "Dodaj naprawę",
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
                                        text = "Dodaj naprawę",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF2E7D32),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )

                                    repairsInputField.forEach { inputField ->
                                        OutlinedTextField(
                                            value = inputRepairsFieldValues[inputField] ?: "",
                                            onValueChange = { newValue ->
                                                inputRepairsFieldValues =
                                                    inputRepairsFieldValues.toMutableMap().apply {
                                                        this[inputField] = newValue
                                                    }
                                            },
                                            label = { Text(inputField.label) },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = when (inputField.label) {
                                                        "Opis" -> Icons.Default.Description
                                                        "Koszt" -> Icons.Default.AttachMoney
                                                        "Data naprawy" -> Icons.Default.DateRange
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
                                                inputRepairsFieldValues.mapKeys { it.key.label }

                                            val description = fieldValues["Opis"] ?: ""
                                            val costStr = fieldValues["Koszt"] ?: ""
                                            val date = fieldValues["Data naprawy"] ?: ""

                                            if (description.isBlank() || costStr.isBlank() || date.isBlank()) {
                                                CoroutineScope(Dispatchers.Main).launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = "Wszystkie pola muszą być wypełnione",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                }
                                                return@Button
                                            }

                                            val cost = costStr.toDoubleOrNull()
                                            if (cost == null) {
                                                CoroutineScope(Dispatchers.Main).launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = "Koszt musi być liczbą",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                }
                                                return@Button
                                            }

                                            val repair = Repair(LocalDate.parse(date).toString(), description, cost)

                                            CoroutineScope(Dispatchers.IO).launch {
                                                try {
                                                    vehicleService.assignRepairToVehicle(repair, currentVehicle)
                                                    withContext(Dispatchers.Main) {
                                                        showAddForm = false
                                                        refreshTrigger++
                                                        // Resetuj wartości formularza
                                                        inputRepairsFieldValues = repairsInputField.associateWith { "" }
                                                        snackbarHostState.showSnackbar(
                                                            message = "Naprawa została dodana",
                                                            duration = SnackbarDuration.Short
                                                        )
                                                    }
                                                } catch (e: Exception) {
                                                    withContext(Dispatchers.Main) {
                                                        snackbarHostState.showSnackbar(
                                                            message = "Błąd podczas dodawania naprawy",
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
                                            text = "Dodaj naprawę",
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
                                        imageVector = Icons.Default.Build,
                                        contentDescription = "Brak napraw",
                                        modifier = Modifier.size(48.dp),
                                        tint = Color(0xFF9E9E9E)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Brak historii napraw",
                                        fontSize = 16.sp,
                                        color = Color(0xFF757575),
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "Dodaj pierwszą naprawę klikając przycisk powyżej",
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

                // Jeśli są naprawy, pokazuj przycisk dodawania na dole
                if (repairs.isNotEmpty()) {
                    item {
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
                                contentDescription = if (showAddForm) "Anuluj" else "Dodaj naprawę",
                                modifier = Modifier.size(20.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (showAddForm) "Anuluj" else "Dodaj kolejną naprawę",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }

                    if (showAddForm) {
                        item {
                            // Formularz dodawania (identyczny jak powyżej)
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
                                        text = "Dodaj naprawę",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF2E7D32),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )

                                    repairsInputField.forEach { inputField ->
                                        OutlinedTextField(
                                            value = inputRepairsFieldValues[inputField] ?: "",
                                            onValueChange = { newValue ->
                                                inputRepairsFieldValues =
                                                    inputRepairsFieldValues.toMutableMap().apply {
                                                        this[inputField] = newValue
                                                    }
                                            },
                                            label = { Text(inputField.label) },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = when (inputField.label) {
                                                        "Opis" -> Icons.Default.Description
                                                        "Koszt" -> Icons.Default.AttachMoney
                                                        "Data naprawy" -> Icons.Default.DateRange
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
                                                inputRepairsFieldValues.mapKeys { it.key.label }

                                            val description = fieldValues["Opis"] ?: ""
                                            val costStr = fieldValues["Koszt"] ?: ""
                                            val date = fieldValues["Data naprawy"] ?: ""

                                            if (description.isBlank() || costStr.isBlank() || date.isBlank()) {
                                                CoroutineScope(Dispatchers.Main).launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = "Wszystkie pola muszą być wypełnione",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                }
                                                return@Button
                                            }

                                            val cost = costStr.toDoubleOrNull()
                                            if (cost == null) {
                                                CoroutineScope(Dispatchers.Main).launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = "Koszt musi być liczbą",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                }
                                                return@Button
                                            }

                                            val repair = Repair(LocalDate.parse(date).toString(), description, cost)

                                            CoroutineScope(Dispatchers.IO).launch {
                                                try {
                                                    vehicleService.assignRepairToVehicle(repair, currentVehicle)
                                                    withContext(Dispatchers.Main) {
                                                        showAddForm = false
                                                        refreshTrigger++
                                                        // Resetuj wartości formularza
                                                        inputRepairsFieldValues = repairsInputField.associateWith { "" }
                                                        snackbarHostState.showSnackbar(
                                                            message = "Naprawa została dodana",
                                                            duration = SnackbarDuration.Short
                                                        )
                                                    }
                                                } catch (e: Exception) {
                                                    withContext(Dispatchers.Main) {
                                                        snackbarHostState.showSnackbar(
                                                            message = "Błąd podczas dodawania naprawy",
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
                                            text = "Dodaj naprawę",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White
                                        )
                                    }
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
}

@Composable
fun RepairItem(
    repair: Repair,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onEdit: (Repair) -> Unit,
    onDelete: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var showEditRepairDialog by remember { mutableStateOf(false) }

    // LISTA inputFieldów dla edycji
    val repairsInputField = listOf(
        repairInputField("Opis", repair.description),
        repairInputField("Koszt", repair.cost.toString()),
        repairInputField("Data naprawy", repair.repairDate.toString())
    )

    // Lista z wartościami
    var inputRepairsFieldValues by remember { mutableStateOf(repairsInputField.associateWith { it.value }) }

    // Ustaw aktualne wartości gdy dialog się otwiera
    LaunchedEffect(showEditRepairDialog) {
        if (showEditRepairDialog) {
            inputRepairsFieldValues = mapOf(
                repairInputField("Opis", "") to repair.description,
                repairInputField("Koszt", "") to repair.cost.toString(),
                repairInputField("Data naprawy", "") to repair.repairDate.toString()
            )
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = "Naprawa",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = repair.description,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Zwiń" else "Rozwiń",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))

                // Informacje o naprawie
                InfoRowRepair(
                    icon = Icons.Default.DateRange,
                    label = "Data naprawy",
                    value = repair.repairDate.toString()
                )

                InfoRowRepair(
                    icon = Icons.Default.AttachMoney,
                    label = "Koszt",
                    value = "${repair.cost} PLN"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Przyciski akcji
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showEditRepairDialog = true },
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
                        onClick = onDelete,
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

    // Dialog edycji
    if (showEditRepairDialog) {
        CustomModalDialog(
            onDismiss = { showEditRepairDialog = false },
            title = "Edytuj naprawę",
            onConfirm = {
                val fieldValues = inputRepairsFieldValues.mapKeys { it.key.label }
                val description = fieldValues["Opis"] ?: ""
                val costStr = fieldValues["Koszt"] ?: ""
                val date = fieldValues["Data naprawy"] ?: ""

                if (description.isBlank() || costStr.isBlank() || date.isBlank()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        snackbarHostState.showSnackbar(
                            message = "Wszystkie pola muszą być wypełnione",
                            duration = SnackbarDuration.Short
                        )
                    }
                    return@CustomModalDialog
                }

                val cost = costStr.toDoubleOrNull()
                if (cost == null) {
                    CoroutineScope(Dispatchers.Main).launch {
                        snackbarHostState.showSnackbar(
                            message = "Koszt musi być liczbą",
                            duration = SnackbarDuration.Short
                        )
                    }
                    return@CustomModalDialog
                }

                // Aktualizuj wartości naprawy
                repair.description = description
                repair.cost = cost
                repair.repairDate = date

                onEdit(repair)
                showEditRepairDialog = false
            },
            content = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    repairsInputField.forEach { inputField ->
                        OutlinedTextField(
                            value = inputRepairsFieldValues[inputField] ?: "",
                            onValueChange = { newValue ->
                                inputRepairsFieldValues = inputRepairsFieldValues.toMutableMap().apply {
                                    this[inputField] = newValue
                                }
                            },
                            label = { Text(inputField.label) },
                            leadingIcon = {
                                Icon(
                                    imageVector = when (inputField.label) {
                                        "Opis" -> Icons.Default.Description
                                        "Koszt" -> Icons.Default.AttachMoney
                                        "Data naprawy" -> Icons.Default.DateRange
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
                }
            }
        )
    }
}

@Composable
fun InfoRowRepair(
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
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF757575),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 14.sp,
                color = Color(0xFF2E7D32),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

//@Composable
//fun CustomModalDialog(
//    onDismiss: () -> Unit,
//    title: String,
//    onConfirm: () -> Unit,
//    content: @Composable () -> Unit
//) {
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = {
//            Text(
//                text = title,
//                fontSize = 18.sp,
//                fontWeight = FontWeight.SemiBold,
//                color = Color(0xFF2E7D32)
//            )
//        },
//        text = {
//            content()
//        },
//        confirmButton = {
//            Button(
//                onClick = onConfirm,
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(0xFF4CAF50)
//                ),
//                shape = RoundedCornerShape(8.dp)
//            ) {
//                Text(
//                    text = "Zapisz",
//                    color = Color.White,
//                    fontWeight = FontWeight.Medium
//                )
//            }
//        },
//        dismissButton = {
//            TextButton(
//                onClick = onDismiss,
//                colors = ButtonDefaults.textButtonColors(
//                    contentColor = Color(0xFF757575)
//                )
//            ) {
//                Text(
//                    text = "Anuluj",
//                    fontWeight = FontWeight.Medium
//                )
//            }
//        },
//        containerColor = Color.White,
//        shape = RoundedCornerShape(16.dp)
//    )
//}