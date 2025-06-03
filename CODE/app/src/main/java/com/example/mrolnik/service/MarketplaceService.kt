package com.example.mrolnik.service

import java.io.File
import android.util.Log
import com.example.mrolnik.config.SupabaseClient
import com.example.mrolnik.model.MarketplaceItem
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.selects.select
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import io.github.jan.supabase.postgrest.query.Columns

class MarketplaceService {

    val supabase = SupabaseClient().getSupabaseClient()
    var userId: Int = UserService.getLoggedUserId()
    var resultOfInsert = MarketplaceItem(0,"","",
        0.0,"","","",false)

    suspend fun getAllMarketplaceItems(): List<MarketplaceItem> {
        return try {
            supabase
                .from("marketplaceItem")
                .select()
                .decodeList<MarketplaceItem>()
        } catch (e: Exception) {
            Log.e("MarketplaceService", "Error fetching marketplace items: ${e.message}", e)
            emptyList()
        }
    }
    suspend fun getMarketplaceItemsByUserId(userId: Int): List<MarketplaceItem> {
        return try {
            supabase
                .from("marketplaceItem")
                .select {
                    filter {
                        eq("userId", userId)
                    }
                }
                .decodeList<MarketplaceItem>()
        } catch (e: Exception) {
            Log.e("MarketplaceService", "Error fetching user's marketplace items: ${e.message}", e)
            emptyList()
        }
    }



    suspend fun addMarketplaceItem(item: MarketplaceItem, imageBytes: ByteArray, imageName: String): Boolean {
        val tempFile = File.createTempFile("upload_temp", ".jpg")
        tempFile.writeBytes(imageBytes)
        return try {
            // 1. Upload image to Supabase storage
            val bucketName = "marketplace-images"
            val imagePath = "$imageName.jpg"
            val uploadResult = supabase.storage
                .from(bucketName)
                .upload(path = imagePath, file = tempFile) {
                    upsert = true
                }

            // 2. Get public URL
            val publicURL = "https://${supabase.supabaseUrl}/storage/v1/object/public/$bucketName/$imagePath"


            // 3. Add item to database with image URL
            val result = supabase.from("marketplaceItem").insert(
                listOf(
                    mapOf(
                        "userId" to item.userId,
                        "title" to item.title,
                        "description" to item.description,
                        "price" to item.price,
                        "image" to publicURL,
                        "publicationDate" to item.publicationDate,
                        "location" to item.location,
                        "isActive" to item.isActive
                    )
                )
            ) {
                select()
            }.decodeSingle<MarketplaceItem>()


            true
        } catch (e: Exception) {
            Log.e("MarketplaceService", "Error adding item: ${e.message}")
            false
        }
    }




    suspend fun updateMarketplaceItem(item: MarketplaceItem) {
        try {
            supabase.from("marketplaceItem").update({
                set("title", item.title)
                set("description", item.description)
                set("price", item.price)
                set("publicationDate", item.publicationDate)
                set("location", item.location)
                set("isActive", item.isActive)
            }) {
                filter {
                    eq("marketplaceItemId", item.marketplaceItemId)
                }
            }

        } catch (e: Exception) {
            Log.e("MarketplaceService", "Updating marketplace item error: ${e.message}")
        }
    }




    suspend fun deleteMarketplaceItem(item: MarketplaceItem) {
        try {
            // Usuń rekord z bazy danych
            supabase.from("marketplaceItem").delete {
                filter {
                    eq("marketplaceItemId", item.marketplaceItemId)
                }
            }

            // Jeśli istnieje URL obrazka, spróbuj usunąć zdjęcie ze storage
            item.image?.let { url ->
                val bucketName = "marketplace-images"

                // Wyodrębnij ścieżkę pliku z pełnego URL
                val prefix = "storage/v1/object/public/$bucketName/"
                val index = url.indexOf(prefix)
                if (index != -1) {
                    val imagePath = url.substring(index + prefix.length)

                    // Usuń obraz ze storage
                    supabase.storage
                        .from(bucketName)
                        .delete(listOf(imagePath))
                }
            }

        } catch (e: Exception) {
            Log.e("MarketplaceService", "Deleting marketplace item error: ${e.message}")
        }
    }



}