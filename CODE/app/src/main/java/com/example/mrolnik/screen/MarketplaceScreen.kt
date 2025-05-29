package com.example.mrolnik.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.example.mrolnik.R
import com.example.mrolnik.model.Offer
import com.example.mrolnik.service.UserService
import com.example.mrolnik.service.OfferService
import kotlinx.coroutines.launch

data class MarketplaceItem(
    val id: Int,
    val title: String,
    val description: String,
    val price: Double,
    val imageURL: String? = null,
    val publicationDate: String = "",
    val location: String = "",
    val seller: String = "",
    val isActive: Boolean = true
)

data class marketplaceOffertInputField(val label: String, val value: String)

@Composable
fun MarketplaceScreen(navController: NavController) {
    val sampleItems = listOf(
        MarketplaceItem(
            id = 1,
            title = "iPhone 14 Pro - stan idealny",
            description = "Sprzedam iPhone 14 Pro w kolorze Space Black. Telefon używany przez 6 miesięcy, zawsze w etui i z folią ochronną. Bateria w idealnym stanie - 100% pojemności.",
            price = 3500.0,
            imageURL = "https://example.com/iphone.jpg",
            publicationDate = "2024-01-15",
            location = "Warszawa, Mokotów",
            seller = "Jan Kowalski",
            isActive = true
        ),
        MarketplaceItem(
            id = 2,
            title = "Rower górski Trek X-Caliber 8",
            description = "Sprzedam rower górski w bardzo dobrym stanie. Przejechane około 2000 km. Regularnie serwisowany.",
            price = 2800.0,
            imageURL = null, // brak obrazka
            publicationDate = "2024-01-10",
            location = "Kraków",
            seller = "Anna Nowak",
            isActive = false
        )
    )

    // Form fields
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var imageURL by remember { mutableStateOf("") }
    var publicationDate by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(true) }
    val userNames = remember { mutableStateMapOf<Int, String>() }

    val coroutineScope = rememberCoroutineScope()
    var expandedItemId by remember { mutableStateOf<Int?>(null) }

    val loggedInUser = UserService.getLoggedUser().firstName + " " + UserService.getLoggedUser().lastName
    val loggedInUserId:Int = UserService.getLoggedUserId()

    var showAddDialog by remember { mutableStateOf(false) }

    var showDeleteDialog by remember { mutableStateOf(false) }

    val backIcon = painterResource(R.drawable.baseline_arrow_back)

    LaunchedEffect(Unit) {

    }

    var showOnlyMyOffers by remember { mutableStateOf(false) }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)) {
            IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(painter = backIcon, contentDescription = "Wróć", modifier = Modifier.size(24.dp))
            }

            Text(
                text = "Rynek",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { showAddDialog = true }) {
                Text("Dodaj ofertę")
            }
            Button(onClick = { showOnlyMyOffers = !showOnlyMyOffers }) {
                Text(if (showOnlyMyOffers) "Wszystkie oferty" else "Moje oferty")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sampleItems.size) { index ->
                val item = sampleItems[index]
                MarketplaceItemCard(
                    item = item,
                    isExpanded = expandedItemId == item.id,
                    onClick = {
                        expandedItemId = if (expandedItemId == item.id) null else item.id
                    },
                    onMessageClick = {
                        // Obsługa kliknięcia w przycisk wiadomości
                        // Tutaj możesz dodać nawigację do ekranu czatu
                    }
                )
            }
        }

        if (showAddDialog) {
            CustomModalDialog(
                onDismiss = { showAddDialog = false },
                title = "Nowe ogłoszenie",
                onConfirm = {
                    if (title.isNotBlank() && description.isNotBlank()) {
                        val newItem = MarketplaceItem(
                            title = title,
                            description = description,
                            price = price.toDoubleOrNull() ?: 0.0,
                            imageURL = imageURL.ifBlank { null },
                            publicationDate = publicationDate,
                            location = location,
                            isActive = isActive,
                            id = 3,
                            seller = "Jan Nowak",
                        )

                        title = ""
                        description = ""
                        price = ""
                        imageURL = ""
                        publicationDate = ""
                        location = ""
                        isActive = true
                    }
                    showAddDialog = false
                },
                content = {
                    Text("Dodajesz jako: $loggedInUser", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    Column {
                        TextField(value = title, onValueChange = { title = it }, label = { Text("Tytuł") })
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(value = description, onValueChange = { description = it }, label = { Text("Opis") })
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(value = price, onValueChange = { price = it }, label = { Text("Cena") })
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(value = imageURL, onValueChange = { imageURL = it }, label = { Text("URL obrazka") })
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(value = publicationDate, onValueChange = { publicationDate = it }, label = { Text("Data publikacji") })
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(value = location, onValueChange = { location = it }, label = { Text("Lokalizacja") })
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isActive, onCheckedChange = { isActive = it })
                            Text("Aktywne")
                        }
                    }
                }
            )
        }
    }
}


