package com.example.mrolnik.service

import android.util.Log
import com.example.mrolnik.config.SupabaseClient
import com.example.mrolnik.model.Offer
import com.example.mrolnik.model.Vehicle
import io.github.jan.supabase.postgrest.from
import com.example.mrolnik.service.UserService
import io.github.jan.supabase.postgrest.query.Columns

val userService = UserService()
class OfferService {
    val supabase = SupabaseClient().getSupabaseClient()
    val userId: Int = 0
    var resultOfInsert = Offer(0, "")


    suspend fun addOffer(offer: Offer): Offer? {
        try {
            resultOfInsert = supabase.from("offer").insert(
                mapOf(
                    "userId" to UserService.getLoggedUserId(),
                    "description" to offer.description
                )
            ) {
                select()
            }.decodeSingle<Offer>()
            return resultOfInsert
        } catch (e: Exception) {
            Log.e("OfferService", "Adding Offer to database error: ${e.message}")
            return null
        }

    }
    suspend fun getAllOffersByUserId(): List<Offer> {
        var usersOffers: List<Offer> = emptyList()
        try {
            val userId = UserService.getLoggedUserId()

            usersOffers = supabase.from("offer")
                .select {
                    filter {
                        eq("userId", userId)
                    }
                }
                .decodeList<Offer>()
        } catch (e: Exception) {
            Log.e("OfferService", "Fetching user's offers error: ${e.message}")
        }
        return usersOffers
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

    suspend fun updateOffer(offer: Offer) {
        try {
            supabase.from("offer").update(
                {
                    set("description", offer.description)
                }
            ) {

                filter {
                    eq("offerId", offer.offerId)
                }
            }
        } catch (e: Exception) {
            Log.e("OferService", "Updating offer data error ${e.message}")
        }
    }

    suspend fun deleteOffer(offer: Offer) {
        try {
            supabase.from("offer").delete {
                filter {
                    eq("offerId", offer.offerId)
                }
            }
        } catch (e: Exception) {
            Log.e("OferService", "Deleting offer error ${e.message}")
        }
    }
    suspend fun getOfferUserName(offer: Offer): String? {
        return try {
            userService.getNameFromId(offer.userId)
        } catch (e: Exception) {
            Log.e("OfferService", "Error getting offer user's name: ${e.message}")
            null
        }
    }

}