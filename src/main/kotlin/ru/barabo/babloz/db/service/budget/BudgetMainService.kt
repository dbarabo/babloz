package ru.barabo.babloz.db.service.budget

import org.slf4j.LoggerFactory
import ru.barabo.babloz.db.BablozOrm
import ru.barabo.babloz.db.entity.budget.BudgetMain
import ru.barabo.babloz.db.entity.budget.BudgetRow
import ru.barabo.db.EditType
import ru.barabo.db.Query
import ru.barabo.db.service.StoreService
import java.time.LocalDate

object BudgetMainService: StoreService<BudgetMain, List<BudgetMain>>(BablozOrm, BudgetMain::class.java) {

    private val logger = LoggerFactory.getLogger(BudgetMainService::class.java)

    override fun elemRoot(): List<BudgetMain> = dataList

    fun reCalcSelectedRow() {
        val selected = BudgetMain.selectedBudget?:return

        selected.id?.let { reCalcItemById(it, selected) }

        sentRefreshAllListener(EditType.EDIT)
    }

    fun createNewBudget(startEnd: Pair<LocalDate, LocalDate?>) {

        val selectOldBudgetRowList = BudgetRowService.elemRoot().toList()

        val newBudgetMain = createNewBudgetOnly(startEnd)

        val otherBudgetRow = createOtherBudgetRow(newBudgetMain)

        newBudgetMain.copyBudgetRowList(selectOldBudgetRowList, otherBudgetRow)
    }

    private fun createNewBudgetOnly(startEnd: Pair<LocalDate, LocalDate?>): BudgetMain {
        val (start, end) = BudgetMain.budgetTypePeriod.getStartEndByDate(startEnd.first)

        val newBudgetMain = BudgetMain(name = BudgetMain.budgetTypePeriod.nameByTypeDate(start),
                typePeriod = BudgetMain.budgetTypePeriod.dbValue, startPeriod = start, endPeriod = end)

        BudgetMain.selectedBudget = newBudgetMain
        BudgetMainService.save(newBudgetMain)

        return newBudgetMain
    }

    private fun createOtherBudgetRow(budgetMain: BudgetMain): BudgetRow =
            BudgetRowService.save( BudgetRow.createOthersRow(budgetMain) )

    private fun BudgetMain.copyBudgetRowList(budgetRowList: List<BudgetRow>, otherBudgetRow: BudgetRow) {

        budgetRowList.forEach {
            val newBudgetRow = BudgetRowService.save( copyBudgetRow(it, otherBudgetRow))

            copyBudgetCategories(it.id!!, newBudgetRow.id!!)
        }
    }

    private fun BudgetMain.copyBudgetRow(source: BudgetRow, newOtherBudgetRow: BudgetRow): BudgetRow =
        if(source.isOther()) newOtherBudgetRow.apply { amount = source.amount }
        else source.createCopy(this)

    private fun copyBudgetCategories(sourceBudgetRowId: Int, destinationBudgetRowId: Int) {

        val executeQuery = insertCopyCategories(destinationBudgetRowId)

        logger.error("sourceBudgetRowId=$sourceBudgetRowId")
        logger.error("destinationBudgetRowId=$destinationBudgetRowId")
        logger.error("query=$executeQuery")

        orm.executeQuery(executeQuery, arrayOf(sourceBudgetRowId))
    }

    private fun insertCopyCategories(destBudgetRow: Int)=
            """insert into BUDGET_CATEGORY (ID, BUDGET_ROW, CATEGORY)
                select (SELECT COALESCE(MAX(ID), 0) + 1  from BUDGET_CATEGORY) + CATEGORY,
                $destBudgetRow, CATEGORY from BUDGET_CATEGORY where BUDGET_ROW = ?"""
}