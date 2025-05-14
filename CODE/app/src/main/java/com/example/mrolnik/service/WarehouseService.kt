package com.example.mrolnik.service

import android.util.Log
import com.example.mrolnik.config.SupabaseClient
import com.example.mrolnik.model.Resource
import io.github.jan.supabase.postgrest.from
import com.example.mrolnik.model.Warehouse
import io.github.jan.supabase.postgrest.query.Columns

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

    suspend fun getAllByUserId(): List<Warehouse> {
        var usersWarehouses: List<Warehouse> = emptyList()
        try {
            val userId = UserService.getLoggedUserId()

            val usersWarehousesId = supabase.from("user_warehouse")
                .select(columns = Columns.list("warehouseId")) {
                    filter {
                        eq("userId", userId)
                    }
                }
                .decodeList<Map<String, Int>>()
                .mapNotNull { it["warehouseId"] }

            usersWarehouses = supabase.from("warehouse").select {
                filter {
                    isIn("warehouseId", usersWarehousesId)
                }
            }
                .decodeList<Warehouse>()
        } catch (e: Exception) {
            Log.e("WarehouseService", "Fetching user's warehouses error ${e.message}")
        }
        return usersWarehouses
    }

    suspend fun updateWarehouse(warehouse: Warehouse) {
        try {
            supabase.from("warehouse").update(
                {
                    set("warehouseName", warehouse.warehouseName)
                }
            ) {
                filter {
                    eq("warehouseId", warehouse.warehouseId)
                }
            }
        } catch (e: Exception) {
            Log.e("WarehouseService", "Updating warehouse data error ${e.message}")
        }
    }

    suspend fun deleteWarehouse(warehouse: Warehouse) {
        try {
            supabase.from("warehouse").delete {
                filter {
                    eq("warehouseId", warehouse.warehouseId)
                }
            }
        } catch (e: Exception) {
            Log.e("WarehouseService", "Deleting warehouse error ${e.message}")
        }
    }

    suspend fun assignResourceToWarehouse(resource: Resource, warehouse: Warehouse?) {
        try {
            supabase.from("resource").insert(
                mapOf(
                    "name" to resource.name,
                    "quantity" to resource.quantity,
                    "unitMeasures" to resource.unitMeasures,
                    "warehouseId" to warehouse?.warehouseId
                ))
        } catch (e: Exception) {
            Log.e("WarehouseService", "Assigning resource to warehouse error: ${e.message}")
        }
    }

    suspend fun updateResource(resource: Resource) {
        try {
            supabase.from("resource").update(
                {
                    set("name", resource.name)
                    set("quantity", resource.quantity)
                    set("unitMeasures", resource.unitMeasures)
                }
            ) {
                filter {
                    eq("resourceId", resource.resourceId)
                }
            }
        } catch (e: Exception) {
            Log.e("WarehouseService", "Updating resource error: ${e.message}")
        }
    }

    suspend fun deleteResource(resource: Resource) {
        try {
            supabase.from("resource").delete{
                filter {
                    eq("resourceId", resource.resourceId)
                }
            }
        } catch (e: Exception) {
            Log.e("WarehouseService", "Deleting resource error: ${e.message}")
        }
    }

    suspend fun getAllResourcesByWarehouseId(warehouse: Warehouse?): List<Resource> {
        var warehousesResources: List<Resource> = emptyList()
        try {
            warehousesResources = supabase.from("resource")
                .select {
                    filter {
                        eq("warehouseId", warehouse!!.warehouseId)
                    }
                }
                .decodeList<Resource>()
        } catch (e: Exception) {
            Log.e("WarehouseService", "Fetching user's resources error ${e.message}")
        }
        return warehousesResources
    }
}