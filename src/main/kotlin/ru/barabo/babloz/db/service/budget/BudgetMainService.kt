package ru.barabo.babloz.db.service.budget

import ru.barabo.babloz.db.BablozOrm
import ru.barabo.babloz.db.entity.budget.BudgetMain
import ru.barabo.db.service.StoreService

object BudgetMainService: StoreService<BudgetMain, List<BudgetMain>>(BablozOrm) {

    override fun clazz(): Class<BudgetMain> = BudgetMain::class.java

    override fun elemRoot(): List<BudgetMain> = dataList
}