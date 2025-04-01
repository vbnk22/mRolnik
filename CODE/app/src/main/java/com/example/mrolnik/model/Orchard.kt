package com.example.mrolnik.model

class Orchard {
    var orchardId: Int = 0
    var orchardName: String = ""
    var fruitTreeId: Int = 0

    constructor(orchardName: String) {
        this.orchardName = orchardName
    }
}