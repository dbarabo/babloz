package ru.barabo.babloz.db.service.report

import org.slf4j.LoggerFactory
import ru.barabo.babloz.db.BablozOrm
import ru.barabo.babloz.db.entity.Category
import ru.barabo.babloz.db.entity.Category.Companion.TRANSFER_CATEGORY
import ru.barabo.babloz.db.service.CategoryService.findByName
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs


object ReportService {

    private val logger = LoggerFactory.getLogger(ReportService::class.java)!!

    fun getCategoryTurnList(): List<Category> = listOf(
            findByName("Продукты") ?: TRANSFER_CATEGORY,
            findByName("Бензин") ?: TRANSFER_CATEGORY,
            findByName("Спорт-секции") ?: TRANSFER_CATEGORY
    )

    fun getPeriods(): List<LocalDate> {

        val list = ArrayList<LocalDate>()
        var date = LocalDate.of(2013, 2, 1)

        do {
            list += date
            date = date.plusMonths(1)
        } while (date < LocalDate.of(2018, 12, 1))

        return list
    }

    fun getTurnsByCategory(category: Category, months: List<LocalDate>): IntArray {

        val values = IntArray(months.size)
        for ((index, month) in months.withIndex()) {

            val turn = BablozOrm.selectValue(
                    SELECT_TURN_MONTH, arrayOf(month.toSqlDate(), month.plusMonths(1).toSqlDate(), category.id)) as? Number

            logger.error("turn=$turn")

            logger.error("month=$month")

            logger.error("category=${category.id}")

            values[index] = abs(turn?.toInt() ?: 0)
        }



        return values
    }

    private const val SELECT_TURN_MONTH =
            """select (select COALESCE(sum(p.AMOUNT), 0) from PAY p, category chi where c.ID in (chi.id, chi.parent)
                and p.CATEGORY = chi.ID and p.CREATED >= ? and p.CREATED < ?) TURN
from category c where COALESCE(c.SYNC, 0) != 2 and c.ID = ?"""

}

fun LocalDate.toSqlDate() = java.sql.Date(atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
