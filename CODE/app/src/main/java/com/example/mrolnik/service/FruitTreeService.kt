package com.example.mrolnik.service

import android.util.Log
import com.example.mrolnik.config.SupabaseClient
import com.example.mrolnik.model.Cultivation
import com.example.mrolnik.model.Field
import com.example.mrolnik.model.FruitTree
import com.example.mrolnik.model.Orchard
import io.github.jan.supabase.postgrest.from

class FruitTreeService {
    val supabase = SupabaseClient().getSupabaseClient()

    suspend fun deleteFruitTree(fruitTree: FruitTree): Boolean {
        try {
            supabase.from("fruitTree").delete {
                filter {
                    eq("fruitTreeId", fruitTree.fruitTreeId)
                }
            }
            return true
        } catch (e: Exception) {
            Log.e("FruitTreeService", "Deleting fruit tree from database error: ${e.message}")
            return false
        }
    }

    suspend fun updateFruitTree(fruitTree: FruitTree): Boolean {
        try {
            supabase.from("fruitTree").update(
                {
                    set("plantName", fruitTree.plantName)
                    set("plannedHarvestDate", fruitTree.plannedHarvestDate)
                    set("usedSprayingQuantity", fruitTree.usedSprayingQuantity)
                }
            ) {
                filter {
                    eq("fruitTreeId", fruitTree.fruitTreeId)
                }
            }
            return true
        } catch (e: Exception) {
            Log.e("FruitTreeService", "Updating fruitTree error: ${e.message}")
            return false
        }
    }

    suspend fun getAllFruitTreesByOrchardId(orchard: Orchard?): List<FruitTree> {
        var orchardsFruitTrees: List<FruitTree> = emptyList()
        try {
            orchardsFruitTrees = supabase.from("fruitTree")
                .select {
                    filter {
                        eq("orchardId", orchard!!.orchardId)
                    }
                }
                .decodeList<FruitTree>()
        } catch (e: Exception) {
            Log.e("FruitTreeService", "Fetching orchard's fruitTree error ${e.message}")
        }
        return orchardsFruitTrees
    }

    suspend fun assignFruitTreeToOrchard(fruitTree: FruitTree, orchard: Orchard?) {
        try {
            supabase.from("fruitTree").insert(
                mapOf(
                    "plantName" to fruitTree.plantName,
                    "plannedHarvestDate" to fruitTree.plannedHarvestDate,
                    "usedSprayingQuantity" to fruitTree.usedSprayingQuantity,
                    "orchardId" to orchard?.orchardId
                )
            )
        } catch (e: Exception) {
            Log.e("FruitTreeService", "Assigning fruitTree to orchard error: ${e.message}")
        }
    }
}