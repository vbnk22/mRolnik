package com.example.mrolnik.model

class Cultivation() {
    var cultivationId: Int = 0
    var plantName: String = ""
    var sowingDate: String = ""
    var plannedHarvestDate: String = ""
    var usedFertilizerQuantity: Double = 0.0
    var usedSprayingQuantity: Double = 0.0
    var fieldId: Int = 0

    constructor(plantName: String, plannedHarvestDate: String, usedSprayingQuantity: Double, usedFerltilizerQuantity: Double): this() {
        this.plantName = plantName
        this.plannedHarvestDate = plannedHarvestDate
        this.usedSprayingQuantity = usedSprayingQuantity
        this.usedFertilizerQuantity = usedFerltilizerQuantity
    }

    constructor(plantName: String, sowingDate: String, plannedHarvestDate: String, usedSprayingQuantity: Double, usedFerltilizerQuantity: Double): this() {
        this.plantName = plantName
        this.sowingDate = sowingDate
        this.plannedHarvestDate = plannedHarvestDate
        this.usedSprayingQuantity = usedSprayingQuantity
        this.usedFertilizerQuantity = usedFerltilizerQuantity
    }
}