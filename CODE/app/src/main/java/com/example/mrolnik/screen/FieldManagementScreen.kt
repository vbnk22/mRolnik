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
import com.example.mrolnik.model.Field
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
    var newFieldName by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    var fields by remember { mutableStateOf(emptyList<Field>()) }
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
                    text = "Zarządzanie polami",
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
                    contentDescription = if (showForm) "Anuluj" else "Dodaj pole",
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (showForm) "Anuluj" else "Dodaj pole",
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
                            text = "Dodaj nowe pole",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = newFieldName,
                            onValueChange = { newFieldName = it },
                            label = { Text("Nazwa pola") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Landscape,
                                    contentDescription = "Nazwa pola",
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

                                if (newFieldName.isBlank()) {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Nazwa pola nie może być pusta",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    return@Button
                                }

                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val field = Field(newFieldName)
                                        fieldService.addField(field)
                                        fieldService.addFieldIdToAssociationTable()

                                        val fetchedFields = fieldService.getAllByUserId()

                                        withContext(Dispatchers.Main) {
                                            fields = fetchedFields
                                            newFieldName = ""
                                            showForm = false
                                            snackbarHostState.showSnackbar(
                                                message = "Pole zostało dodane",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            snackbarHostState.showSnackbar(
                                                message = "Błąd podczas dodawania pola",
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
                                text = "Dodaj pole",
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
                text = "Twoje pola",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2E7D32),
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Lista pól
            LaunchedEffect(Unit) {
                val fetchedFields = withContext(Dispatchers.IO) {
                    fieldService.getAllByUserId()
                }
                fields = fetchedFields
            }

            if (fields.isEmpty()) {
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
                            imageVector = Icons.Default.Landscape,
                            contentDescription = "Brak pól",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF9E9E9E)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Brak pól",
                            fontSize = 16.sp,
                            color = Color(0xFF757575),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Dodaj pierwsze pole klikając przycisk powyżej",
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
                    items(fields) { field ->
                        FieldRow(
                            field = field,
                            navController = navController,
                            onFieldUpdated = {
                                // Odśwież listę po aktualizacji
                                CoroutineScope(Dispatchers.IO).launch {
                                    val fetchedFields = fieldService.getAllByUserId()
                                    withContext(Dispatchers.Main) {
                                        fields = fetchedFields
                                    }
                                }
                            },
                            onFieldDeleted = {
                                // Odśwież listę po usunięciu
                                CoroutineScope(Dispatchers.IO).launch {
                                    val fetchedFields = fieldService.getAllByUserId()
                                    withContext(Dispatchers.Main) {
                                        fields = fetchedFields
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
fun FieldRow(
    field: Field,
    navController: NavController,
    onFieldUpdated: () -> Unit,
    onFieldDeleted: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val sharedViewModel = LocalSharedViewModel.current

    // Lista pól, które chcemy edytować
    val inputFields = listOf(
        fieldInputField("Nazwa", field.fieldName)
    )

    // Stan dla dynamicznie tworzonych inputów
    var inputFieldValues by remember { mutableStateOf(inputFields.associateWith { it.value }) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = field.fieldName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2E7D32)
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
                                fieldService.deleteField(field)
                                withContext(Dispatchers.Main) {
                                    onFieldDeleted()
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    snackbarHostState.showSnackbar(
                                        message = "Błąd podczas usuwania pola",
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
                        sharedViewModel.selectField(field)
                        navController.navigate("cultivations")
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Szczegóły",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    if (showDialog) {
        CustomModalDialog(
            onDismiss = { showDialog = false },
            title = "Edytuj: ${field.fieldName}",
            onConfirm = {
                val newFieldName = inputFieldValues.getValue(fieldInputField("Nazwa", field.fieldName))

                if (newFieldName.isBlank()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        snackbarHostState.showSnackbar(
                            message = "Nazwa pola nie może być pusta",
                            duration = SnackbarDuration.Short
                        )
                    }
                    return@CustomModalDialog
                }

                field.fieldName = newFieldName

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        fieldService.updateField(field)
                        withContext(Dispatchers.Main) {
                            onFieldUpdated()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            snackbarHostState.showSnackbar(
                                message = "Błąd podczas aktualizacji pola",
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

data class fieldInputField(val label: String, val value: String)