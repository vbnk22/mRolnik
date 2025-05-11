package com.example.mrolnik.model

class FruitTree {
    var fruitTreeId: Int = 0
    var plantName: String = ""
    var plannedHarvestDate: String = ""
    var usedSprayingQuantity: Double = 0.0
    var sprayingId: Int = 0

    constructor(plantName: String, plannedHarvestDate: String, usedSprayingQuantity: Double) {
        this.plantName = plantName
        this.plannedHarvestDate = plannedHarvestDate
        this.usedSprayingQuantity = usedSprayingQuantity
    }
}