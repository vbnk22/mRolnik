package com.example.mrolnik.model

class Cultivation {
    var cultivationId: Int = 0
    var plantName: String = ""
    var sowingDate: String = ""
    var plannedHarvestDate: String = ""
    var fertilizerId: Int = 0
    var usedFertilizerlizerQuantity: Double = 0.0
    var usedSprayingQuantity: Double = 0.0
    var sprayingId: Int = 0

    constructor(plantName: String, plannedHarvestDate: String, usedSprayingQuantity: Double, usedFerlizerQuantity: Double) {
        this.plantName = plantName
        this.plannedHarvestDate = plannedHarvestDate
        this.usedSprayingQuantity = usedSprayingQuantity
        this.usedFertilizerlizerQuantity = usedFerlizerQuantity
    }
}