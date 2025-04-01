package com.example.mrolnik.service

import android.util.Log
import com.example.mrolnik.config.SupabaseClient
import io.github.jan.supabase.postgrest.from
import com.example.mrolnik.model.Warehouse

class WarehouseService {
    val supabase = SupabaseClient().getSupabaseClient()
    var userId: Int = 0
    var resultOfInsert = Warehouse("")

    suspend fun addWarehouse(warehouse: Warehouse): Boolean {
        try {
            resultOfInsert = supabase.from("warehouse").insert(
                mapOf(
                    "warehouseName" to warehouse.warehouseName,
                )
            ) {
                select()
            }.decodeSingle<Warehouse>()
            return true
        } catch (e: Exception) {
            Log.e("WarehouseService", "Adding warehouse to database error: ${e.message}")
            return false
        }
    }

    suspend fun addWarehouseIdToAssociationTable() {
        try {
            userId = UserService.getLoggedUserId()
            supabase.from("user_warehouse").insert(
                mapOf(
                    "userId" to userId,
                    "warehouseId" to resultOfInsert.warehouseId
                )
            )
        } catch (e: Exception) {
            Log.e("AnimalService", "Adding animalId to association table error: ${e.message}")
        }
    }
}