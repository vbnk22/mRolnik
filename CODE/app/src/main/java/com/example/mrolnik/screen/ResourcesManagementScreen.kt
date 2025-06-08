package com.example.mrolnik.screen

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavController
import com.example.mrolnik.viewmodel.LocalSharedViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mrolnik.R
import com.example.mrolnik.model.Repair
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.mrolnik.model.Resource
import com.example.mrolnik.service.VehicleService
import com.example.mrolnik.service.WarehouseService
import kotlinx.datetime.LocalDate
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

data class resourcesInputField(val label: String, val value: String)

@Composable
fun ResourcesManagementScreen(navController: NavController) {
    val sharedWarehouseViewModel = LocalSharedViewModel.current
    val warehouseState = sharedWarehouseViewModel.selectedWarehouse.collectAsState()
    val currentWarehouse = warehouseState.value

    var showForm by remember { mutableStateOf(false) }
    var resources by remember { mutableStateOf(emptyList<Resource>()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // LISTA inputFieldów dla dodawania zasobu
    val resourcesInputField = listOf(
        resourcesInputField("Nazwa", ""),
        resourcesInputField("Ilość", ""),
        resourcesInputField("Jednostka", "")
    )

    // Lista z wartościami
    var inputResourcesFieldValues by remember { mutableStateOf(resourcesInputField.associateWith { it.value }) }

    val warehouseService = WarehouseService()

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
                    text = "Zarządzanie zasobami",
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
                    contentDescription = if (showForm) "Anuluj" else "Dodaj zasób",
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (showForm) "Anuluj" else "Dodaj zasób",
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
                            text = "Dodaj nowy zasób",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        resourcesInputField.forEach { inputField ->
                            OutlinedTextField(
                                value = inputResourcesFieldValues[inputField] ?: "",
                                onValueChange = { newValue ->
                                    inputResourcesFieldValues = inputResourcesFieldValues.toMutableMap().apply {
                                        this[inputField] = newValue
                                    }
                                },
                                label = { Text(inputField.label) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when (inputField.label) {
                                            "Nazwa" -> Icons.Default.Inventory
                                            "Ilość" -> Icons.Default.Numbers
                                            "Jednostka" -> Icons.Default.Scale
                                            else -> Icons.Default.Info
                                        },
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

                                val fieldValues = inputResourcesFieldValues.mapKeys { it.key.label }
                                val name = fieldValues["Nazwa"] ?: ""
                                val quantityStr = fieldValues["Ilość"] ?: ""
                                val unitMeasures = fieldValues["Jednostka"] ?: ""

                                if (name.isBlank() || quantityStr.isBlank() || unitMeasures.isBlank()) {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Wszystkie pola muszą być wypełnione",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    return@Button
                                }

                                val quantity = quantityStr.toDoubleOrNull()
                                if (quantity == null || quantity <= 0) {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Ilość musi być liczbą większą od zera",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    return@Button
                                }

                                val resource = Resource(name, quantity, unitMeasures)

                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        warehouseService.assignResourceToWarehouse(resource, currentWarehouse)
                                        val fetchedResources = warehouseService.getAllResourcesByWarehouseId(currentWarehouse)

                                        withContext(Dispatchers.Main) {
                                            resources = fetchedResources
                                            inputResourcesFieldValues = resourcesInputField.associateWith { "" }
                                            showForm = false
                                            snackbarHostState.showSnackbar(
                                                message = "Zasób został dodany",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            snackbarHostState.showSnackbar(
                                                message = "Błąd podczas dodawania zasobu",
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
                                text = "Dodaj zasób",
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
                text = "Zasoby w ${currentWarehouse?.warehouseName ?: "magazynie"}",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2E7D32),
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Lista zasobów
            LaunchedEffect(Unit) {
                val fetchedResources = withContext(Dispatchers.IO) {
                    warehouseService.getAllResourcesByWarehouseId(currentWarehouse)
                }
                resources = fetchedResources
            }

            if (resources.isEmpty()) {
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
                            imageVector = Icons.Default.Inventory,
                            contentDescription = "Brak zasobów",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF9E9E9E)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Brak zasobów",
                            fontSize = 16.sp,
                            color = Color(0xFF757575),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Dodaj pierwszy zasób klikając przycisk powyżej",
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
                    items(resources) { resource ->
                        ResourceRow(
                            resource = resource,
                            navController = navController,
                            onResourceUpdated = {
                                // Odśwież listę po aktualizacji
                                CoroutineScope(Dispatchers.IO).launch {
                                    val fetchedResources = warehouseService.getAllResourcesByWarehouseId(currentWarehouse)
                                    withContext(Dispatchers.Main) {
                                        resources = fetchedResources
                                    }
                                }
                            },
                            onResourceDeleted = {
                                // Odśwież listę po usunięciu
                                CoroutineScope(Dispatchers.IO).launch {
                                    val fetchedResources = warehouseService.getAllResourcesByWarehouseId(currentWarehouse)
                                    withContext(Dispatchers.Main) {
                                        resources = fetchedResources
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
fun ResourceRow(
    resource: Resource,
    navController: NavController,
    onResourceUpdated: () -> Unit,
    onResourceDeleted: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDetails by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val warehouseService = WarehouseService()

    val inputFields = listOf(
        resourcesInputField("Nazwa", resource.name),
        resourcesInputField("Ilość", resource.quantity.toString()),
        resourcesInputField("Jednostka", resource.unitMeasures)
    )

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
                        text = resource.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        text = "${resource.quantity} ${resource.unitMeasures}",
                        fontSize = 14.sp,
                        color = Color(0xFF757575),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { showEditDialog = true },
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
                                    warehouseService.deleteResource(resource)
                                    withContext(Dispatchers.Main) {
                                        onResourceDeleted()
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        snackbarHostState.showSnackbar(
                                            message = "Błąd podczas usuwania zasobu",
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Szczegóły zasobu",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2E7D32)
                        )

                        Row {
                            Text(
                                text = "Nazwa: ",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF757575)
                            )
                            Text(
                                text = resource.name,
                                fontSize = 14.sp,
                                color = Color(0xFF2E2E2E)
                            )
                        }

                        Row {
                            Text(
                                text = "Ilość: ",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF757575)
                            )
                            Text(
                                text = resource.quantity.toString(),
                                fontSize = 14.sp,
                                color = Color(0xFF2E2E2E)
                            )
                        }

                        Row {
                            Text(
                                text = "Jednostka: ",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF757575)
                            )
                            Text(
                                text = resource.unitMeasures,
                                fontSize = 14.sp,
                                color = Color(0xFF2E2E2E)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        CustomModalDialog(
            onDismiss = { showEditDialog = false },
            title = "Edytuj: ${resource.name}",
            onConfirm = {
                val fieldValues = inputFieldValues.mapKeys { it.key.label }
                val name = fieldValues["Nazwa"] ?: ""
                val quantityStr = fieldValues["Ilość"] ?: ""
                val unitMeasures = fieldValues["Jednostka"] ?: ""

                if (name.isBlank() || quantityStr.isBlank() || unitMeasures.isBlank()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        snackbarHostState.showSnackbar(
                            message = "Wszystkie pola muszą być wypełnione",
                            duration = SnackbarDuration.Short
                        )
                    }
                    return@CustomModalDialog
                }

                val quantity = quantityStr.toDoubleOrNull()
                if (quantity == null || quantity <= 0) {
                    CoroutineScope(Dispatchers.Main).launch {
                        snackbarHostState.showSnackbar(
                            message = "Ilość musi być liczbą większą od zera",
                            duration = SnackbarDuration.Short
                        )
                    }
                    return@CustomModalDialog
                }

                resource.name = name
                resource.quantity = quantity
                resource.unitMeasures = unitMeasures

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        warehouseService.updateResource(resource)
                        withContext(Dispatchers.Main) {
                            onResourceUpdated()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            snackbarHostState.showSnackbar(
                                message = "Błąd podczas aktualizacji zasobu",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                }
                showEditDialog = false
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