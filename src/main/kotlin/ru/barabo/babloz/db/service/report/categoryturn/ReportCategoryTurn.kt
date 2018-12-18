package ru.barabo.babloz.db.service.report.categoryturn

import ru.barabo.babloz.db.entity.Category
import ru.barabo.babloz.db.service.report.CallBackUpdateListener
import ru.barabo.babloz.db.service.report.ChangeDateRange
import ru.barabo.babloz.db.service.report.ChangeEntity

interface ReportCategoryTurn : ChangeEntity<Category>, ChangeDateRange, CallBackUpdateListener<Category> {

    fun setCategoryView(categoryView: CategoryView)
}