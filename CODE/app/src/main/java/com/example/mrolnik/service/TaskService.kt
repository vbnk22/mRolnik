package com.example.mrolnik.service

import Task
import android.util.Log
import com.example.mrolnik.config.SupabaseClient
import io.github.jan.supabase.postgrest.from

class TaskService {
    private val supabase = SupabaseClient().getSupabaseClient()

    suspend fun addTask(task: Task): Boolean {
        try {
            val result = supabase.from("task").insert(
                mapOf(
                    "taskName" to task.taskName,
                    "description" to task.description,
                    "realizeDate" to task.realizeDate, // "yyyy-MM-dd"
                    "plannerId" to task.plannerId
                )
            ) {
                select()
            }.decodeSingle<Task>()

            return true
        } catch (e: Exception) {
            Log.e("TaskService", "Error inserting task: ${e.message}")
            return false
        }
    }

    suspend fun updateTask(task: Task) {
        try {
            supabase.from("task").update(
                {
                    set("taskName", task.taskName)
                    set("description", task.description)
                    set("realizeDate", task.realizeDate)

                }
            ) {
                filter {
                    eq("taskId", task.taskId!!)
                }
            }
        } catch (e: Exception) {
            Log.e("TaskService", "Updating task error ${e.message}")
        }
    }



    suspend fun deleteTask(task: Task) {
        try {
            supabase.from("task").delete {
                filter {
                    eq("taskId", task.taskId!!)
                }
            }
        } catch (e: Exception) {
            Log.e("TaskService", "Deleting task error ${e.message}")
        }
    }


    suspend fun getTasksByDate(plannerId: Int, date: String): List<Task> {
        return try {
            supabase.from("task").select {
                filter {
                    eq("plannerId", plannerId)
                    eq("realizeDate", date)
                }
            }.decodeList()
        } catch (e: Exception) {
            Log.e("TaskService", "Error fetching tasks: ${e.message}")
            emptyList()
        }
    }
}