@Composable
fun MarketplaceItemCard(
    item: MarketplaceItem,
    isExpanded: Boolean = false,
    onClick: () -> Unit = {},
    onMessageClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {

    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Obrazek na górze - pełna szerokość
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                if (item.imageURL != null) {
                    SubcomposeAsyncImage(
                        model = item.imageURL,
                        contentDescription = item.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "Błąd ładowania obrazka",
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                } else {
                    // Placeholder gdy brak obrazka
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Brak obrazka",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Zawartość karty
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Nagłówek z tytułem, ceną i przyciskiem rozwijania
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        // Tytuł
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Cena
                        Text(
                            text = "${String.format("%.2f", item.price)} zł",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Przycisk rozwijania
                    IconButton(onClick = onClick) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = if (isExpanded) "Zwiń" else "Rozwiń",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Rozwinięte informacje
                if (isExpanded) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Opis
                    if (item.description.isNotBlank()) {
                        Text(
                            text = "Opis:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Data publikacji
                    if (item.publicationDate.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Data publikacji: ",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = item.publicationDate,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Lokalizacja
                    if (item.location.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Lokalizacja: ",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = item.location,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Sprzedawca
                    if (item.seller.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sprzedawca: ",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = item.seller,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Status aktywności
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Status: ",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (item.isActive) "Aktywne" else "Nieaktywne",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (item.isActive)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // TODO: Wyświetlić odpowiednie przyciski
                    // Przycisk akcji
                    Button(
                        onClick = onMessageClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Napisz wiadomość")
                    }

                    Button(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Edytuj")
                    }

                    Button(
                        onClick = {
                        //TODO: Usuwanie oferty
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Usuń")
                    }
                }
            }

            if (showEditDialog) {
                var editedTitle by remember { mutableStateOf(item.title) }
                var editedDescription by remember { mutableStateOf(item.description) }
                var editedPrice by remember { mutableStateOf(item.price.toString()) }
                var editedImageURL by remember { mutableStateOf(item.imageURL ?: "") }
                var editedPublicationDate by remember { mutableStateOf(item.publicationDate) }
                var editedLocation by remember { mutableStateOf(item.location) }
                var editedIsActive by remember { mutableStateOf(item.isActive) }

                CustomModalDialog(
                    onDismiss = { showEditDialog = false },
                    title = "Edytuj ogłoszenie",
                    onConfirm = {
                        val updatedItem = item.copy(
                            title = editedTitle,
                            description = editedDescription,
                            price = editedPrice.toDoubleOrNull() ?: 0.0,
                            imageURL = editedImageURL.ifBlank { null },
                            publicationDate = editedPublicationDate,
                            location = editedLocation,
                            isActive = editedIsActive
                        )

                        // Tutaj możesz wysłać aktualizację do bazy lub przekazać przez callback
                        // Przykład:
                        // onUpdate(updatedItem)

                        showEditDialog = false
                    },
                    content = {
                        Column {
                            TextField(value = editedTitle, onValueChange = { editedTitle = it }, label = { Text("Tytuł") })
                            Spacer(modifier = Modifier.height(8.dp))
                            TextField(value = editedDescription, onValueChange = { editedDescription = it }, label = { Text("Opis") })
                            Spacer(modifier = Modifier.height(8.dp))
                            TextField(value = editedPrice, onValueChange = { editedPrice = it }, label = { Text("Cena") })
                            Spacer(modifier = Modifier.height(8.dp))
                            TextField(value = editedImageURL, onValueChange = { editedImageURL = it }, label = { Text("URL obrazka") })
                            Spacer(modifier = Modifier.height(8.dp))
                            TextField(value = editedPublicationDate, onValueChange = { editedPublicationDate = it }, label = { Text("Data publikacji") })
                            Spacer(modifier = Modifier.height(8.dp))
                            TextField(value = editedLocation, onValueChange = { editedLocation = it }, label = { Text("Lokalizacja") })
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = editedIsActive, onCheckedChange = { editedIsActive = it })
                                Text("Aktywne")
                            }
                        }
                    }
                )
            }
        }
    }
}