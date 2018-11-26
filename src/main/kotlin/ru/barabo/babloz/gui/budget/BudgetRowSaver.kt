package ru.barabo.babloz.gui.budget

import org.slf4j.LoggerFactory
import ru.barabo.babloz.db.entity.budget.BudgetRow
import ru.barabo.babloz.db.service.budget.BudgetRowService
import ru.barabo.babloz.gui.binding.AbstractSaver
import ru.barabo.babloz.gui.custom.ChangeSelectEdit

object BudgetRowSaver : AbstractSaver<BudgetRow, BudgetRowBind>(BudgetRowBind::class.java, changeSelectEdit = ChangeSelectEdit.CONFIRM) {

    private val logger = LoggerFactory.getLogger(BudgetRowSaver::class.java)

    override fun serviceSave(value: BudgetRow) {

        logger.error("serviceSave value=$value")

        BudgetRowService.save(value)
    }
}