package ru.barabo.babloz.db.service.budget

import ru.barabo.babloz.db.BablozOrm
import ru.barabo.babloz.db.entity.Category
import ru.barabo.babloz.db.entity.budget.BudgetCategory
import ru.barabo.db.service.StoreService

object BudgetCategoryService : StoreService<BudgetCategory, List<BudgetCategory>>(BablozOrm, BudgetCategory::class.java) {

    override fun elemRoot(): List<BudgetCategory> = dataList

    fun findByCategory(category: Category): BudgetCategory? = dataList.firstOrNull { it.category == category.id }
}