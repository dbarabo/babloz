package ru.barabo.babloz.db.entity.budget

import ru.barabo.babloz.db.BudgetTypePeriod
import ru.barabo.babloz.db.service.budget.BudgetCategoryService
import ru.barabo.babloz.db.service.budget.BudgetMainService
import ru.barabo.babloz.db.service.budget.BudgetRowService
import ru.barabo.db.annotation.*
import ru.barabo.db.converter.SqliteLocalDate
import java.math.BigDecimal
import java.time.LocalDate

@TableName("BUDGET_MAIN")
@SelectQuery("select m.*, " +
        "(select coalesce(sum(r.amount), 0) from BUDGET_ROW r where r.MAIN = m.id) AMOUNT_BUDGET, " +
        "(select coalesce(sum(-1*p.amount), 0) from pay p, category c where p.category = c.id and c.TYPE = 0 and p.created >= m.START_PERIOD and p.created < m.END_PERIOD) AMOUNT_REAL " +
        "from BUDGET_MAIN m where m.TYPE_PERIOD = ? order by id")
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
        var typePeriod :Int = BudgetTypePeriod.MONTH.dbValue,

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
        @ReadOnly
        var amountBudget : BigDecimal? = null,

        @ColumnName("AMOUNT_REAL")
        @ColumnType(java.sql.Types.NUMERIC)
        @ReadOnly
        var amountReal : BigDecimal? = null      ) : ParamsSelect {

    companion object {
        var budgetTypePeriod: BudgetTypePeriod = BudgetTypePeriod.MONTH
                set(value) {
                    field = value

                    BudgetMainService.initData()
                }


        var selectedBudget: BudgetMain? = null
            set(value) {
                field = value

                BudgetRowService.initData()

                BudgetCategoryService.initData()
            }
    }
    override fun selectParams(): Array<Any?>? = arrayOf(budgetTypePeriod.dbValue)
}