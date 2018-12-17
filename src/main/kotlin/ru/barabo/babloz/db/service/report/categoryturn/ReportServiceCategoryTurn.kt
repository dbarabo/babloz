package ru.barabo.babloz.db.service.report.categoryturn

import org.slf4j.LoggerFactory
import ru.barabo.babloz.db.entity.Category
import ru.barabo.babloz.db.selectValueType
import ru.barabo.babloz.db.service.report.DateRange
import ru.barabo.babloz.db.service.report.PeriodType
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs

val logger = LoggerFactory.getLogger(ReportServiceCategoryTurn::class.java)!!

object ReportServiceCategoryTurn : ReportCategoryTurn {

    override val categorySet = LinkedHashSet<Category>()

    override val periodType = PeriodType.MONTH

    override val datePeriods: MutableList<LocalDate> =
            DateRange.updateDateRange(end = LocalDate.now(), periodType = periodType).getPeriods(periodType)

    private val mapCategoryTurn = HashMap<Category, IntArray>()

    private var categoryView: CategoryView = CategoryView.ALL

    override val listeners: MutableList<(List<LocalDate>, Map<*, IntArray>) -> Unit> = ArrayList()

    override fun dateListenerList(): List<LocalDate> = datePeriods

    override fun infoMap(): Map<*, IntArray> = mapCategoryTurn

    override fun updateCategoryInfo() = updateTurn()

    override fun updateDateRangeInfo() = updateTurn()

    override fun setCategoryView(categoryView: CategoryView) {
        ReportServiceCategoryTurn.categoryView = categoryView

        updateTurn()
    }

    private fun categoryListByView(): List<Category> {

        return when (categoryView) {
            CategoryView.PARENT_ONLY -> parentOnlyCategoryList()
            CategoryView.CHILD_ONLY -> childOnlyCategoryList()
            else -> categorySet.toList()
        }
     }

    private fun parentOnlyCategoryList(): List<Category> {

        val parentList = ArrayList<Category>()

        for(category in categorySet) {

            val isAddCategory = (category.parent == null) ||
                    (categorySet.firstOrNull { it.id == category.parent } == null)

            if(isAddCategory) {
                parentList += category
            }
        }
        return parentList
    }

    private fun childOnlyCategoryList(): List<Category> = categorySet.filter { it.parent != null }

    private fun updateTurn() {

        mapCategoryTurn.clear()

        categoryListByView().forEach {
            mapCategoryTurn[it] = it.getTurnsByDates(datePeriods, periodType)
        }

        updateInfoListeners()
    }
}

private const val SELECT_TURN_MONTH =
        """select (select COALESCE(sum(p.AMOUNT), 0) from PAY p, category chi where c.ID in (chi.id, chi.parent)
                and p.CATEGORY = chi.ID and p.CREATED >= ? and p.CREATED < ?) TURN
from category c where COALESCE(c.SYNC, 0) != 2 and c.ID = ?"""

private fun Category.getTurnsByDates(months: List<LocalDate>, periodType: PeriodType): IntArray {

    val values = IntArray(months.size)
    for ((index, month) in months.withIndex()) {

        val turn = selectValueType<Number>(
                SELECT_TURN_MONTH, arrayOf(month.toSqlDate(), periodType.nextDate(month).toSqlDate(), id))

        values[index] = abs(turn?.toInt() ?: 0)
    }
    return values
}

fun LocalDate.toSqlDate() = java.sql.Date(atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
