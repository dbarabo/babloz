package ru.barabo.babloz.db.service.report.costincome

import ru.barabo.babloz.db.entity.Category
import ru.barabo.babloz.db.entity.CategoryType
import ru.barabo.babloz.db.service.report.CallBackUpdateListener
import ru.barabo.babloz.db.service.report.ChangeDateRange
import ru.barabo.babloz.db.service.report.ChangeEntity

interface ReportCostsIncomeTurn : ChangeEntity<Category>, ChangeDateRange, CallBackUpdateListener<CategoryType>