package ru.barabo.babloz.db.importer

enum class PayField(val label :String) {
    ACCOUNT("ACCOUNT"),
    CREATED("DATE"),
    CATEGORY("CATEGORY"),
    AMOUNT("AMOUNT"),
    DESCRIPTION("NOTE"),
    PROJECT("PROJECT");

    companion object {

    }
}