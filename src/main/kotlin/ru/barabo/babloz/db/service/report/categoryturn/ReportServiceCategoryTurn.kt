package ru.barabo.babloz.db.service.report.categoryturn

import ru.barabo.babloz.db.entity.Category
import ru.barabo.babloz.db.processLongTransactionsKill
import ru.barabo.babloz.db.selectValueType
import ru.barabo.babloz.db.service.report.DateRange
import ru.barabo.babloz.db.service.report.PeriodType
import ru.barabo.db.SessionSetting
import ru.barabo.db.localDateFormatToSql
import java.time.LocalDate

//val logger = LoggerFactory.getLogger(ReportServiceCategoryTurn::class.java)!!

object ReportServiceCategoryTurn : ReportCategoryTurn {

    override val entitySet: MutableSet<Category> = LinkedHashSet()

    override var periodType = PeriodType.MONTH
    set(value) {
        field = value
        setPeriod(value)
    }

    override var dateRange: DateRange = DateRange.minMaxDateList(periodType)

    private val mapCategoryTurn = HashMap<Category, IntArray>()

    private var categoryView: CategoryView = CategoryView.ALL

    override val listeners: MutableList<()->Unit> = ArrayList()

    override fun infoMap(): Map<Category, IntArray> = mapCategoryTurn

    override fun updateEntityInfo(entity: Category) = updateTurn()

    override fun updateDateRangeInfo() = updateTurn()

    override fun setCategoryView(categoryView: CategoryView) {
        this.categoryView = categoryView

        updateTurn()
    }

    private fun categoryListByView(): List<Category> {

        return when (categoryView) {
            CategoryView.PARENT_ONLY -> parentOnlyCategoryList()
            CategoryView.CHILD_ONLY -> childOnlyCategoryList()
            else -> entitySet.toList()
        }
     }

    private fun parentOnlyCategoryList(): List<Category> {

        val parentList = ArrayList<Category>()

        for(category in entitySet) {

            val isAddCategory = (category.parent == null) ||
                    (entitySet.firstOrNull { it.id == category.parent } == null)

            if(isAddCategory) {
                parentList += category
            }
        }
        return parentList
    }

    private fun childOnlyCategoryList(): List<Category> = entitySet.filter { it.parent != null }

    private fun updateTurn() {

        mapCategoryTurn.clear()

        val datePeriods = dateRangeByList()

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

private fun Category.getTurnsByDates(months: List<LocalDate>, periodType: PeriodType): IntArray =
        processByDates(months, periodType) {session, start, end ->
            selectValueType<Number>(SELECT_TURN_MONTH, arrayOf(start, end, id), session)
        }

fun LocalDate.toSqlDate() = localDateFormatToSql(this) //java.sql.Date(atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())

internal fun processByDates(dates: List<LocalDate>, periodType: PeriodType,
                            process: (session: SessionSetting, start: String, end: String)->Number?): IntArray {
    val values = IntArray(dates.size)

    processLongTransactionsKill { session->
        for ((index, month) in dates.withIndex()) {

            val turn = process(session, month.toSqlDate(), periodType.nextDate(month).toSqlDate())

            values[index] = kotlin.math.abs(turn?.toInt() ?: 0)
        }
    }
    return values
}





