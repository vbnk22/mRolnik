package com.example.mrolnik.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mrolnik.service.UserService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(navController: NavController) {
    val email = remember { mutableStateOf("") }
    val imie = remember { mutableStateOf("") }
    val nazwisko = remember { mutableStateOf("") }
    val login = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val confirmPassword = remember { mutableStateOf("") }
    val passwordVisible = remember { mutableStateOf(false) }
    val confirmPasswordVisible = remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val userService = UserService()
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Logo/Ikona aplikacji
            Card(
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(40.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF4CAF50)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Agriculture,
                        contentDescription = "Logo",
                        modifier = Modifier.size(40.dp),
                        tint = Color.White
                    )
                }
            }

            // Tytuł
            Text(
                text = "Rejestracja",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Text(
                text = "Stwórz nowe konto",
                fontSize = 14.sp,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Formularz rejestracji
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Pola imię i nazwisko w jednym rzędzie
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = imie.value,
                            onValueChange = { imie.value = it },
                            label = { Text("Imię", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Imię",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4CAF50),
                                focusedLabelColor = Color(0xFF4CAF50)
                            )
                        )

                        OutlinedTextField(
                            value = nazwisko.value,
                            onValueChange = { nazwisko.value = it },
                            label = { Text("Nazwisko", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.AccountBox,
                                    contentDescription = "Nazwisko",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4CAF50),
                                focusedLabelColor = Color(0xFF4CAF50)
                            )
                        )
                    }

                    // Email
                    OutlinedTextField(
                        value = email.value,
                        onValueChange = { email.value = it },
                        label = { Text("Email", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4CAF50),
                            focusedLabelColor = Color(0xFF4CAF50)
                        )
                    )

                    // Login
                    OutlinedTextField(
                        value = login.value,
                        onValueChange = { login.value = it },
                        label = { Text("Login", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Login",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4CAF50),
                            focusedLabelColor = Color(0xFF4CAF50)
                        )
                    )

                    // Hasło
                    OutlinedTextField(
                        value = password.value,
                        onValueChange = { password.value = it },
                        label = { Text("Hasło", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Hasło",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { passwordVisible.value = !passwordVisible.value }
                            ) {
                                Icon(
                                    imageVector = if (passwordVisible.value)
                                        Icons.Default.VisibilityOff
                                    else
                                        Icons.Default.Visibility,
                                    contentDescription = if (passwordVisible.value)
                                        "Ukryj hasło"
                                    else
                                        "Pokaż hasło",
                                    tint = Color(0xFF666666),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible.value)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4CAF50),
                            focusedLabelColor = Color(0xFF4CAF50)
                        )
                    )

                    // Powtórz hasło
                    OutlinedTextField(
                        value = confirmPassword.value,
                        onValueChange = { confirmPassword.value = it },
                        label = { Text("Powtórz hasło", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Powtórz hasło",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { confirmPasswordVisible.value = !confirmPasswordVisible.value }
                            ) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible.value)
                                        Icons.Default.VisibilityOff
                                    else
                                        Icons.Default.Visibility,
                                    contentDescription = if (confirmPasswordVisible.value)
                                        "Ukryj hasło"
                                    else
                                        "Pokaż hasło",
                                    tint = Color(0xFF666666),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible.value)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4CAF50),
                            focusedLabelColor = Color(0xFF4CAF50)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Przycisk rejestracji
                    Button(
                        onClick = {
                            keyboardController?.hide()

                            if (email.value.isBlank() || login.value.isBlank() || password.value.isBlank() || confirmPassword.value.isBlank() || imie.value.isBlank() || nazwisko.value.isBlank()) {
                                imie.value = ""
                                nazwisko.value = ""
                                login.value = ""
                                password.value = ""
                                confirmPassword.value = ""
                                email.value = ""
                                focusManager.clearFocus()
                                CoroutineScope(Dispatchers.Main).launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Wszystkie pola muszą być uzupełnione",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                                return@Button
                            }

                            val digitsOrSpecials = Regex(".*[\\d\\W].*")
                            if (digitsOrSpecials.containsMatchIn(imie.value) || digitsOrSpecials.containsMatchIn(nazwisko.value)) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    imie.value = ""
                                    nazwisko.value = ""
                                    login.value = ""
                                    password.value = ""
                                    confirmPassword.value = ""
                                    email.value = ""
                                    focusManager.clearFocus()
                                    snackbarHostState.showSnackbar(
                                        message = "Imię lub nazwisko nie mogą zawierać cyfr lub znaków specjalnych",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                                return@Button
                            }

                            val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}\$")
                            if (!emailRegex.matches(email.value)) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    imie.value = ""
                                    nazwisko.value = ""
                                    login.value = ""
                                    password.value = ""
                                    confirmPassword.value = ""
                                    email.value = ""
                                    focusManager.clearFocus()
                                    snackbarHostState.showSnackbar(
                                        message = "Niepoprawny format e-maila",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                                return@Button
                            }

                            if (login.value.equals(password.value)) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    login.value = ""
                                    password.value = ""
                                    confirmPassword.value = ""
                                    snackbarHostState.showSnackbar(
                                        message = "Login i hasło nie mogą być takie same",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                                return@Button
                            }

                            if (password.value != confirmPassword.value) {
                                password.value = ""
                                confirmPassword.value = ""
                                CoroutineScope(Dispatchers.Main).launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Hasła nie są identyczne",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                                return@Button
                            }

                            val passwordRegex = Regex("^(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$")
                            if (!passwordRegex.matches(password.value)) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    password.value = ""
                                    confirmPassword.value = ""
                                    snackbarHostState.showSnackbar(
                                        message = "Hasło musi mieć min. 8 znaków, 1 dużą literę i 1 znak specjalny",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                                return@Button
                            }

                            CoroutineScope(Dispatchers.IO).launch {
                                if (userService.registerUser(imie, nazwisko, login, password, email)) {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Rejestracja zakończona sukcesem",
                                            duration = SnackbarDuration.Short
                                        )
                                        navController.navigate("login")
                                    }
                                } else {
                                    snackbarHostState.showSnackbar(
                                        message = "Błąd podczas rejestracji",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(26.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Text(
                            text = "Zarejestruj się",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Link do logowania
            TextButton(
                onClick = { navController.navigate("login") }
            ) {
                Text(
                    text = "Masz już konto? Zaloguj się",
                    fontSize = 14.sp,
                    color = Color(0xFF4CAF50)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
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