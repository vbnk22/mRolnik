package com.example.mrolnik

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable

fun RegisterScreen(navController: NavController) {
    val email = remember { mutableStateOf("") }
    val imie = remember { mutableStateOf("") }
    val nazwisko = remember { mutableStateOf("") }
    val login = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val confirmPassword = remember { mutableStateOf("") }
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = imie.value,
                onValueChange = { imie.value = it },
                label = { Text("Imie") },
                modifier = Modifier.fillMaxWidth(0.8f)
            )
            OutlinedTextField(
                value = nazwisko.value,
                onValueChange = { nazwisko.value = it },
                label = { Text("Nazwisko") },
                modifier = Modifier.fillMaxWidth(0.8f)
            )
            OutlinedTextField(
                value = email.value,
                onValueChange = { email.value = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(0.8f)
            )
            OutlinedTextField(
                value = login.value,
                onValueChange = { login.value = it },
                label = { Text("Login") },
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            OutlinedTextField(
                value = password.value,
                onValueChange = { password.value = it },
                label = { Text("Hasło") },
                visualTransformation = PasswordVisualTransformation(), // Ukrycie hasła
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            OutlinedTextField(
                value = confirmPassword.value,
                onValueChange = { confirmPassword.value = it },
                label = { Text("Powtórz hasło") },
                visualTransformation = PasswordVisualTransformation(), // Ukrycie hasła
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Button(onClick = {
                if (email.value.isBlank() || login.value.isBlank() || password.value.isBlank() || confirmPassword.value.isBlank()) {
                    // Walidacja pustych pól
                    navController.navigate("main")
                    return@Button
                }
                if (password.value != confirmPassword.value) {
                    CoroutineScope(Dispatchers.Main).launch {
                        snackbarHostState.showSnackbar(
                            message = "Hasła nie są identyczne",
                            duration = androidx.compose.material3.SnackbarDuration.Short
                        )

                    }

                    return@Button
                }

                val supabaseClient = com.example.mrolnik.config.SupabaseClient().getSupabaseClient()

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // INSERT do tabeli user2
                        supabaseClient
                            .postgrest["user2"]
                            .insert(
                                mapOf(
                                    "firstName" to imie.value,
                                    "lastName" to nazwisko.value,
                                    "login" to login.value,
                                    "password" to password.value,
                                    "email" to email.value
                                )
                            )

                        withContext(Dispatchers.Main) {
                            println("Rejestracja zakończona sukcesem")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            println("Błąd podczas rejestracji: ${e.message}")
                        }
                    }
                }
            }) {
                Text("Zarejestruj się")
            }
        }
    }
}