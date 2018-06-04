package ru.barabo.babloz.db.entity.budget

import org.slf4j.LoggerFactory
import ru.barabo.babloz.db.service.budget.BudgetTreeCategoryService
import ru.barabo.db.annotation.*
import java.math.BigDecimal

@TableName("BUDGET_ROW")
@SelectQuery("""select r.*,
       case when r.name = 'Все остальные категории' then

       (select coalesce(sum(-1*p.amount), 0) from PAY p, CATEGORY c, BUDGET_MAIN m
where m.id = r.MAIN and p.category = c.id and c.TYPE = 0 and p.created >= m.START_PERIOD and p.created < m.END_PERIOD
  and c.id not in (select bc.category from BUDGET_CATEGORY bc, BUDGET_ROW br where bc.BUDGET_ROW = br.ID and br.MAIN = m.id) )

       else  (select coalesce(sum(-1*p.amount), 0) from pay p, category c, BUDGET_MAIN m, BUDGET_CATEGORY bc
            where m.id = r.MAIN and p.category = c.id and c.TYPE = 0 and p.created >= m.START_PERIOD and p.created < m.END_PERIOD
            and bc.BUDGET_ROW = r.id and
           (c.id = bc.category or (bc.INCLUDE_SUB_CATEGORY != 0 and c.id in (select cc.id from category cc where cc.parent = c.id) ) ) )
       end     AMOUNT_REAL
       from BUDGET_ROW r where r.MAIN = ? order by id""")
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
        @ReadOnly
        var amount: BigDecimal? = null,

        @ColumnName("AMOUNT_REAL")
        @ColumnType(java.sql.Types.NUMERIC)
        @ReadOnly
        var amountReal: BigDecimal? = null)
    : ParamsSelect {

    companion object {
        private val logger = LoggerFactory.getLogger(BudgetRow::class.java)

        var budgetRowSelected: BudgetRow? = null
        set(value) {

            val oldValue = field

            field = value

            if(oldValue !== value) {
                BudgetTreeCategoryService.initData()
            }
        }

        private const val OTHER_NAME = "Все остальные категории"

        private const val NEW_NAME = "Новая строка бюджета"

        fun createOthersRow(budgetMain: BudgetMain): BudgetRow =
            BudgetRow(main = budgetMain.id, name = OTHER_NAME, amount = BigDecimal(0))

        fun createNewEmptyRow(budgetMain: BudgetMain): BudgetRow =
                BudgetRow(main = budgetMain.id, name = NEW_NAME, amount = BigDecimal(0))
    }

    override fun selectParams(): Array<Any?>? =  arrayOf(BudgetMain.selectedBudget?.id?:Int::class)

    fun isOther(): Boolean = (name == OTHER_NAME)

    fun isNewName(): Boolean = (name == NEW_NAME)
}