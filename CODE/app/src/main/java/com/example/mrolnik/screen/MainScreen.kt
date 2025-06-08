package com.example.mrolnik.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun MainScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4CAF50), // Zielony dla tematu rolniczego
                        Color(0xFF81C784),
                        Color(0xFFA5D6A7)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo/Ikona aplikacji
            Card(
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 32.dp),
                shape = RoundedCornerShape(60.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Agriculture,
                        contentDescription = "Logo Aplikacji",
                        modifier = Modifier.size(60.dp),
                        tint = Color(0xFF2E7D32)
                    )
                }
            }

            // Tytuł aplikacji
            Text(
                text = "Mój Rolnik",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Zarządzaj swoim gospodarstwem",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            // Przyciski
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ModernButton(
                    text = "Logowanie",
                    icon = Icons.Default.Person,
                    onClick = { navController.navigate("login") },
                    isPrimary = true
                )

                ModernButton(
                    text = "Rejestracja",
                    icon = Icons.Default.PersonAdd,
                    onClick = { navController.navigate("register") },
                    isPrimary = false
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Stopka
            Text(
                text = "Witaj w świecie nowoczesnego rolnictwa",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ModernButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isPrimary: Boolean
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPrimary) Color.White else Color.Transparent
        ),
        border = if (!isPrimary) ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.horizontalGradient(listOf(Color.White, Color.White))
        ) else null,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isPrimary) 4.dp else 0.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (isPrimary) Color(0xFF2E7D32) else Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (isPrimary) Color(0xFF2E7D32) else Color.White
            )
        }
    }
}
