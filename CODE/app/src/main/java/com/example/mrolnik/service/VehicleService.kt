package com.example.mrolnik.service

import android.util.Log
import com.example.mrolnik.config.SupabaseClient
import com.example.mrolnik.model.Vehicle
import io.github.jan.supabase.postgrest.from

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
            Log.e("VehicleService", "Adding vehicle to database error: ${e.message}")
        }
    }
}