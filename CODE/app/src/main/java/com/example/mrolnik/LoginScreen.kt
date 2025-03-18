package com.example.mrolnik

import android.widget.Toast
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
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

    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
    val context = LocalContext.current

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
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val result = supabaseClient
                            .postgrest["user"] //ZMIENIĆ NAZWE KLASY NA PRAWIDLOWA USER2 TO MOJA TESTOWA
                            .select {
                                filter {
                                    eq("login", login.value)
                                    eq("password", password.value)
                                }
                            }
                            .decodeList<User>()

                        if (result.isNotEmpty()) {

                            withContext(Dispatchers.Main) {
                                navController.navigate("home")
                                snackbarHostState.showSnackbar(
                                    message = "Wynik zapytania: ${result}",
                                    duration = androidx.compose.material3.SnackbarDuration.Short
                                )



                            }
                        }
                        else {
                            withContext(Dispatchers.Main) {
                                snackbarHostState.showSnackbar(
                                    message = "Wynik zapytania: ${result}",
                                    duration = androidx.compose.material3.SnackbarDuration.Short
                                )
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
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
