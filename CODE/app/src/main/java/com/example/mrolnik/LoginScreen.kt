package com.example.mrolnik

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.PasswordVisualTransformation
//import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mrolnik.model.User
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(navController: NavController) {
    val login = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val supabaseClient = remember { com.example.mrolnik.config.SupabaseClient().getSupabaseClient() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
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
                value = login.value,
                onValueChange = { login.value = it },
                label = { Text("Login") },
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            OutlinedTextField(
                value = password.value,
                onValueChange = { password.value = it },
                label = { Text("Hasło") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Button(onClick = {
                keyboardController?.hide()

                CoroutineScope(Dispatchers.IO).launch {
                    if (login.value.isBlank()) {
                        withContext(Dispatchers.Main) {
                            password.value = ""
                            focusManager.clearFocus()
                            snackbarHostState.showSnackbar(
                                message = "Login nie może być pusty",
                                duration = androidx.compose.material3.SnackbarDuration.Short
                            )
                        }
                        return@launch
                    }

                    if (password.value.isBlank()) {
                        withContext(Dispatchers.Main) {
                            login.value = ""
                            focusManager.clearFocus()
                            snackbarHostState.showSnackbar(
                                message = "Hasło nie może być puste",
                                duration = androidx.compose.material3.SnackbarDuration.Short
                            )
                        }
                        return@launch
                    }
                    try {
                        val result = supabaseClient
                            .postgrest["user"]
                            .select {
                                filter {
                                    eq("login", login.value)
                                    eq("password", password.value)
                                }
                            }
                            .decodeList<User>()

                        withContext(Dispatchers.Main) {
                            if (result.isNotEmpty()) {
                                snackbarHostState.showSnackbar(
                                    message = "Zalogowano",
                                    duration = androidx.compose.material3.SnackbarDuration.Short
                                )
                                navController.navigate("home")
                            } else {
                                login.value = ""
                                password.value = ""
                                focusManager.clearFocus()
                                snackbarHostState.showSnackbar(
                                    message = "Nieprawidłowy login lub hasło",
                                    duration = androidx.compose.material3.SnackbarDuration.Short
                                )
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            snackbarHostState.showSnackbar(
                                message = "Wystąpił błąd: ${e.message}",
                                duration = androidx.compose.material3.SnackbarDuration.Short
                            )
                        }
                    }
                }
            }) {
                Text("Zaloguj się")
            }


        }

        // Snackbar
        androidx.compose.material3.SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
