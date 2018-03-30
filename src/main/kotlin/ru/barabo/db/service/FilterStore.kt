package ru.barabo.db.service

import ru.barabo.babloz.gui.formatter.parseToMoney
import kotlin.reflect.KCallable
import kotlin.reflect.KClass

interface FilterStore<E: Any> {

    var isFiltered: Boolean

    val allData: MutableList<E>?

    var filterCriteria: MutableList<FilterCriteria>

    val indexFieldToFilter: Map<KCallable<*>, KClass<*>>

    fun getDataListStore(): MutableList<E>

    fun clearCriteria() {
        resetFilter()

        setDataFromAll()
    }

    private fun resetFilter() {
        if(!isFiltered) return

        isFiltered = false

        filterCriteria.clear()
    }

    private fun setDataFromAll() {
        getDataListStore().clear()

        getDataListStore().addAll(allData!!)
    }

    fun setCriteria(criteria: String) {

        fillCriteriaData(criteria)

        setFilter()
    }

    private fun fillCriteriaData(criteria: String) {
        filterCriteria.clear()

        val words = criteria.split("[\\p{Punct}\\s]+")
                .filter { it.trim().length > 2 || it.toNumber() != null }
                .map { it.trim().toUpperCase() }

        val numbers = words.filter { it.toNumber() != null }.map{it.toNumber()!!}

        indexFieldToFilter.entries.forEach {

            //LoggerFactory.getLogger(FilterStore::class.java).info("it.value=${it.value}")

            when(it.value) {
                String::class -> if(words.isNotEmpty()) filterCriteria.add(FilterCriteria(it.key, words))

                Number::class -> if(numbers.isNotEmpty())  filterCriteria.add(FilterCriteria(it.key, numbers))
            }

        }
    }

    private fun setFilter() {

        if(filterCriteria.isNotEmpty()) {
            return setFilterByCriteria()
        }

        if(isFiltered) {
            clearCriteria()
        }
    }

    private fun setFilterByCriteria() {

        isFiltered = true

        getDataListStore().clear()

        allData?.forEach {

            if(filterCriteria.isAccess(it) ) {
                getDataListStore().add(it)
            }
        }
    }

    private fun MutableList<FilterCriteria>.isAccess(row: E): Boolean {

        val accessTrue = this.firstOrNull { it.isAccess(row) }

        return accessTrue != null
    }
}


private fun String.toNumber(): Number? = this.trim().parseToMoney()
