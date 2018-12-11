package ru.barabo.babloz.db.service.report

import ru.barabo.babloz.db.entity.Category
import java.time.LocalDate

interface ListenerCategoryTurn {

   fun changeInfo(dates: List<LocalDate>, turnCategories: Map<Category, IntArray>)
}