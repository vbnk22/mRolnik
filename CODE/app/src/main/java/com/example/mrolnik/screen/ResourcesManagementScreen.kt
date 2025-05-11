package com.example.mrolnik.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavController
import com.example.mrolnik.viewmodel.LocalSharedViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mrolnik.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class resourcesInputField(val label: String, val value: String)

@Composable
fun ResourcesManagementScreen(navController: NavController) {
    val sharedWarehouseViewModel = LocalSharedViewModel.current
    val warehouseState = sharedWarehouseViewModel.selectedWarehouse.collectAsState()

    val currentWarehouse = warehouseState.value // TODO: Wybrany magazyn wieć możesz korzystać z ID i po tym wyszukiwać Resources

    var showAddRecourcesDialog by remember { mutableStateOf(false) }
    val backIcon = painterResource(R.drawable.baseline_arrow_back)
    val addIcon = painterResource(id = R.drawable.baseline_add)

    // TODO: LISTA inputFieldów dla dodawania zasobu
    val resourcesInputField = listOf(
        resourcesInputField("Nazwa", ""), resourcesInputField("Ilość", ""), resourcesInputField("Jednoska", "")
    )

    // TODO: Lista z wartościami
    var inputResourcesFieldValues by remember { mutableStateOf(resourcesInputField.associateWith { it.value }) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
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
                text = "Zarządzanie zasobami",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center
            )

        }

        Button(
            onClick = { showAddRecourcesDialog = true },
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Icon(
                painter = addIcon,
                contentDescription = "EDIT",
                modifier = Modifier.size(24.dp)
            )
        }

        if(showAddRecourcesDialog){
            CustomModalDialog(
                onDismiss = { showAddRecourcesDialog = false },
                title = "Dodaj zasoby",
                onConfirm = {
                    // TODO: zrobić dodawanie zasobów do magazynu możesz do tego użyć currentWarehouse.warehouseId
                    // Jest zrobione tak jak w edycjach za pomocą CustomDialog wiec chyba możesz przekopiować i pozmieniać niektóre elementy
                },
                content = {
                    resourcesInputField.forEach { inputField ->
                        TextField(
                            value = inputResourcesFieldValues[inputField] ?: "",
                            onValueChange = { newValue ->
                                inputResourcesFieldValues = inputResourcesFieldValues.toMutableMap().apply {
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