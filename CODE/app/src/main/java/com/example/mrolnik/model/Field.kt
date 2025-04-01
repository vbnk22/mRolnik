package com.example.mrolnik.model

class Field {
    var fieldId: Int = 0
    var fieldName: String = ""
    var cultivationId: Int = 0

    constructor(fieldName: String) {
        this.fieldName = fieldName
    }
}