package ru.barabo.babloz.gui.report

import javafx.scene.control.Tab
import javafx.scene.layout.VBox
import ru.barabo.babloz.db.service.report.costincome.ReportServiceCostsIncomeTurn
import ru.barabo.babloz.gui.pay.comboBoxDates
import tornadofx.addChildIfPossible
import tornadofx.fieldset
import tornadofx.form

object ReportCustomCostsIncomeTurn: Tab("Доходы и расходы", VBox()) {

    init {
        form {

            fieldset("Категории отчета") {
                addChildIfPossible(checkTreeCategory(ReportServiceCostsIncomeTurn))
            }

            fieldset("Период отчета") {
                comboBoxDates(ReportServiceCostsIncomeTurn::setDateRange)
            }
        }
    }
}