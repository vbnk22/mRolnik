package com.example.mrolnik.service

import android.util.Log
import com.example.mrolnik.config.SupabaseClient
import com.example.mrolnik.model.Combustion
import com.example.mrolnik.model.Cultivation
import com.example.mrolnik.model.Fertilizer
import com.example.mrolnik.model.Vehicle
import io.github.jan.supabase.postgrest.from

class CombustionService {
    val supabase = SupabaseClient().getSupabaseClient()

    suspend fun deleteCombustion(combustion: Combustion): Boolean {
        try {
            supabase.from("combustion").delete {
                filter {
                    eq("combustionId", combustion.combustionId)
                }
            }
            return true
        } catch (e: Exception) {
            Log.e("CombustionService", "Deleting combustion from database error: ${e.message}")
            return false
        }
    }

    suspend fun updateCombustion(combustion: Combustion): Boolean {
        try {
            supabase.from("combustion").update(
                {
                    set("measurementDate", combustion.measurementDate)
                    set("amountOfFuel", combustion.amountOfFuel)
                    set("mileage", combustion.mileage)
                }
            ) {
                filter {
                    eq("combustionId", combustion.combustionId)
                }
            }
            return true
        } catch (e: Exception) {
            Log.e("CombustionService", "Updating combustion error: ${e.message}")
            return false
        }
    }

    suspend fun getCombustionByVehicleId(vehicle: Vehicle?): Combustion? {
        var cultivationsCombustion: List<Combustion> = emptyList();
        try {
            cultivationsCombustion = supabase.from("combustion")
                .select {
                    filter {
                        eq("vehicleId", vehicle!!.vehicleId)
                    }
                }
                .decodeList<Combustion>()
        } catch (e: Exception) {
            Log.e("CombustionService", "Fetching vehicle's combustion error ${e.message}")
        }
        return cultivationsCombustion.firstOrNull()
    }

    suspend fun assignVehicleToCombustion(combustion: Combustion, vehicle: Vehicle?) {
        try {
            supabase.from("combustion").insert(
                mapOf(
                    "measurementDate" to combustion.measurementDate,
                    "amountOfFuel" to combustion.amountOfFuel,
                    "mileage" to combustion.mileage,
                    "vehicleId" to vehicle?.vehicleId
                )
            )
        } catch (e: Exception) {
            Log.e("CombustionService", "Assigning vehicle to combustion error: ${e.message}")
        }
    }
}