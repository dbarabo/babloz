package ru.barabo.babloz.gui.report

import javafx.scene.chart.XYChart
import javafx.scene.control.Tab
import javafx.scene.layout.VBox
import ru.barabo.babloz.db.service.report.categoryturn.ReportServiceCategoryTurn
import ru.barabo.babloz.db.service.report.costincome.ReportServiceCostsIncomeTurn
import ru.barabo.babloz.gui.pay.comboBoxDates
import tornadofx.addChildIfPossible
import tornadofx.fieldset
import tornadofx.form

object ReportCustomCostsIncomeTurn: Tab("Доходы и расходы", VBox()), ChangeTabSelected {

    init {
        form {

            fieldset("Категории отчета") {
                addChildIfPossible(checkTreeCategory(ReportServiceCostsIncomeTurn))
            }

            fieldset("Даты начала-конца отчета") {
                comboBoxDates(ReportServiceCostsIncomeTurn::setDateRange)
            }

            fieldset("Период дат") {
                comboBoxPeriodTypes(ReportServiceCostsIncomeTurn::periodType)
            }
        }
    }

    override fun processSelect() {
        ReportServiceCostsIncomeTurn.updateInfoListeners()
    }
}

object ReportCostsIncomeDrawChart {

    fun drawChart(chartMap: Map<DiagramViewType, XYChart<String, Number>>): XYChart<String, Number> {

        val chart = chartMap[DiagramViewType.BAR_CHART]!!

        val dates = ReportServiceCostsIncomeTurn.dateRangeByList()

        val costsIncomeTurn = ReportServiceCostsIncomeTurn.infoMap()

        return chart.drawChart(dates, costsIncomeTurn, { it.label }, ReportServiceCostsIncomeTurn.periodType)
    }
}