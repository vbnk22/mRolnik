package com.example.mrolnik.model

import java.util.Date

data class Task(
    val taskId: Int?,
    val taskName: String?,
    val realizeDate: Date?,
    var description: String = ""
)
