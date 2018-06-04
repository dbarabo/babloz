package ru.barabo.babloz.db.service.budget

import ru.barabo.babloz.db.BablozOrm
import ru.barabo.babloz.db.entity.budget.BudgetRow
import ru.barabo.db.service.StoreService

object BudgetRowService: StoreService<BudgetRow, List<BudgetRow> >(BablozOrm) {

    override fun clazz(): Class<BudgetRow> = BudgetRow::class.java

    override fun elemRoot(): List<BudgetRow> = dataList
}