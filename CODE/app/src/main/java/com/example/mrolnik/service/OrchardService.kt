package com.example.mrolnik.service

import android.util.Log
import com.example.mrolnik.config.SupabaseClient
import com.example.mrolnik.model.Animal
import com.example.mrolnik.model.Orchard
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class OrchardService {
    val supabase = SupabaseClient().getSupabaseClient()
    var userId: Int = 0
    var resultOfInsert = Orchard("")

    suspend fun addOrchard(orchard: Orchard): Boolean {
        try {
            resultOfInsert = supabase.from("orchard").insert(
                mapOf(
                    "orchardName" to orchard.orchardName
                )
            ) {
                select()
            }.decodeSingle<Orchard>()
            return true
        } catch (e: Exception) {
            Log.e("OrchardService", "Adding orchard to database error: ${e.message}")
            return false
        }
    }

    suspend fun addOrchardIdToAssociationTable() {
        try {
            userId = UserService.getLoggedUserId()
            supabase.from("user_orchard").insert(
                mapOf(
                    "userId" to userId,
                    "orchardId" to resultOfInsert.orchardId,
                )
            )
        } catch (e: Exception) {
            Log.e("OrchardService", "Adding orchardId to association table error: ${e.message}")
        }
    }

    suspend fun getAllByUserId(): List<Orchard> {
        var usersOrchards: List<Orchard> = emptyList()
        try {
            val userId = UserService.getLoggedUserId()

            val usersOrchardsId = supabase.from("user_orchard")
                .select(columns = Columns.list("orchardId")) {
                    filter {
                        eq("userId", userId)
                    }
                }
                .decodeList<Map<String, Int>>()
                .mapNotNull { it["orchardId"] }

            usersOrchards = supabase.from("orchard").select {
                filter {
                    isIn("orchardId", usersOrchardsId)
                }
            }
                .decodeList<Orchard>()
        } catch (e: Exception) {
            Log.e("OrchardService", "Fetching user's orchards error ${e.message}")
        }
        return usersOrchards
    }

    suspend fun updateOrchard(orchard: Orchard) {
        try {
            supabase.from("orchard").update(
                {
                    set("orchardName", orchard.orchardName)
                }
            ) {
                filter {
                    eq("orchardId", orchard.orchardId)
                }
            }
        } catch (e: Exception) {
            Log.e("OrchardService", "Updating orchard data error ${e.message}")
        }
    }

    suspend fun deleteOrchard(orchard: Orchard) {
        try {
            supabase.from("orchard").delete {
                filter {
                    eq("orchardId", orchard.orchardId)
                }
            }
        } catch (e: Exception) {
            Log.e("OrchardService", "Deleting orchard error ${e.message}")
        }
    }
}