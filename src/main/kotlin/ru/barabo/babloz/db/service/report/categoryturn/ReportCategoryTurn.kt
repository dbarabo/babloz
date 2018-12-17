package ru.barabo.babloz.db.service.report.categoryturn

import ru.barabo.babloz.db.service.report.CallBackUpdateListener
import ru.barabo.babloz.db.service.report.ChangeCategory
import ru.barabo.babloz.db.service.report.ChangeDateRange

interface ReportCategoryTurn : ChangeCategory, ChangeDateRange, CallBackUpdateListener {

    fun setCategoryView(categoryView: CategoryView)
}