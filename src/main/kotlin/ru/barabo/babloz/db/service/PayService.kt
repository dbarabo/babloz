package ru.barabo.babloz.db.service

import ru.barabo.babloz.db.BablozOrm
import ru.barabo.babloz.db.entity.Pay
import ru.barabo.babloz.db.service.filter.FilterPay
import ru.barabo.db.EditType
import ru.barabo.db.service.FilterCriteria
import ru.barabo.db.service.StoreService
import java.time.LocalDateTime
import kotlin.reflect.KCallable
import kotlin.reflect.KClass

object PayService : StoreService<Pay, List<Pay>>(BablozOrm, Pay::class.java), FilterPay {

    override var dateStart: LocalDateTime = LocalDateTime.MIN

    override var dateEnd: LocalDateTime = LocalDateTime.MIN

    override val accountsFilter: MutableList<Int> = ArrayList()

    override val categoryFilter: MutableList<Int> = ArrayList()

    override val projectFilter: MutableList<Int> = ArrayList()

    override fun elemRoot(): List<Pay> = dataList

    override var allData: MutableList<Pay>? = null

    override var filterCriteria: MutableList<FilterCriteria> = ArrayList()

    var sumTable: Double = 0.0
    private set

    override fun getDataListStore(): MutableList<Pay> = dataList

    override val indexFieldToFilter: Map<KCallable<*>, KClass<*>> = mapOf(
            Pay::category to String::class,
            Pay::description to String::class,
            Pay::amount to Number::class
    )

    override fun sentRefreshAllListener(refreshType: EditType) {
        sumTable = recalcSum()
        super.sentRefreshAllListener(refreshType)
    }

    override fun afterFilterAction() {
        sentRefreshAllListener(EditType.FILTER)
    }

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

    private fun recalcSum(): Double = dataList.sumByDouble { it.amount?.toDouble() ?: 0.0 }

    fun firstByCriteria(criteria: (Pay)->Boolean): Pay? = allData?.firstOrNull { criteria(it) }
}