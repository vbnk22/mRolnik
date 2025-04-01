package com.example.mrolnik.model

class Vehicle {
    var vehicleId: Int = 0
    var vehicleName: String = ""
    var technicalCondition: String = ""

    constructor(vehicleName: String, technicalCondition: String) {
        this.vehicleName = vehicleName
        this.technicalCondition = technicalCondition
    }
}
