package com.example.mrolnik.model

class Combustion() {
    var combustionId: Int = 0
    var measurementDate: String = ""
    var amountOfFuel: Double = 0.0
    var mileage: Double = 0.0
    var vehicleId: Int = 0

    constructor(measurementDate: String, amountOfFuel: Double, mileage: Double) : this() {
        this.measurementDate = measurementDate
        this.amountOfFuel = amountOfFuel
        this.mileage = mileage
    }
}