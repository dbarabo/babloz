package ru.barabo.babloz.db.service.report.costincome

import ru.barabo.babloz.db.entity.Category
import ru.barabo.babloz.db.entity.CategoryType
import ru.barabo.babloz.db.selectValueType
import ru.barabo.babloz.db.service.report.DateRange
import ru.barabo.babloz.db.service.report.PeriodType
import ru.barabo.babloz.db.service.report.categoryturn.processByDates
import java.sql.Date
import java.time.LocalDate

object ReportServiceCostsIncomeTurn : ReportCostsIncomeTurn {

    override val entitySet: MutableSet<Category> = LinkedHashSet()

    override val periodType = PeriodType.MONTH

    override val datePeriods: MutableList<LocalDate> = DateRange.minMaxDateList(periodType)

    private val mapCategoryTypeTurn = HashMap<CategoryType, IntArray>()

    override val listeners: MutableList<(List<LocalDate>, Map<CategoryType, IntArray>) -> Unit> = ArrayList()

    override fun dateListenerList(): List<LocalDate> = datePeriods

    override fun infoMap(): Map<CategoryType, IntArray> = mapCategoryTypeTurn

    override fun updateEntityInfo(entity: Category) = updateCostsIncomeTurn()

    override fun updateDateRangeInfo() = updateCostsIncomeTurn()

    private fun updateCostsIncomeTurn() {

        mapCategoryTypeTurn.clear()

        mapCategoryTypeTurn[CategoryType.COST] = entitySet.getTurnsByDates(datePeriods, CategoryType.COST, periodType)

        mapCategoryTypeTurn[CategoryType.INCOMES] = entitySet.getTurnsByDates(datePeriods, CategoryType.INCOMES, periodType)

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

private fun MutableSet<Category>.plusCategories(): String = if(isEmpty()) SELECT_TURN_MONTH_COSTS else
    SELECT_TURN_MONTH_COSTS + addCategories()

private fun MutableSet<Category>.addCategories(): String = " and p.CATEGORY in ( ${categoriesList()} )"

private fun MutableSet<Category>.categoriesList() = joinToString(", ") { it.id.toString() }

private fun MutableSet<Category>.getTurnsByDates(months: List<LocalDate>, categoryType: CategoryType, periodType: PeriodType): IntArray =
    processByDates(months, periodType) { start: Date, end: Date ->
            selectValueType<Number>(plusCategories(), arrayOf(start, end, categoryType.ordinal))
    }
