package ru.barabo.babloz.db.service

import ru.barabo.babloz.db.BablozOrm
import ru.barabo.babloz.db.entity.Account
import ru.barabo.babloz.db.entity.Pay
import ru.barabo.babloz.db.service.filter.FilterPay
import ru.barabo.db.service.FilterCriteria
import ru.barabo.db.service.StoreService
import kotlin.reflect.KCallable
import kotlin.reflect.KClass

object PayService : StoreService<Pay, List<Pay>>(BablozOrm), FilterPay {

    override val accountsFilter: MutableList<Int> = ArrayList()

    override fun elemRoot(): List<Pay> = dataList

    override fun clazz(): Class<Pay> = Pay::class.java

    override var allData: MutableList<Pay>? = null

    override var filterCriteria: MutableList<FilterCriteria> = ArrayList()

    override fun getDataListStore(): MutableList<Pay> = dataList

    override val indexFieldToFilter: Map<KCallable<*>, KClass<*>> = mapOf(
            Pay::category to String::class,
            Pay::description to String::class,
            Pay::amount to Number::class
    )

    @Synchronized
    override fun beforeRead() {

        if(allData == null) {
            allData = ArrayList()
        }

        allData?.clear()
    }

    override fun processInsert(item :Pay) {
        allData!!.add(item)
    }

    override fun setCriteria(criteria: String) {
        super.setCriteria(criteria)

        sentRefreshAllListener()
    }

    override fun setAccountFilter(accounts: List<Account>) {
        super.setAccountFilter(accounts)

        sentRefreshAllListener()
    }

    fun firstByCriteria(criteria: (Pay)->Boolean): Pay? = allData?.firstOrNull { criteria(it) }
}