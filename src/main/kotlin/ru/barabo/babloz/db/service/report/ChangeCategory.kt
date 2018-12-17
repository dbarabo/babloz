package ru.barabo.babloz.db.service.report

import ru.barabo.babloz.db.entity.Category

interface ChangeCategory {

    val categorySet: MutableSet<Category>

    fun addCategory(category: Category) {
        categorySet.add(category)

        updateCategoryInfo()
    }

    fun removeCategory(category: Category) {
        categorySet.remove(category)

        updateCategoryInfo()
    }

    fun updateCategoryInfo()
}