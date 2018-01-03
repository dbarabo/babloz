package ru.barabo.babloz.db.entity

enum class AccountType(val label :String) {

    CURRENT("Текущие (оборотные) счета"),
    CREDIT("Расходные счета (кредиты)"),
    DEPOSIT("Доходные счета (вклады)")
}