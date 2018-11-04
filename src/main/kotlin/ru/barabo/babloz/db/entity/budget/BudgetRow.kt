package ru.barabo.babloz.db.entity.budget

import ru.barabo.babloz.db.entity.budget.BudgetRow.Companion.CALC_AMOUNT_REAL
import ru.barabo.babloz.db.service.budget.BudgetTreeCategoryService
import ru.barabo.babloz.gui.budget.BudgetRowSaver
import ru.barabo.db.annotation.*
import java.math.BigDecimal
import java.text.DecimalFormat

@TableName("BUDGET_ROW")
@SelectQuery("select r.*, $CALC_AMOUNT_REAL from BUDGET_ROW r where COALESCE(r.SYNC, 0) != 2 and r.MAIN = ? order by id")
data class BudgetRow(
        @ColumnName("ID")
        @SequenceName("SELECT COALESCE(MAX(ID), 0) + 1  from BUDGET_ROW")
        @ColumnType(java.sql.Types.INTEGER)
        var id: Int? = null,

        @ColumnName("MAIN")
        @ColumnType(java.sql.Types.INTEGER)
        var main: Int? = null,

        @ColumnName("NAME")
        @ColumnType(java.sql.Types.VARCHAR)
        var name :String? = null,

        @ColumnName("AMOUNT")
        @ColumnType(java.sql.Types.NUMERIC)
        var amount: BigDecimal? = null,

        @ColumnName("AMOUNT_REAL")
        @ColumnType(java.sql.Types.NUMERIC)
        @CalcColumnQuery("select $CALC_AMOUNT_REAL from BUDGET_ROW r where r.id = ?")
        @ReadOnly
        var amountReal: BigDecimal? = null,

        @ColumnName("SYNC")
        @ColumnType(java.sql.Types.INTEGER)
        @Transient
        var sync :Int? = null
)
    : ParamsSelect {

    val amountFormat: String get() = amount?.let { DecimalFormat("0").format(it) }?:""

    val amountRealFormat: String get() = amountReal?.let { DecimalFormat("0").format(it) }?:""

    val percentAll: Double?
    get() {
        val amountDouble = amount?.toDouble()?.let { if(it == 0.0) Double.MAX_VALUE else it} ?: Double.MAX_VALUE

        val amountRealDouble = amountReal?.toDouble() ?: 0.0

        return amountRealDouble / amountDouble
    }

    companion object {

        internal const val OTHER_NAME = "Все остальные категории"

        internal const val CALC_AMOUNT_REAL =

           """case when r.name = '$OTHER_NAME' then
               (select coalesce(sum(-1*p.amount), 0) from PAY p, CATEGORY c, BUDGET_MAIN m
                         where m.id = r.MAIN and p.category = c.id and c.TYPE = 0 and p.created >= m.START_PERIOD and p.created < m.END_PERIOD
                           and c.id not in (select bc.category from BUDGET_CATEGORY bc, BUDGET_ROW br where bc.BUDGET_ROW = br.ID and br.MAIN = m.id) )
           else  (select coalesce(sum(-1*p.amount), 0) from pay p, category c, BUDGET_MAIN m, BUDGET_CATEGORY bc
                where m.id = r.MAIN and p.category = c.id and c.TYPE = 0 and p.created >= m.START_PERIOD and p.created < m.END_PERIOD
                and bc.BUDGET_ROW = r.id and
               (c.id = bc.category or (bc.INCLUDE_SUB_CATEGORY != 0 and c.id in (select cc.id from category cc where cc.parent = c.id) ) ) )
           end     AMOUNT_REAL"""

        var budgetRowSelected: BudgetRow? = null
        set(value) {

            val oldValue = field

            field = value

            if(oldValue !== value) {
                BudgetTreeCategoryService.initData()

                BudgetRowSaver.changeSelectEditValue(field)
            }
        }

        private const val NEW_NAME = "Новая строка бюджета"

        fun createOthersRow(budgetMain: BudgetMain): BudgetRow =
            BudgetRow(main = budgetMain.id, name = OTHER_NAME, amount = BigDecimal(0))

        fun createNewEmptyRow(budgetMain: BudgetMain): BudgetRow =
                BudgetRow(main = budgetMain.id, name = NEW_NAME, amount = BigDecimal(0))
    }

    override fun selectParams(): Array<Any?>? =  arrayOf(BudgetMain.selectedBudget?.id?:Int::class)

    fun isOther(): Boolean = (name == OTHER_NAME)

    fun createCopy(newBudgetMain: BudgetMain)= BudgetRow(main = newBudgetMain.id, name = name, amount = amount)
 }