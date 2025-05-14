package com.example.mrolnik.service

import android.util.Log
import com.example.mrolnik.config.SupabaseClient
import com.example.mrolnik.model.Cultivation
import com.example.mrolnik.model.Fertilizer
import io.github.jan.supabase.postgrest.from

class FertilizerService {
    val supabase = SupabaseClient().getSupabaseClient()

    suspend fun deleteFertilizer(fertilizer: Fertilizer): Boolean {
        try {
            supabase.from("fertilizer").delete {
                filter {
                    eq("fertilizerId", fertilizer.fertilizerId)
                }
            }
            return true
        } catch (e: Exception) {
            Log.e("FertilizerService", "Deleting fertilizer from database error: ${e.message}")
            return false
        }
    }

    suspend fun updateFertilizer(fertilizer: Fertilizer): Boolean {
        try {
            supabase.from("fertilizer").update(
                {
                    set("fertilizerName", fertilizer.fertilizerName)
                    set("fertilizingDate", fertilizer.fertilizingDate)
                    set("fertilizerQuantity", fertilizer.fertilizerQuantity)
                }
            ) {
                filter {
                    eq("fertilizerId", fertilizer.fertilizerId)
                }
            }
            return true
        } catch (e: Exception) {
            Log.e("FertilizerService", "Updating fertilizer error: ${e.message}")
            return false
        }
    }

    suspend fun getAllFertilizersByCultivationId(cultivation: Cultivation?): List<Fertilizer> {
        var cultivationsFertilizers: List<Fertilizer> = emptyList()
        try {
            cultivationsFertilizers = supabase.from("fertilizer")
                .select {
                    filter {
                        eq("cultivationId", cultivation!!.cultivationId)
                    }
                }
                .decodeList<Fertilizer>()
        } catch (e: Exception) {
            Log.e("FertilizerService", "Fetching cultivation's fertilizer error ${e.message}")
        }
        return cultivationsFertilizers
    }

    suspend fun assignFertilizerToCultivation(fertilizer: Fertilizer, cultivation: Cultivation?) {
        try {
            supabase.from("fertilizer").insert(
                mapOf(
                    "fertilizerName" to fertilizer.fertilizerName,
                    "fertilizingDate" to fertilizer.fertilizingDate,
                    "fertilizerQuantity" to fertilizer.fertilizerQuantity,
                    "cultivationId" to cultivation?.cultivationId
                )
            )
        } catch (e: Exception) {
            Log.e("FertilizerService", "Assigning fertilizer to cultivation error: ${e.message}")
        }
    }
}