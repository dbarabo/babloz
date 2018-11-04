package ru.barabo.babloz.db.service

import ru.barabo.babloz.db.BablozOrm
import ru.barabo.babloz.db.entity.Currency
import ru.barabo.db.service.StoreService

object CurrencyService : StoreService<Currency, List<Currency> >(BablozOrm, Currency::class.java) {

    override fun elemRoot(): List<Currency> = dataList

    fun currencyList() = dataList
}