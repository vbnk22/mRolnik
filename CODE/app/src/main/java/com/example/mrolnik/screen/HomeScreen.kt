package com.example.mrolnik.screen


import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Announcement
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

data class MenuItem(
    val title: String,
    val route: String,
    val icon: ImageVector,
    val color: Color
)

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen()
                }
            }
        }
    }
}

@Composable
fun HomeScreen() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomePage(navController)
        }
        composable("animals") {
            AnimalsManagementScreen(navController)
        }
        composable("chat") {
            ChatScreen(navController)
        }
        composable("fields") {
            FieldManagementScreen(navController)
        }
        composable("marketplace") {
            MarketplaceScreen(navController)
        }
        composable("orchard") {
            OrchardManagementScreen(navController)
        }
        composable("planner") {
            PlannerScreen(navController)
        }
        composable("vehicle") {
            VehicleManagementScreen(navController)
        }
        composable("warehouse") {
            WarehouseManagementScreen(navController)
        }
        composable("announcement") {
            AnnouncementScreen(navController)
        }
        composable("resources") {
            ResourcesManagementScreen(navController)
        }
        composable("vehicleInformation") {
            VehicleInformationScreen(navController)
        }
        composable("vehicleRepairs") {
            VehicleRepairHistoryScreen(navController)
        }
        composable("fruitTrees") {
            FruitTreesScreen(navController)
        }
        composable("sprayingHistory") {
            SprayingHistoryScreen(navController)
        }
        composable("fertilizerHistory") {
            FertilizerHistoryScreen(navController)
        }
        composable("cultivations"){
            CultivationsScreen(navController)
        }
        composable("sprayingCultivationHistory"){
            SprayingCultivationHistoryScreen(navController)
        }
        composable(
            "chatMessages/{chatRoomId}/{otherUserName}",
            arguments = listOf(
                navArgument("chatRoomId") { type = NavType.IntType },
                navArgument("otherUserName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val chatRoomId = backStackEntry.arguments?.getInt("chatRoomId") ?: 0
            val otherUserName = backStackEntry.arguments?.getString("otherUserName") ?: ""
            ChatMessagesScreen(
                chatRoomId = chatRoomId,
                otherUserName = otherUserName,
                navController = navController
            )
        }
    }
}

@Composable
fun HomePage(navController: NavController) {
    val menuItems = listOf(
        MenuItem("Zarządzanie zwierzętami", "animals", Icons.Default.Pets, Color(0xFF4CAF50)),
        MenuItem("Zarządzanie polami", "fields", Icons.Default.Grass, Color(0xFF8BC34A)),
        MenuItem("Zarządzanie sadami", "orchard", Icons.Default.LocalFlorist, Color(0xFFFF9800)),
        MenuItem("Zarządzanie pojazdami", "vehicle", Icons.Default.DirectionsCar, Color(0xFF2196F3)),
        MenuItem("Zarządzanie magazynami", "warehouse", Icons.Default.Warehouse, Color(0xFF9C27B0)),
        MenuItem("Ogłoszenia", "announcement", Icons.Default.Announcement, Color(0xFFF44336)),
        MenuItem("Chat", "chat", Icons.Default.Chat, Color(0xFF00BCD4)),
        MenuItem("Rynek", "marketplace", Icons.Default.ShoppingCart, Color(0xFF607D8B)),
        MenuItem("Planer", "planner", Icons.Default.CalendarMonth, Color(0xFF795548))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Nagłówek
        Text(
            text = "System Zarządzania Gospodarstwem",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            textAlign = TextAlign.Center
        )

        // Grid z przyciskami
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(menuItems) { item ->
                MenuCard(
                    item = item,
                    onClick = { navController.navigate(item.route) }
                )
            }
        }
    }
}

@Composable
fun MenuCard(
    item: MenuItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp,
            pressedElevation = 8.dp
        )
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxSize(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = item.color
            ),
            elevation = null,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .padding(bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        modifier = Modifier.size(32.dp),
                        tint = item.color
                    )
                }
                Text(
                    text = item.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 14.sp
                )
            }
        }
    }
}