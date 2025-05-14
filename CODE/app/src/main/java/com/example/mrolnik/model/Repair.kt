package com.example.mrolnik.model

class Repair() {
    var repairId: Int = 0
    lateinit var repairDate: String
    var description: String = ""
    var cost: Double = 0.0
    var vehicleId: Int = 0

    constructor(repairDate: String, description: String, cost: Double) : this() {
        this.repairDate = repairDate
        this.description = description
        this.cost = cost
    }
    constructor(repairDate: String, description: String, cost: Double, vehicleId: Int) : this() {
        this.repairDate = repairDate
        this.description = description
        this.cost = cost
        this.vehicleId = vehicleId
    }
    constructor(repairId: Int, repairDate: String, description: String, cost: Double, vehicleId: Int) : this() {
        this.repairId = repairId
        this.repairDate = repairDate
        this.description = description
        this.cost = cost
        this.vehicleId = vehicleId
    }
}