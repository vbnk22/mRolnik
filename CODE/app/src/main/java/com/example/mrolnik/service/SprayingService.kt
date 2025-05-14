package com.example.mrolnik.service

import android.util.Log
import com.example.mrolnik.config.SupabaseClient
import com.example.mrolnik.model.Cultivation
import com.example.mrolnik.model.Fertilizer
import com.example.mrolnik.model.FruitTree
import com.example.mrolnik.model.Spraying
import io.github.jan.supabase.postgrest.from

class SprayingService {
    val supabase = SupabaseClient().getSupabaseClient()

    suspend fun deleteSpraying(spraying: Spraying): Boolean {
        try {
            supabase.from("spraying").delete {
                filter {
                    eq("sprayingId", spraying.sprayingId)
                }
            }
            return true
        } catch (e: Exception) {
            Log.e("SprayingService", "Deleting spraying from database error: ${e.message}")
            return false
        }
    }

    suspend fun updateSpraying(spraying: Spraying): Boolean {
        try {
            supabase.from("spraying").update(
                {
                    set("sprayingName", spraying.sprayingName)
                    set("sprayingDate", spraying.sprayingDate)
                    set("sprayingQuantity", spraying.sprayingQuantity)
                }
            ) {
                filter {
                    eq("sprayingId", spraying.sprayingId)
                }
            }
            return true
        } catch (e: Exception) {
            Log.e("SprayingService", "Updating spraying error: ${e.message}")
            return false
        }
    }

    suspend fun getAllSprayingByCultivationId(cultivation: Cultivation?): List<Spraying> {
        var cultivationsSpraying: List<Spraying> = emptyList()
        try {
            cultivationsSpraying = supabase.from("spraying")
                .select {
                    filter {
                        eq("cultivationId", cultivation!!.cultivationId)
                    }
                }
                .decodeList<Spraying>()
        } catch (e: Exception) {
            Log.e("SprayingService", "Fetching cultivation's spraying error ${e.message}")
        }
        return cultivationsSpraying
    }

    suspend fun getAllSprayingByFruitTreeId(fruitTree: FruitTree?): List<Spraying> {
        var cultivationsSpraying: List<Spraying> = emptyList()
        try {
            cultivationsSpraying = supabase.from("spraying")
                .select {
                    filter {
                        eq("fruitTreeId", fruitTree!!.fruitTreeId)
                    }
                }
                .decodeList<Spraying>()
        } catch (e: Exception) {
            Log.e("SprayingService", "Fetching cultivation's spraying error ${e.message}")
        }
        return cultivationsSpraying
    }

    suspend fun assignSprayingToCultivation(spraying: Spraying, cultivation: Cultivation?) {
        try {
            supabase.from("spraying").insert(
                mapOf(
                    "sprayingName" to spraying.sprayingName,
                    "sprayingDate" to spraying.sprayingDate,
                    "sprayingQuantity" to spraying.sprayingQuantity,
                    "cultivationId" to cultivation?.cultivationId
                )
            )
        } catch (e: Exception) {
            Log.e("SprayingService", "Assigning spraying to cultivation error: ${e.message}")
        }
    }

    suspend fun assignSprayingToFruitTree(spraying: Spraying, fruitTree: FruitTree?) {
        try {
            supabase.from("spraying").insert(
                mapOf(
                    "sprayingName" to spraying.sprayingName,
                    "sprayingDate" to spraying.sprayingDate,
                    "sprayingQuantity" to spraying.sprayingQuantity,
                    "fruitTreeId" to fruitTree?.fruitTreeId
                )
            )
        } catch (e: Exception) {
            Log.e("SprayingService", "Assigning spraying to fruitTree error: ${e.message}")
        }
    }
}