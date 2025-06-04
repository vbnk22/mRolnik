package com.example.mrolnik.screen

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
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
import com.example.mrolnik.service.MarketplaceService
import com.example.mrolnik.service.UserService
import com.example.mrolnik.service.OfferService
import com.example.mrolnik.model.MarketplaceItem
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

var userService = UserService()
var marketplaceService = MarketplaceService()
@Composable

fun MarketplaceScreen(navController: NavController) {
    // Form fields
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var image by remember { mutableStateOf("") }
    var publicationDate by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(true) }
    val userNames = remember { mutableStateMapOf<Int, String>() }

    var expandedItemId by remember { mutableStateOf<Int?>(null) }

    val loggedInUser = UserService.getLoggedUser().firstName + " " + UserService.getLoggedUser().lastName
    val loggedInUserId:Int = UserService.getLoggedUserId()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var itemBeingEdited by remember { mutableStateOf<MarketplaceItem?>(null) }

    var showDeleteDialog by remember { mutableStateOf(false) }

    val backIcon = painterResource(R.drawable.baseline_arrow_back)
    val coroutineScope = rememberCoroutineScope()
    var marketplaceItems by remember { mutableStateOf<List<MarketplaceItem>>(emptyList()) }
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    LaunchedEffect(Unit) {
        try {
            Log.d("Marketplace", "Pobieram oferty...")
            val items = marketplaceService.getAllMarketplaceItems()
            Log.d("Marketplace", "Pobrano: ${items.size}")
            marketplaceItems = items

        } catch (e: Exception) {
            Log.e("MarketplaceScreen", "Błąd przy pobieraniu ofert: ${e.message}")
        }
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
            Button(onClick = {
                showOnlyMyOffers = !showOnlyMyOffers

                coroutineScope.launch {
                    try {
                        marketplaceItems = if (showOnlyMyOffers) {
                            marketplaceService.getMarketplaceItemsByUserId(UserService.getLoggedUserId())
                        } else {
                            marketplaceService.getAllMarketplaceItems()
                        }
                    } catch (e: Exception) {
                        Log.e("MarketplaceScreen", "Błąd przy filtrowaniu ofert: ${e.message}")
                    }
                }
            }) {
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
            items(marketplaceItems.size) { index ->
                val item = marketplaceItems[index]
                MarketplaceItemCard(
                    item = item,
                    isExpanded = expandedItemId == item.marketplaceItemId,
                    onClick = {
                        expandedItemId = if (expandedItemId == item.marketplaceItemId) null else item.marketplaceItemId
                    },
                    onMessageClick = {
                        // TODO: przenieść użytkownika do widoku odpowiedniego chatRoom
                        coroutineScope.launch {
                            chatService.createChatRoom(loggedInUserId, item.userId)
                        }
                    },
                    onDelete = { itemToDelete ->
                        coroutineScope.launch {
                            try {
                                marketplaceService.deleteMarketplaceItem(itemToDelete)
                                marketplaceItems = marketplaceItems.filterNot {
                                    it.marketplaceItemId == itemToDelete.marketplaceItemId
                                }
                            } catch (e: Exception) {
                                Log.e("MarketplaceScreen", "Błąd przy usuwaniu oferty: ${e.message}")
                            }
                        }
                    },
                    onUpdate = { updatedItem ->
                        coroutineScope.launch {
                            try {
                                marketplaceService.updateMarketplaceItem(updatedItem)
                                Log.d("MarketplaceScreen", "Oferta zaktualizowana: ${updatedItem.title}")
                                marketplaceItems = marketplaceItems.map {
                                    if (it.marketplaceItemId == updatedItem.marketplaceItemId) updatedItem else it
                                }
                            } catch (e: Exception) {
                                Log.e("MarketplaceScreen", "Błąd przy aktualizacji oferty: ${e.message}")
                            }
                        }
                    }
                )
            }
        }
        fun uriToByteArray(context: Context, uri: Uri): ByteArray {
            return context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes()
            } ?: ByteArray(0)
        }


        if (showAddDialog) {
            CustomModalDialog(
                onDismiss = { showAddDialog = false },
                title = "Nowe ogłoszenie",

                onConfirm = {
                    if (title.isNotBlank() && description.isNotBlank()) {
                        // Przygotuj plik obrazu z selectedImageUri, jeśli jest
                        val success = if (selectedImageUri != null) {
                            // Konwersja URI na ByteArray i nazwa pliku (np. tytuł lub timestamp)
                            val imageBytes = uriToByteArray(context, selectedImageUri!!)
                            val imageName = "image_${System.currentTimeMillis()}"

                            // Wywołaj suspend function addMarketplaceItem
                            runBlocking {
                                marketplaceService.addMarketplaceItem(
                                    MarketplaceItem(
                                        userId = UserService.getLoggedUserId(),
                                        title = title,
                                        description = description,
                                        price = price.toDoubleOrNull() ?: 0.0,
                                        image = null, // imageURL ustawisz po uploadzie w bazie, więc tutaj null
                                        publicationDate = publicationDate,
                                        location = location,
                                        isActive = isActive
                                    ),
                                    imageBytes,
                                    imageName
                                )
                            }
                        } else {
                            // Jeśli brak obrazka, możesz dodać item bez imageBytes i imageName (inny wariant metody)
                            runBlocking {
                                marketplaceService.addMarketplaceItem(
                                    MarketplaceItem(
                                        userId = UserService.getLoggedUserId(),
                                        title = title,
                                        description = description,
                                        price = price.toDoubleOrNull() ?: 0.0,
                                        image = null,
                                        publicationDate = publicationDate,
                                        location = location,
                                        isActive = isActive
                                    ),
                                    ByteArray(0), // lub zmodyfikuj funkcję by obsługiwała brak obrazka
                                    ""
                                )
                            }
                        }

                        // Wyczyść pola
                        title = ""
                        description = ""
                        price = ""
                        publicationDate = ""
                        location = ""
                        isActive = true
                        selectedImageUri = null
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
                        Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                            Text(text = "Wybierz obrazek")
                        }
                        selectedImageUri?.let { uri ->
                            Text(text = "Wybrano obrazek: ${uri.lastPathSegment ?: uri.toString()}")
                        }
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
    onDelete: (MarketplaceItem) -> Unit = {},
    onUpdate: (MarketplaceItem) -> Unit = {},
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
                if (item.image != null) {
                    SubcomposeAsyncImage(
                        model = item.image,
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
                    // SPRZEDAJĄCY
                    val sellerName = remember { mutableStateOf("") }

                    LaunchedEffect(item.userId) {
                        try {
                            val name = userService.getNameFromId(item.userId)
                            sellerName.value = name ?: ""
                        } catch (e: Exception) {
                            Log.e("MarketplaceScreen", "Nie udało się pobrać imienia i nazwiska: ${e.message}")
                        }
                    }

                    if (sellerName.value.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sprzedający: ",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = sellerName.value,
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
                            onDelete(item)
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
                var editedPublicationDate by remember { mutableStateOf(item.publicationDate) }
                var editedLocation by remember { mutableStateOf(item.location) }
                var editedIsActive by remember { mutableStateOf(item.isActive) }

                CustomModalDialog(
                    onDismiss = { showEditDialog = false },
                    title = "Edytuj ogłoszenie",
                    onConfirm = {
                        val updatedItem = MarketplaceItem(
                            userId = item.userId,
                            title = editedTitle,
                            description = editedDescription,
                            price = editedPrice.toDoubleOrNull() ?: 0.0,
                            image = item.image,
                            publicationDate = editedPublicationDate,
                            location = editedLocation,
                            isActive = editedIsActive
                        )
                        updatedItem.marketplaceItemId = item.marketplaceItemId


                        // Przykład:
                        onUpdate(updatedItem)

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