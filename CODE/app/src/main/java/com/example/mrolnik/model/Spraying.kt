package com.example.mrolnik.model

class Spraying() {
    var sprayingId: Int = 0
    var sprayingDate: String = ""
    var sprayingName: String = ""
    var sprayingQuantity: Double = 0.0
    var cultivationId: Int = 0
    var fruitTreeId: Int = 0

    constructor(sprayingName: String, sprayingDate: String, sprayingQuantity: Double) : this() {
        this.sprayingName = sprayingName
        this.sprayingDate = sprayingDate
        this.sprayingQuantity = sprayingQuantity
    }
}