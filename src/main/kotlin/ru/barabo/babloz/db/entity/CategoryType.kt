package ru.barabo.babloz.db.entity

enum class CategoryType(val label :String) {
    COST ("Расходы"),
    INCOMES("Доходы"),
    TRANSFER("Перевод")
}