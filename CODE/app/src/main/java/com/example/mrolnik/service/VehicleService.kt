package com.example.mrolnik.service

import android.util.Log
import androidx.collection.emptyObjectList
import com.example.mrolnik.config.SupabaseClient
import com.example.mrolnik.model.Repair
import com.example.mrolnik.model.Vehicle
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.datetime.LocalDate

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

    suspend fun getAllVehiclesByUserId(): List<Vehicle> {
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

    suspend fun getAllVehicles(): List<Vehicle> {
        var usersVehicles: List<Vehicle> = emptyList()
        try {
            usersVehicles = supabase.from("vehicle").select().decodeList<Vehicle>()
        } catch (e: Exception) {
            Log.e("VehicleService", "Updating vehicle data error ${e.message}")
        }
        return usersVehicles
    }

    suspend fun updateVehicle(vehicle: Vehicle) {
        try {
            supabase.from("vehicle").update(
                {
                    set("vehicleName", vehicle.vehicleName)
                }
            ) {
                filter {
                    eq("vehicleId", vehicle.vehicleId)
                }
            }
        } catch (e: Exception) {
            Log.e("VehicleService", "Updating vehicle data error ${e.message}")
        }
    }

    suspend fun deleteVehicle(vehicle: Vehicle) {
        try {
            supabase.from("vehicle").delete {
                filter {
                    eq("vehicleId", vehicle.vehicleId)
                }
            }
        } catch (e: Exception) {
            Log.e("VehicleService", "Deleting vehicle error ${e.message}")
        }
    }

    // Opcjonalnie jako parametr: vehicle albo vehicleId: Int
    suspend fun assignRepairToVehicle(repair: Repair, vehicle: Vehicle?) {
        try {
            supabase.from("repair").insert(
                mapOf(
                    "repairDate" to repair.repairDate,
                    "description" to repair.description,
                    "cost" to repair.cost,
                    "vehicleId" to vehicle?.vehicleId
                ))
        } catch (e: Exception) {
            Log.e("VehicleService", "Assigning repair to vehicle error: ${e.message}")
        }
    }

    suspend fun updateRepair(repair: Repair) {
        try {
            supabase.from("repair").update(
                {
                    set("repairDate", repair.repairDate)
                    set("description", repair.description)
                    set("cost", repair.cost)
                }
            ) {
                filter {
                    eq("repairId", repair.repairId)
                }
            }
        } catch (e: Exception) {
            Log.e("VehicleService", "Updating repair error: ${e.message}")
        }
    }

    suspend fun deleteRepair(repair: Repair) {
        try {
            supabase.from("repair").delete{
                filter {
                    eq("repairId", repair.repairId)
                }
            }
        } catch (e: Exception) {
            Log.e("VehicleService", "Deleting repair error: ${e.message}")
        }
    }

    suspend fun getAllRepairsByVehicleId(vehicle: Vehicle?): List<Repair> {
        var vehiclesRepairs: List<Repair> = emptyList()
        try {
            vehiclesRepairs = supabase.from("repair")
                .select {
                    filter {
                        eq("vehicleId", vehicle!!.vehicleId)
                    }
                }
                .decodeList<Repair>()
        } catch (e: Exception) {
            Log.e("VehicleService", "Fetching user's vehicles error ${e.message}")
        }
        return vehiclesRepairs
    }
}