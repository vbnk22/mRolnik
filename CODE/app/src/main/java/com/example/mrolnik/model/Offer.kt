package com.example.mrolnik.model

class Offer() {
    var offerId:Int = 0
    var userId:Int = 0
    var description:String = ""

    constructor(userId: Int,description:String) : this() {
        this.userId = userId
        this.description = description
    }
}