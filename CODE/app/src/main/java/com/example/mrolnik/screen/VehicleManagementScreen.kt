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
import com.example.mrolnik.model.Vehicle
import com.example.mrolnik.service.VehicleService
import com.example.mrolnik.viewmodel.LocalSharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

var vehicleService = VehicleService()

@Composable
fun VehicleManagementScreen(navController: NavController) {
    var showForm by remember { mutableStateOf(false) }
    var vehicleName by remember { mutableStateOf("") }
    var vehicleCondition by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    var vehicles by remember { mutableStateOf(emptyList<Vehicle>()) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

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
                    text = "Zarządzanie pojazdami",
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
                    contentDescription = if (showForm) "Anuluj" else "Dodaj pojazd",
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (showForm) "Anuluj" else "Dodaj pojazd",
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
                            text = "Dodaj nowy pojazd",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = vehicleName,
                            onValueChange = { vehicleName = it },
                            label = { Text("Nazwa pojazdu") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = "Nazwa pojazdu",
                                    tint = Color(0xFF4CAF50)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4CAF50),
                                focusedLabelColor = Color(0xFF4CAF50)
                            )
                        )

                        OutlinedTextField(
                            value = vehicleCondition,
                            onValueChange = { vehicleCondition = it },
                            label = { Text("Stan techniczny pojazdu") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = "Stan techniczny",
                                    tint = Color(0xFF4CAF50)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4CAF50),
                                focusedLabelColor = Color(0xFF4CAF50)
                            )
                        )

                        Button(
                            onClick = {
                                keyboardController?.hide()
                                focusManager.clearFocus()

                                if (vehicleName.isBlank()) {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Nazwa pojazdu nie może być pusta",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    return@Button
                                }

                                if (vehicleCondition.isBlank()) {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Stan techniczny nie może być pusty",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    return@Button
                                }

                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val vehicle = Vehicle(vehicleName, vehicleCondition)
                                        vehicleService.addVehicle(vehicle)
                                        vehicleService.addVehicleIdToAssociationTable()

                                        val fetchedVehicles = vehicleService.getAllVehiclesByUserId()

                                        withContext(Dispatchers.Main) {
                                            vehicles = fetchedVehicles
                                            vehicleName = ""
                                            vehicleCondition = ""
                                            showForm = false
                                            snackbarHostState.showSnackbar(
                                                message = "Pojazd został dodany",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            snackbarHostState.showSnackbar(
                                                message = "Błąd podczas dodawania pojazdu",
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
                                text = "Dodaj pojazd",
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
                text = "Twoje pojazdy",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2E7D32),
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Lista pojazdów
            LaunchedEffect(Unit) {
                val fetchedVehicles = withContext(Dispatchers.IO) {
                    vehicleService.getAllVehiclesByUserId()
                }
                vehicles = fetchedVehicles
            }

            if (vehicles.isEmpty()) {
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
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = "Brak pojazdów",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF9E9E9E)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Brak pojazdów",
                            fontSize = 16.sp,
                            color = Color(0xFF757575),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Dodaj pierwszy pojazd klikając przycisk powyżej",
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
                    items(vehicles) { vehicle ->
                        VehicleRow(
                            vehicle = vehicle,
                            navController = navController,
                            onVehicleUpdated = {
                                // Odśwież listę po aktualizacji
                                CoroutineScope(Dispatchers.IO).launch {
                                    val fetchedVehicles = vehicleService.getAllVehiclesByUserId()
                                    withContext(Dispatchers.Main) {
                                        vehicles = fetchedVehicles
                                    }
                                }
                            },
                            onVehicleDeleted = {
                                // Odśwież listę po usunięciu
                                CoroutineScope(Dispatchers.IO).launch {
                                    val fetchedVehicles = vehicleService.getAllVehiclesByUserId()
                                    withContext(Dispatchers.Main) {
                                        vehicles = fetchedVehicles
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

data class vehicleInputField(val label: String, val value: String)

@Composable
fun VehicleRow(
    vehicle: Vehicle,
    navController: NavController,
    onVehicleUpdated: () -> Unit,
    onVehicleDeleted: () -> Unit
) {
    val sharedViewModel = LocalSharedViewModel.current
    var showDialog by remember { mutableStateOf(false) }
    var showDetails by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Lista pól, które chcemy edytować, oparta na obiekcie Vehicle
    val inputFields = listOf(
        vehicleInputField("Nazwa", vehicle.vehicleName),
        vehicleInputField("Stan techniczny", vehicle.technicalCondition)
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
            modifier = Modifier.padding(16.dp)
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
                        text = vehicle.vehicleName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        text = "Stan: ${vehicle.technicalCondition}",
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
                                    vehicleService.deleteVehicle(vehicle)
                                    withContext(Dispatchers.Main) {
                                        onVehicleDeleted()
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        snackbarHostState.showSnackbar(
                                            message = "Błąd podczas usuwania pojazdu",
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

                    IconButton(
                        onClick = { showDetails = !showDetails },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                    ) {
                        Icon(
                            imageVector = if (showDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (showDetails) "Zwiń" else "Rozwiń",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (showDetails) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            sharedViewModel.selectVehicle(vehicle)
                            navController.navigate("vehicleInformation")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Szczegółowe informacje",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = {
                            sharedViewModel.selectVehicle(vehicle)
                            navController.navigate("vehicleRepairs")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "Naprawy",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Historia napraw",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        CustomModalDialog(
            onDismiss = { showDialog = false },
            title = "Edytuj: ${vehicle.vehicleName}",
            onConfirm = {
                val newVehicleName = inputFieldValues.getValue(vehicleInputField("Nazwa", vehicle.vehicleName))
                val newTechnicalCondition = inputFieldValues.getValue(vehicleInputField("Stan techniczny", vehicle.technicalCondition))

                if (newVehicleName.isBlank()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        snackbarHostState.showSnackbar(
                            message = "Nazwa pojazdu nie może być pusta",
                            duration = SnackbarDuration.Short
                        )
                    }
                    return@CustomModalDialog
                }

                if (newTechnicalCondition.isBlank()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        snackbarHostState.showSnackbar(
                            message = "Stan techniczny nie może być pusty",
                            duration = SnackbarDuration.Short
                        )
                    }
                    return@CustomModalDialog
                }

                vehicle.vehicleName = newVehicleName
                vehicle.technicalCondition = newTechnicalCondition

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        vehicleService.updateVehicle(vehicle)
                        withContext(Dispatchers.Main) {
                            onVehicleUpdated()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            snackbarHostState.showSnackbar(
                                message = "Błąd podczas aktualizacji pojazdu",
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