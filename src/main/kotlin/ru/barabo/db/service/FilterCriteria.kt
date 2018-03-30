package ru.barabo.db.service

import org.slf4j.LoggerFactory
import kotlin.reflect.KCallable

data class FilterCriteria(val getter: KCallable<*>, val andValues: List<Any>) {

    private val logger = LoggerFactory.getLogger(FilterCriteria::class.java)

    fun <E> isAccess(row: E): Boolean {

        val valueRow = getter.call(row) ?: return false

        val isNotAccess = andValues.map {
            when (it) {
                is String -> valueRow.toString().toUpperCase().indexOf(it) >= 0
                is Number -> (valueRow as Number).toInt() == it.toInt()
                else -> false
            }
        }.firstOrNull { !it }

        logger.info("valueRow= $valueRow isNotAccess=$isNotAccess")

        return isNotAccess ?: true
    }
}