package com.example.mrolnik.model


class Resource {
    var resourceId: Int = 0
    var name: String = ""
    var quantity: Double = 0.0
    var unitMeasures: String = ""
    var warehouseId: Int = 0

    constructor(name: String, quantity: Double, unitMeasures: String) {
        this.name = name
        this.quantity = quantity
        this.unitMeasures = unitMeasures
    }
}

