package com.example.mrolnik.service

import android.util.Log
import com.example.mrolnik.config.SupabaseClient
import com.example.mrolnik.model.Cultivation
import com.example.mrolnik.model.Field
import io.github.jan.supabase.postgrest.from

class CultivationService {
    val supabase = SupabaseClient().getSupabaseClient()

    suspend fun deleteCultivation(cultivation: Cultivation): Boolean {
        try {
            supabase.from("cultivation").delete {
                filter {
                    eq("cultivationId", cultivation.cultivationId)
                }
            }
            return true
        } catch (e: Exception) {
            Log.e("CultivationService", "Deleting cultivation from database error: ${e.message}")
            return false
        }
    }

    suspend fun updateCultivation(cultivation: Cultivation): Boolean {
        try {
            supabase.from("cultivation").update(
                {
                    set("plantName", cultivation.plantName)
                    set("sowingDate", cultivation.sowingDate)
                    set("plannedHarvestDate", cultivation.plannedHarvestDate)
                    set("usedFertilizerQuantity", cultivation.usedFertilizerQuantity)
                    set("usedSprayingQuantity", cultivation.usedSprayingQuantity)
                }
            ) {
                filter {
                    eq("cultivationId", cultivation.cultivationId)
                }
            }
            return true
        } catch (e: Exception) {
            Log.e("CultivationService", "Updating cultivation error: ${e.message}")
            return false
        }
    }

    suspend fun getAllCultivationsByFieldId(field: Field?): List<Cultivation> {
        var fieldsCultivations: List<Cultivation> = emptyList()
        try {
            fieldsCultivations = supabase.from("cultivation")
                .select {
                    filter {
                        eq("fieldId", field!!.fieldId)
                    }
                }
                .decodeList<Cultivation>()
        } catch (e: Exception) {
            Log.e("CultivationService", "Fetching user's cultivations error ${e.message}")
        }
        return fieldsCultivations
    }

    suspend fun assignCultivationToField(cultivation: Cultivation, field: Field?) {
        try {
            supabase.from("cultivation").insert(
                mapOf(
                    "plantName" to cultivation.plantName,
                    "sowingDate" to cultivation.sowingDate,
                    "plannedHarvestDate" to cultivation.plannedHarvestDate,
                    "usedFertilizerQuantity" to cultivation.usedFertilizerQuantity,
                    "usedSprayingQuantity" to cultivation.usedSprayingQuantity,
                    "fieldId" to field?.fieldId
                )
            )
        } catch (e: Exception) {
            Log.e("CultivationService", "Assigning cultivation to field error: ${e.message}")
        }
    }
}