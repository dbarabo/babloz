package ru.barabo.babloz.gui.report

import javafx.application.Platform
import javafx.scene.chart.*
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import ru.barabo.babloz.db.service.report.categoryturn.ReportServiceCategoryTurn
import ru.barabo.babloz.db.service.report.costincome.ReportServiceCostsIncomeTurn
import ru.barabo.babloz.db.service.report.restaccount.ReportServiceRestAccounts

object FactoryChart : VBox()  {

    private val lineChart = LineChart<String, Number>(CategoryAxis(), NumberAxis())

    private val barChart = BarChart<String, Number>(CategoryAxis(), NumberAxis())

    private val mapDiagramViewType: Map<DiagramViewType, XYChart<String,Number>> = mapOf(
            DiagramViewType.LINE_CHART to lineChart,
            DiagramViewType.BAR_CHART to barChart
    )

    init {
        ReportServiceCategoryTurn.addListener{ processInfo(ReportCategoryTurnDrawChart::drawChart) }

        ReportServiceCostsIncomeTurn.addListener { processInfo(ReportCostsIncomeDrawChart::drawChart) }

        ReportServiceRestAccounts.addListener { processInfo(ReportAccountRestDrawChart::drawChart) }
    }

    private fun processInfo(processDrawChart: (Map<DiagramViewType, XYChart<String, Number>>)->XYChart<String, Number>) {

        Platform.runLater {
            run {
                mapDiagramViewType.values.forEach { it.data.removeAll(it.data) }

                val chart = processDrawChart(mapDiagramViewType)

                addChart(chart)
            }
        }
    }

    private fun addChart(chart: Chart) {
        if(children.contains(chart)) return

        children.removeAll(children)

        children.add(chart)

        VBox.setVgrow(chart, Priority.ALWAYS)
    }
}
