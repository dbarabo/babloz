package ru.barabo.babloz.db.entity.budget

import ru.barabo.db.annotation.*


@TableName("BUDGET_CATEGORY")
@SelectQuery("select bc.* from BUDGET_CATEGORY bc, BUDGET_ROW br where COALESCE(bc.SYNC, 0) != 2 and bc.BUDGET_ROW = br.id and br.MAIN = ?")
data class BudgetCategory(
        @ColumnName("ID")
        @SequenceName("SELECT COALESCE(MAX(ID), 0) + 1  from BUDGET_CATEGORY")
        @ColumnType(java.sql.Types.INTEGER)
        var id :Int? = null,

        @ColumnName("BUDGET_ROW")
        @ColumnType(java.sql.Types.INTEGER)
        var budgetRow :Int? = null,

        @ColumnName("CATEGORY")
        @ColumnType(java.sql.Types.INTEGER)
        var category :Int? = null,

        @ColumnName("SYNC")
        @ColumnType(java.sql.Types.INTEGER)
        @Transient
        var sync :Int? = null
) : ParamsSelect {

    override fun selectParams(): Array<Any?>? = arrayOf(BudgetMain.selectedBudget?.id?:Int::class)

    override fun toString(): String = "ID=$id budgetRow=$budgetRow category=$category"
}