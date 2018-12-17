package ru.barabo.babloz.db.service.report.costincome

import ru.barabo.babloz.db.entity.Category
import ru.barabo.babloz.db.entity.CategoryType
import ru.barabo.babloz.db.selectValueType
import ru.barabo.babloz.db.service.report.DateRange
import ru.barabo.babloz.db.service.report.PeriodType
import ru.barabo.babloz.db.service.report.categoryturn.toSqlDate
import java.time.LocalDate
import kotlin.math.abs

object ReportServiceCostsIncomeTurn : ReportCostsIncomeTurn {

    override val categorySet = LinkedHashSet<Category>()

    override val periodType = PeriodType.MONTH

    override val datePeriods: MutableList<LocalDate> =
            DateRange.updateDateRange(end = LocalDate.now(), periodType = periodType).getPeriods(periodType)

    private val mapCategoryTypeTurn = HashMap<CategoryType, IntArray>()

    override val listeners: MutableList<(List<LocalDate>, Map<*, IntArray>) -> Unit> = ArrayList()

    override fun dateListenerList(): List<LocalDate> = datePeriods

    override fun infoMap(): Map<*, IntArray> = mapCategoryTypeTurn

    override fun updateCategoryInfo() = updateCostsIncomeTurn()

    override fun updateDateRangeInfo() = updateCostsIncomeTurn()

    private fun updateCostsIncomeTurn() {

        mapCategoryTypeTurn.clear()

        mapCategoryTypeTurn[CategoryType.COST] = categorySet.getTurnsByDates(datePeriods, CategoryType.COST, periodType)

        mapCategoryTypeTurn[CategoryType.INCOMES] = categorySet.getTurnsByDates(datePeriods, CategoryType.INCOMES, periodType)

        updateInfoListeners()
    }
}

private const val SELECT_TURN_MONTH_COSTS =
"""select COALESCE(sum(p.AMOUNT), 0)
  from PAY p,
       category chi
where p.CATEGORY = chi.ID
  and COALESCE(p.SYNC, 0) != 2
  and p.CREATED >= ? and p.CREATED < ?
  and chi.TYPE = ?"""

private fun LinkedHashSet<Category>.plusCategories(): String = if(isEmpty()) SELECT_TURN_MONTH_COSTS else
    SELECT_TURN_MONTH_COSTS + addCategories()

private fun LinkedHashSet<Category>.addCategories(): String = " and p.CATEGORY in ( ${categoriesList()} )"

private fun LinkedHashSet<Category>.categoriesList() = joinToString(", ") { it.id.toString() }


private fun LinkedHashSet<Category>.getTurnsByDates(months: List<LocalDate>, categoryType: CategoryType, periodType: PeriodType): IntArray {

    val values = IntArray(months.size)
    for ((index, month) in months.withIndex()) {

        val turn = selectValueType<Number>(
                plusCategories(), arrayOf(month.toSqlDate(), periodType.nextDate(month).toSqlDate(), categoryType.ordinal))

        values[index] = abs(turn?.toInt() ?: 0)
    }
    return values
}