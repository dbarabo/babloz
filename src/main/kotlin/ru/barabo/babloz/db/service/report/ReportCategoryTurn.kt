package ru.barabo.babloz.db.service.report

import ru.barabo.babloz.db.entity.Category
import java.time.LocalDate

interface ReportCategoryTurn {

    fun addCategory(category: Category)

    fun removeCategory(category: Category)

    fun setDateRange(start: LocalDate, end: LocalDate)

    fun addListener(listener: ListenerCategoryTurn)

    fun setCategoryView(categoryView: CategoryView)
}