package ru.barabo.babloz.db.service.report

import org.slf4j.LoggerFactory
import ru.barabo.babloz.db.entity.Category
import ru.barabo.babloz.db.selectValueType
import java.sql.Date
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs

val logger = LoggerFactory.getLogger(ReportServiceCategoryTurn::class.java)!!

object ReportServiceCategoryTurn : ReportCategoryTurn {

    private val categorySet = LinkedHashSet<Category>()

    private val listeners = ArrayList<ListenerCategoryTurn>()

    private const val SELECT_MIN_PAY_DATE = "select MIN(CREATED) from PAY"

    private val minDatePay =
            (selectValueType<Number>(SELECT_MIN_PAY_DATE)?.let { Date(it.toLong()).toLocalDate() } ?: LocalDate.now())

    private val periodType = PeriodType.MONTH

    private var startEndDateRange = updateDateRange(minDatePay, LocalDate.now())

    private val datePeriods: MutableList<LocalDate> = getPeriods()

    private val mapCategoryTurn = HashMap<Category, IntArray>()

    private var categoryView: CategoryView = CategoryView.ALL

    override fun addCategory(category: Category) {
        categorySet.add(category)

        updateTurn()
    }

    override fun removeCategory(category: Category) {
        categorySet.remove(category)

        updateTurn()
    }

    override fun setDateRange(start: LocalDate, end: LocalDate) {

        startEndDateRange = updateDateRange(start, end)

        logger.error("start=$start")
        logger.error("end=$end")
        logger.error("startEndDateRange.start=${startEndDateRange.startInclusive}")
        logger.error("startEndDateRange.end=${startEndDateRange.endExclusive}")

        datePeriods.clear()
        datePeriods.addAll(getPeriods())

        updateTurn()
    }

    private fun updateDateRange(start: LocalDate, end: LocalDate): DataRange {

        val startDate = if(start.toEpochDay() < minDatePay.toEpochDay() ) minDatePay else start

        val endDate = if(end.toEpochDay() == LocalDate.MIN.toEpochDay() ) LocalDate.now() else end

        return DataRange(periodType.convertDate(startDate), periodType.convertDate(endDate) )
    }

    override fun addListener(listener: ListenerCategoryTurn) {
        listeners += listener
    }

    override fun setCategoryView(categoryView: CategoryView) {
        this.categoryView = categoryView

        updateTurn()
    }

    private fun categoryListByView(): List<Category> {

        return when (categoryView) {
            CategoryView.PARENT_ONLY ->  parentOnlyCategoryList()
            CategoryView.CHILD_ONLY ->  childOnlyCategoryList()
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

    private fun changeInfoListeners() {

        listeners.forEach {
            it.changeInfo(datePeriods, mapCategoryTurn)
        }
    }

    private fun updateTurn() {

        mapCategoryTurn.clear()

        categoryListByView().forEach {
            mapCategoryTurn[it] = it.getTurnsByDates(datePeriods)
        }

        changeInfoListeners()
    }

    private fun getPeriods(): MutableList<LocalDate> {

        val list = ArrayList<LocalDate>()
        var date = startEndDateRange.startInclusive

        do {
            list += date
            date = date.plusMonths(1)
        } while (date < startEndDateRange.endExclusive)

        return list
    }
}

private const val SELECT_TURN_MONTH =
        """select (select COALESCE(sum(p.AMOUNT), 0) from PAY p, category chi where c.ID in (chi.id, chi.parent)
                and p.CATEGORY = chi.ID and p.CREATED >= ? and p.CREATED < ?) TURN
from category c where COALESCE(c.SYNC, 0) != 2 and c.ID = ?"""

private fun Category.getTurnsByDates(months: List<LocalDate>): IntArray {

    val values = IntArray(months.size)
    for ((index, month) in months.withIndex()) {

        val turn = selectValueType<Number>(
                SELECT_TURN_MONTH, arrayOf(month.toSqlDate(), month.plusMonths(1).toSqlDate(), id))

        values[index] = abs(turn?.toInt() ?: 0)
    }
    return values
}

private data class DataRange(val startInclusive: LocalDate = LocalDate.now().minusYears(100),
                             val endExclusive: LocalDate = LocalDate.now())

fun LocalDate.toSqlDate() = java.sql.Date(atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
