package com.example.mrolnik.model


class Animal {
    var animalId: Int = 0
    var species: String = ""
    var numberOfAnimals: Int = 0

    constructor(species:String, numberOfAnimals: Int) {
        this.species = species
        this.numberOfAnimals = numberOfAnimals
    }
}