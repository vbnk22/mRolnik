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
import com.example.mrolnik.model.FruitTree
import com.example.mrolnik.service.FruitTreeService
import com.example.mrolnik.viewmodel.LocalSharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class fruitTreeInputField(val label: String, val value: String)

val fruitTreeService = FruitTreeService()

@Composable
fun FruitTreesScreen(navController: NavController) {
    val sharedOrchardViewModel = LocalSharedViewModel.current
    val orchardState = sharedOrchardViewModel.selectedOrchard.collectAsState()
    val currentOrchard = orchardState.value

    var showForm by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var fruitTrees by remember { mutableStateOf(emptyList<FruitTree>()) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // LISTA inputFieldów dla dodawania drzewa
    val fruitTreesInputField = listOf(
        fruitTreeInputField("Nazwa", ""),
        fruitTreeInputField("Planowany zbiór", ""),
        fruitTreeInputField("Jakość oprysków", "")
    )

    // Lista z wartościami
    var inputFruitTreesFieldValues by remember { mutableStateOf(fruitTreesInputField.associateWith { it.value }) }

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
                    text = "Drzewka w sadzie ${currentOrchard?.orchardName ?: ""}",
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
                    contentDescription = if (showForm) "Anuluj" else "Dodaj drzewko",
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (showForm) "Anuluj" else "Dodaj drzewko",
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
                            text = "Dodaj nowe drzewko",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        fruitTreesInputField.forEach { inputField ->
                            OutlinedTextField(
                                value = inputFruitTreesFieldValues[inputField] ?: "",
                                onValueChange = { newValue ->
                                    inputFruitTreesFieldValues = inputFruitTreesFieldValues.toMutableMap().apply {
                                        this[inputField] = newValue
                                    }
                                },
                                label = { Text(inputField.label) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when (inputField.label) {
                                            "Nazwa" -> Icons.Default.Eco
                                            "Planowany zbiór" -> Icons.Default.CalendarToday
                                            "Jakość oprysków" -> Icons.Default.Visibility
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

                                val fieldValues = inputFruitTreesFieldValues.mapKeys { it.key.label }
                                val plantName = fieldValues["Nazwa"] ?: ""
                                val plannedHarvestDate = fieldValues["Planowany zbiór"] ?: ""
                                val usedSprayingQuantity = fieldValues["Jakość oprysków"]?.toDoubleOrNull() ?: 0.0

                                if (plantName.isBlank()) {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Nazwa drzewka nie może być pusta",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    return@Button
                                }

                                val fruitTree = FruitTree(plantName, plannedHarvestDate, usedSprayingQuantity)

                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        fruitTreeService.assignFruitTreeToOrchard(fruitTree, currentOrchard)
                                        val fetchedFruitTrees = fruitTreeService.getAllFruitTreesByOrchardId(currentOrchard)

                                        withContext(Dispatchers.Main) {
                                            fruitTrees = fetchedFruitTrees
                                            inputFruitTreesFieldValues = fruitTreesInputField.associateWith { "" }
                                            showForm = false
                                            snackbarHostState.showSnackbar(
                                                message = "Drzewko zostało dodane",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            snackbarHostState.showSnackbar(
                                                message = "Błąd podczas dodawania drzewka",
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
                                text = "Dodaj drzewko",
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
                text = "Twoje drzewka",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2E7D32),
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Ładowanie danych
            LaunchedEffect(Unit) {
                val fetchedFruitTrees = withContext(Dispatchers.IO) {
                    fruitTreeService.getAllFruitTreesByOrchardId(currentOrchard)
                }
                fruitTrees = fetchedFruitTrees
            }

            // Lista drzewek
            if (fruitTrees.isEmpty()) {
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
                            imageVector = Icons.Default.Eco,
                            contentDescription = "Brak drzewek",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF9E9E9E)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Brak drzewek",
                            fontSize = 16.sp,
                            color = Color(0xFF757575),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Dodaj pierwsze drzewko klikając przycisk powyżej",
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
                    items(fruitTrees) { fruitTree ->
                        FruitTreeRow(
                            fruitTree = fruitTree,
                            navController = navController,
                            onFruitTreeUpdated = {
                                // Odśwież listę po aktualizacji
                                CoroutineScope(Dispatchers.IO).launch {
                                    val fetchedFruitTrees = fruitTreeService.getAllFruitTreesByOrchardId(currentOrchard)
                                    withContext(Dispatchers.Main) {
                                        fruitTrees = fetchedFruitTrees
                                    }
                                }
                            },
                            onFruitTreeDeleted = {
                                // Odśwież listę po usunięciu
                                CoroutineScope(Dispatchers.IO).launch {
                                    val fetchedFruitTrees = fruitTreeService.getAllFruitTreesByOrchardId(currentOrchard)
                                    withContext(Dispatchers.Main) {
                                        fruitTrees = fetchedFruitTrees
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
fun FruitTreeRow(
    fruitTree: FruitTree,
    navController: NavController,
    onFruitTreeUpdated: () -> Unit,
    onFruitTreeDeleted: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val sharedViewModel = LocalSharedViewModel.current

    // Lista pól, które chcemy edytować
    val inputFields = listOf(
        fruitTreeInputField("Nazwa", fruitTree.plantName),
        fruitTreeInputField("Planowany zbiór", fruitTree.plannedHarvestDate),
        fruitTreeInputField("Jakość oprysków", fruitTree.usedSprayingQuantity.toString())
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
            // Główne informacje o drzewku
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = fruitTree.plantName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        text = "Planowany zbiór: ${fruitTree.plannedHarvestDate}",
                        fontSize = 14.sp,
                        color = Color(0xFF757575),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = "Jakość oprysków: ${fruitTree.usedSprayingQuantity}",
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
                                    fruitTreeService.deleteFruitTree(fruitTree)
                                    withContext(Dispatchers.Main) {
                                        onFruitTreeDeleted()
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        snackbarHostState.showSnackbar(
                                            message = "Błąd podczas usuwania drzewka",
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
                        onClick = {
                            sharedViewModel.selectFruitTree(fruitTree)
                            navController.navigate("sprayingHistory")
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Historia oprysków",
                            tint = Color(0xFF2196F3),
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
            title = "Edytuj: ${fruitTree.plantName}",
            onConfirm = {
                val fieldValues = inputFieldValues.mapKeys { it.key.label }
                val plantName = fieldValues["Nazwa"] ?: ""
                val plannedHarvestDate = fieldValues["Planowany zbiór"] ?: ""
                val usedSprayingQuantity = fieldValues["Jakość oprysków"]?.toDoubleOrNull() ?: 0.0

                if (plantName.isBlank()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        snackbarHostState.showSnackbar(
                            message = "Nazwa drzewka nie może być pusta",
                            duration = SnackbarDuration.Short
                        )
                    }
                    return@CustomModalDialog
                }

                fruitTree.plantName = plantName
                fruitTree.plannedHarvestDate = plannedHarvestDate
                fruitTree.usedSprayingQuantity = usedSprayingQuantity

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        fruitTreeService.updateFruitTree(fruitTree)
                        withContext(Dispatchers.Main) {
                            onFruitTreeUpdated()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            snackbarHostState.showSnackbar(
                                message = "Błąd podczas aktualizacji drzewka",
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