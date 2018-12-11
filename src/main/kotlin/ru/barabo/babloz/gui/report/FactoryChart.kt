package ru.barabo.babloz.gui.report

import javafx.scene.chart.*
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import ru.barabo.babloz.db.entity.Category
import ru.barabo.babloz.db.service.report.ListenerCategoryTurn
import ru.barabo.babloz.db.service.report.ReportServiceCategoryTurn
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object FactoryChart : VBox(), ListenerCategoryTurn {

    private val categoryTurnLineChart = LineChart<String, Number>(CategoryAxis(), NumberAxis())

    init {
        ReportServiceCategoryTurn.addListener(this)
    }

    override fun changeInfo(dates: List<LocalDate>, turnCategories: Map<Category, IntArray>) {

        updateCategoryTurnLineChartData(dates, turnCategories)

        addChart(categoryTurnLineChart)
    }

    private fun updateCategoryTurnLineChartData(dates: List<LocalDate>, turnCategories: Map<Category, IntArray>) {

        categoryTurnLineChart.data.removeAll(categoryTurnLineChart.data)

        for(category in turnCategories.keys) {
            val line = XYChart.Series<String, Number>().apply { name = category.name }

            val turn = turnCategories[category]

            for((index, month) in dates.withIndex()) {
                line.data.add(XYChart.Data(month.formatMonth(), turn?.get(index)) )
            }

            categoryTurnLineChart.data.add(line)
        }
    }

    private fun addChart(chart: Chart) {
        if(children.contains(chart)) return

        children.removeAll(children)

        children.add(chart)

        VBox.setVgrow(chart, Priority.ALWAYS)
    }
}

private fun LocalDate.formatMonth() = DateTimeFormatter.ofPattern("yy.MM").format(this)