package ru.barabo.babloz.db.service

import ru.barabo.babloz.db.BablozOrm
import ru.barabo.babloz.db.entity.Currency
import ru.barabo.db.service.StoreService

object CurrencyService : StoreService<Currency, List<Currency> >(BablozOrm) {

    override fun elemRoot(): List<Currency> = dataList

    override fun clazz(): Class<Currency> = Currency::class.java

    fun currencyList() = dataList
}