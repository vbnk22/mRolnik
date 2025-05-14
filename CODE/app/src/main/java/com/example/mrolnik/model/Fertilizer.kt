package com.example.mrolnik.model

class Fertilizer() {
    var fertilizerId: Int = 0
    var fertilizingDate: String = ""
    var fertilizerName: String = ""
    var fertilizerQuantity: Double = 0.0
    var cultivationId: Int = 0

    constructor(fertilizerName: String, fertilizingDate: String, fertilizerQuantity: Double) : this() {
        this.fertilizerName = fertilizerName
        this.fertilizingDate = fertilizingDate
        this.fertilizerQuantity = fertilizerQuantity
    }
}