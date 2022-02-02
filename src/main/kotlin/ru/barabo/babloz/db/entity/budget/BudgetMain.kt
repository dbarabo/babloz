package ru.barabo.babloz.db.entity.budget

import ru.barabo.babloz.db.BudgetTypePeriod
import ru.barabo.babloz.db.service.budget.BudgetCategoryService
import ru.barabo.babloz.db.service.budget.BudgetMainService
import ru.barabo.babloz.db.service.budget.BudgetRowService
import ru.barabo.db.annotation.*
import ru.barabo.db.converter.SqliteLocalDate
import java.math.BigDecimal
import java.text.DecimalFormat
import java.time.LocalDate

private const val AMOUNT_BUDGET =
    "(select coalesce(sum(r.amount), 0) from BUDGET_ROW r where r.MAIN = m.id) AMOUNT_BUDGET"

private const val AMOUNT_REAL = """
    (select coalesce(sum(-1*p.amount), 0) from pay p, category c 
    where p.category = c.id and c.TYPE = 0 and p.created >= m.START_PERIOD and p.created < m.END_PERIOD) AMOUNT_REAL"""

private const val AMOUNT_PROFIT = """
   (select coalesce(sum(p.amount), 0) from pay p
    where p.category is not null 
      and p.account_to is null 
      and p.created >= m.START_PERIOD
      and p.amount > 0
      and p.created < m.END_PERIOD) AMOUNT_PROFIT """

@TableName("BUDGET_MAIN")
@SelectQuery(
        """select m.*,
$AMOUNT_BUDGET,
$AMOUNT_REAL,
$AMOUNT_PROFIT
from BUDGET_MAIN m where COALESCE(m.SYNC, 0) != 2 and m.TYPE_PERIOD = ? order by id""")
data class BudgetMain (
        @ColumnName("ID")
        @SequenceName("SELECT COALESCE(MAX(ID), 0) + 1  from BUDGET_MAIN")
        @ColumnType(java.sql.Types.INTEGER)
        var id :Int? = null,

        @ColumnName("NAME")
        @ColumnType(java.sql.Types.VARCHAR)
        var name :String? = null,

        @ColumnName("TYPE_PERIOD")
        @ColumnType(java.sql.Types.INTEGER)
        var typePeriod :Int? = BudgetTypePeriod.MONTH.dbValue,

        @ColumnName("START_PERIOD")
        @ColumnType(java.sql.Types.DATE)
        @Converter(SqliteLocalDate::class)
        var startPeriod : LocalDate = LocalDate.now(),

        @ColumnName("END_PERIOD")
        @ColumnType(java.sql.Types.DATE)
        @Converter(SqliteLocalDate::class)
        var endPeriod : LocalDate = LocalDate.now(),

        @ColumnName("AMOUNT_BUDGET")
        @ColumnType(java.sql.Types.NUMERIC)
        @CalcColumnQuery("select $AMOUNT_BUDGET from BUDGET_MAIN m where m.id = ?")
        @ReadOnly
        var amountBudget : BigDecimal? = null,

        @ColumnName("AMOUNT_REAL")
        @ColumnType(java.sql.Types.NUMERIC)
        @CalcColumnQuery("select $AMOUNT_REAL from BUDGET_MAIN m where m.id = ?")
        @ReadOnly
        var amountReal : BigDecimal? = null,

        @ColumnName("AMOUNT_PROFIT")
        @ColumnType(java.sql.Types.NUMERIC)
        @CalcColumnQuery("select $AMOUNT_PROFIT from BUDGET_MAIN m where m.id = ?")
        @ReadOnly
        var amountProfit : BigDecimal? = null,

        @ColumnName("SYNC")
        @ColumnType(java.sql.Types.INTEGER)
        @Transient
        var sync :Int? = null

) : ParamsSelect {

    companion object {

        var budgetTypePeriod: BudgetTypePeriod = BudgetTypePeriod.MONTH
                set(value) {
                    field = value

                    BudgetMainService.initData()
                }


        var selectedBudget: BudgetMain? = null
            set(value) {

                val oldValue = field

                field = value

                if(oldValue !== value) {
                    BudgetRowService.initData()

                    BudgetCategoryService.initData()
                }
            }
    }

    var amountBudgetFormat: String = ""
        get() = amountBudget.amountFormat()

    var amountRealFormat: String = ""
        get() = amountReal.amountFormat()

    var amountProfitFormat: String = ""
        get() = amountProfit.amountFormat()


    val percentAll: Double
        get() {
            val amountDouble = amountBudget?.toDouble()?.let { if(it == 0.0) Double.MAX_VALUE else it} ?: Double.MAX_VALUE

            val amountRealDouble = amountReal?.toDouble() ?: 0.0

            return amountRealDouble / amountDouble
        }

    override fun selectParams(): Array<Any?> = arrayOf(budgetTypePeriod.dbValue)
}

private fun BigDecimal?.amountFormat(): String = let { DecimalFormat("#,###").format(it) }?:""