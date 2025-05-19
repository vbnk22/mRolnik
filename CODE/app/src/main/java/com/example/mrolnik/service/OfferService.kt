package com.example.mrolnik.service

import android.util.Log
import com.example.mrolnik.config.SupabaseClient
import com.example.mrolnik.model.Offer
import io.github.jan.supabase.postgrest.from

class OfferService {
    val supabase = SupabaseClient().getSupabaseClient()
    val userId: Int = 0
    var resultOfInsert = Offer(0, "")
    suspend fun addOffer(offer: Offer): Boolean {
        try {
            resultOfInsert = supabase.from("offer").insert(
                mapOf(
                    "userId" to UserService.getLoggedUserId(),
                    "description" to offer.description
                )
            ) {
                select()
            }.decodeSingle<Offer>()
            return true
        } catch (e: Exception) {
            Log.e("OfferService", "Adding Offer to database error: ${e.message}")
            return false
        }

    }
    suspend fun getAllOffers(): List<Offer> {
        var usersOffers: List<Offer> = emptyList()
        try {
            usersOffers = supabase.from("offer").select().decodeList<Offer>()
        } catch (e: Exception) {
            Log.e("OfferService", "Getting Offers data error ${e.message}")
        }
        return usersOffers
    }
}