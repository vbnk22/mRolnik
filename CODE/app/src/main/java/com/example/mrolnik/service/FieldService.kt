package com.example.mrolnik.service

import android.util.Log
import com.example.mrolnik.config.SupabaseClient
import com.example.mrolnik.model.Animal
import io.github.jan.supabase.postgrest.from
import com.example.mrolnik.model.Field
import io.github.jan.supabase.postgrest.query.Columns

class FieldService {
    val supabase = SupabaseClient().getSupabaseClient()
    var userId: Int = UserService.getLoggedUserId()
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

    suspend fun getAllByUserId(): List<Field> {
        var usersFields: List<Field> = emptyList()
        try {
            val usersFieldsId = supabase.from("user_field")
                .select(columns = Columns.list("fieldId")) {
                    filter {
                        eq("userId", userId)
                    }
                }
                .decodeList<Map<String, Int>>()
                .mapNotNull { it["fieldId"] }

            usersFields = supabase.from("field").select {
                filter {
                    isIn("fieldId", usersFieldsId)
                }
            }
                .decodeList<Field>()
        } catch (e: Exception) {
            Log.e("FieldService", "Fetching user's fields error ${e.message}")
        }
        return usersFields
    }

    suspend fun updateField(field: Field) {
        try {
            supabase.from("field").update(
                {
                    set("fieldName", field.fieldName)
                }
            ) {
                filter {
                    eq("fieldId", field.fieldId)
                }
            }
        } catch (e: Exception) {
            Log.e("FieldService", "Updating field data error ${e.message}")
        }
    }

    suspend fun deleteField(field: Field) {
        try {
            supabase.from("field").delete {
                filter {
                    eq("fieldId", field.fieldId)
                }
            }
        } catch (e: Exception) {
            Log.e("FieldService", "Deleting field error ${e.message}")
        }
    }
}