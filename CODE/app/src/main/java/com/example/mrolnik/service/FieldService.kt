package com.example.mrolnik.service

import android.util.Log
import com.example.mrolnik.config.SupabaseClient
import io.github.jan.supabase.postgrest.from
import com.example.mrolnik.model.Field

class FieldService {
    val supabase = SupabaseClient().getSupabaseClient()
    var userId: Int = 0
    var resultOfInsert = Field("")

    suspend fun addField(field: Field): Boolean {
        try {
            resultOfInsert = supabase.from("field").insert(
                mapOf(
                    "fieldName" to field.fieldName
                )
            ) {
                select()
            }.decodeSingle<Field>()
            return true
        } catch (e: Exception) {
            Log.e("FieldService", "Adding field to database error: ${e.message}")
            return false
        }
    }

    suspend fun addFieldIdToAssociationTable() {
        try {
            userId = UserService.getLoggedUserId()
            supabase.from("user_field").insert(
                mapOf(
                    "userId" to userId,
                    "fieldId" to resultOfInsert.fieldId,
                )
            )
        } catch (e: Exception) {
            Log.e("FieldService", "Adding fieldId to association table error: ${e.message}")
        }
    }
}