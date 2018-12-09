package ru.barabo.babloz.gui.report

import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.control.Tab
import javafx.scene.layout.VBox
import ru.barabo.babloz.db.service.report.ReportService
import tornadofx.addChildIfPossible
import tornadofx.form
import java.time.LocalDate
import java.time.format.DateTimeFormatter


object ReportTab : Tab("Отчеты", VBox()) {

    init {

        val categoryTurnLineChart = LineChart<String, Number>(CategoryAxis(), NumberAxis())

        val months = ReportService.getPeriods()

        for (category in ReportService.getCategoryTurnList()) {

            val line = XYChart.Series<String, Number>()

            line.name = category.name

            val turnMonths = ReportService.getTurnsByCategory(category, months)

            for((index, month) in months.withIndex()) {
                line.data.add(XYChart.Data(month.formatMonth(), turnMonths[index]) )
            }

            categoryTurnLineChart.data.add(line)
        }

        form {
            addChildIfPossible(categoryTurnLineChart)
        }

    }
}

fun LocalDate.formatMonth() = DateTimeFormatter.ofPattern("yy.MM").format(this)