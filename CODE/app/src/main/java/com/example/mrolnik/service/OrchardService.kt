package com.example.mrolnik.service

import android.util.Log
import com.example.mrolnik.config.SupabaseClient
import com.example.mrolnik.model.Orchard
import io.github.jan.supabase.postgrest.from

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
}