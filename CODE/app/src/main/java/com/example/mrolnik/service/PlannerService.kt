package com.example.mrolnik.service

import Task
import android.annotation.SuppressLint
import android.util.Log
import com.example.mrolnik.config.SupabaseClient
import com.example.mrolnik.model.Planner

import io.github.jan.supabase.postgrest.from

class PlannerService {
    val supabase = SupabaseClient().getSupabaseClient()


    @SuppressLint("NewApi")
    suspend fun createOrReturnPlanner() : Planner? {
        val userId = UserService.getLoggedUserId()
        var userService = UserService()
        val plannerId = UserService.getLoggedUserPlannerId()
        val planner = Planner(userId)
        //val objectMapper = ObjectMapper().registerModule(JavaTimeModule())
        if (plannerId != userId) {
            try {
                supabase.from("planner")
                    .insert(planner)
                userService.updateUserPlannerId(planner)
            } catch (e: Exception) {
                Log.e("PlannerService", "Creating planner error: ${e.message}")
            }
        }
        else {
            return getPlannerByUserId() // zwrocenie istniejacego plannera
        }
        return planner
    }

    suspend fun getPlannerByUserId(): Planner? {
        val plannerId = UserService.getLoggedUserPlannerId()
        try {
            val result = supabase.from("planner").select {
                filter {
                    eq("plannerId", plannerId)
                }
            }
                .decodeSingle<Planner>()
            if (!result.equals(null)) {
                val planner = result
                return planner
            }
        } catch (e: Exception) {
            Log.e("PlannerService", "Fetching planner error: ${e.message}")
        }
        return null
    }

    suspend fun getTasksForDate(plannerId: String, date: String): List<Task> {
        return supabase.from("task").select{
            filter {
                eq("plannerId", plannerId)
                eq("realizeDate", date)
            }
        }.decodeList<Task>()
    }

}