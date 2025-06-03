package com.example.mrolnik.model

import kotlinx.serialization.Serializable

class MarketplaceItem {
    var marketplaceItemId: Int = 0
    var userId: Int = 0
    var title: String = ""
    var description: String = ""
    var price: Double = 0.0
    var image: String? = null
    var publicationDate: String = ""
    var location: String = ""
    var isActive: Boolean = true

    // W konstruktorze nie ma seller ponieważ seller to nasz userId i z niego sciągniemy nazwę użytkownika
    constructor(userId: Int,title:String, description: String,
                price: Double, image: String?,
                publicationDate: String, location: String,
                isActive: Boolean){
        this.userId = userId
        this.title = title
        this.description = description
        this.price = price
        this.image = image
        this.publicationDate = publicationDate
        this.location = location
        this.isActive = true
    }
}