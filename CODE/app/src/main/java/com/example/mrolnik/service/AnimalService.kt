package com.example.mrolnik.service

import android.util.Log
import com.example.mrolnik.config.SupabaseClient
import io.github.jan.supabase.postgrest.from
import com.example.mrolnik.model.Animal
import com.example.mrolnik.model.Field
import io.github.jan.supabase.postgrest.query.Columns

class AnimalService {
    val supabase = SupabaseClient().getSupabaseClient()
    var userId: Int = UserService.getLoggedUserId()
    var resultOfInsert = Animal("",0)

    suspend fun addAnimal(animal: Animal): Boolean {
        try {
            resultOfInsert = supabase.from("animal").insert(
                mapOf(
                    "species" to animal.species,
                    "numberOfAnimals" to animal.numberOfAnimals
                )
            ) {
                select()
            }.decodeSingle<Animal>()
            return true
        } catch (e: Exception) {
            Log.e("AnimalService", "Adding animal to database error: ${e.message}")
            return false
        }
    }

    suspend fun addAnimalIdToAssociationTable() {
        try {
            supabase.from("user_animal").insert(
                mapOf(
                    "userId" to userId,
                    "animalId" to resultOfInsert.animalId
                )
            )
        } catch (e: Exception) {
            Log.e("AnimalService", "Adding animalId to association table error: ${e.message}")
        }
    }

    suspend fun getAllByUserId(): List<Animal> {
        var usersAnimals: List<Animal> = emptyList()
        try {
            val usersAnimalsId = supabase.from("user_animal")
                .select(columns = Columns.list("animalId")) {
                    filter {
                        eq("userId", userId)
                    }
                }
                .decodeList<Map<String, Int>>()
                .mapNotNull { it["animalId"] }

            usersAnimals = supabase.from("animal").select {
                filter {
                    isIn("animalId", usersAnimalsId)
                }
            }
                .decodeList<Animal>()
        } catch (e: Exception) {
            Log.e("AnimalService", "Fetching user's animals error ${e.message}")
        }
        return usersAnimals
    }

    suspend fun updateAnimal(animal: Animal) {
        try {
            supabase.from("animal").update(
                {
                    set("species", animal.species)
                    set("numberOfAnimals", animal.numberOfAnimals)
                }
            ) {
                filter {
                    eq("animalId", animal.animalId)
                }
            }
        } catch (e: Exception) {
            Log.e("AnimalService", "Updating animal data error ${e.message}")
        }
    }

    suspend fun deleteAnimal(animal: Animal) {
        try {
            supabase.from("animal").delete {
                filter {
                    eq("animalId", animal.animalId)
                }
            }
        } catch (e: Exception) {
            Log.e("AnimalService", "Deleting animal error ${e.message}")
        }
    }
}