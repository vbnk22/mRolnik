package com.example.mrolnik.model

import android.annotation.SuppressLint
import java.time.LocalDate

class Planner {
    var plannerId: Int = 0
    @SuppressLint("NewApi")
    var createDate: String = LocalDate.now().toString()

    constructor(plannerId: Int) {
        this.plannerId = plannerId
    }
}