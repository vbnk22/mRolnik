package com.example.mrolnik.service

import android.util.Log
import com.example.mrolnik.config.SupabaseClient
import com.example.mrolnik.model.Animal
import com.example.mrolnik.model.Vehicle
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class VehicleService {
    val supabase = SupabaseClient().getSupabaseClient()
    var userId: Int = 0
    var resultOfInsert = Vehicle("", "")

    suspend fun addVehicle(vehicle: Vehicle): Boolean {
        try {
            resultOfInsert = supabase.from("vehicle").insert(
                mapOf(
                    "vehicleName" to vehicle.vehicleName,
                    "technicalCondition" to vehicle.technicalCondition
                )
            ) {
                select()
            }.decodeSingle<Vehicle>()
            return true
        } catch (e: Exception) {
            Log.e("VehicleService", "Adding vehicle to database error: ${e.message}")
            return false
        }
    }

    suspend fun addVehicleIdToAssociationTable() {
        try {
            userId = UserService.getLoggedUserId()
            supabase.from("user_vehicle").insert(
                mapOf(
                    "userId" to userId,
                    "vehicleId" to resultOfInsert.vehicleId,
                )
            )
        } catch (e: Exception) {
            Log.e("VehicleService", "Adding vehicleId to association table error: ${e.message}")
        }
    }

    suspend fun getAllByUserId(): List<Vehicle> {
        var usersVehicles: List<Vehicle> = emptyList()
        try {
            val userId = UserService.getLoggedUserId()

            val usersVehiclesId = supabase.from("user_vehicle")
                .select(columns = Columns.list("vehicleId")) {
                    filter {
                        eq("userId", userId)
                    }
                }
                .decodeList<Map<String, Int>>()
                .mapNotNull { it["vehicleId"] }

            usersVehicles = supabase.from("vehicle").select {
                filter {
                    isIn("vehicleId", usersVehiclesId)
                }
            }
                .decodeList<Vehicle>()
        } catch (e: Exception) {
            Log.e("VehicleService", "Fetching user's vehicles error ${e.message}")
        }
        return usersVehicles
    }
}