package ru.barabo.babloz.gui.report

import javafx.scene.chart.*
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import ru.barabo.babloz.db.entity.Category
import ru.barabo.babloz.db.entity.CategoryType
import ru.barabo.babloz.db.service.report.categoryturn.ReportServiceCategoryTurn
import ru.barabo.babloz.db.service.report.costincome.ReportServiceCostsIncomeTurn
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object FactoryChart : VBox()  {

    private val categoryTurnLineChart = LineChart<String, Number>(CategoryAxis(), NumberAxis())

    private val costsIncomeTurnBarChart = BarChart<String, Number>(CategoryAxis(), NumberAxis())

    init {
        ReportServiceCategoryTurn.addListener(::changeInfoCategoryTurn)

        ReportServiceCostsIncomeTurn.addListener(::changeInfoCostsIncomeTurn)
    }

    private fun changeInfoCostsIncomeTurn(dates: List<LocalDate>, turnCostsIncome: Map<*, IntArray>) {

        @Suppress("UNCHECKED_CAST")
        updateCostsIncomeTurnChartData(dates, turnCostsIncome as? Map<CategoryType, IntArray> )

        addChart(costsIncomeTurnBarChart)
    }

    private fun changeInfoCategoryTurn(dates: List<LocalDate>, turnCategories: Map<*, IntArray>) {

        @Suppress("UNCHECKED_CAST")
        updateCategoryTurnLineChartData(dates, turnCategories as? Map<Category, IntArray>)

        addChart(categoryTurnLineChart)
    }

    private fun updateCostsIncomeTurnChartData(dates: List<LocalDate>, turnCostsIncome: Map<CategoryType, IntArray>?) {

        costsIncomeTurnBarChart.data.removeAll(costsIncomeTurnBarChart.data)

        turnCostsIncome ?: return

        for(categoryType in turnCostsIncome.keys) {
            val line = XYChart.Series<String, Number>().apply { name = categoryType.label }

            val turn = turnCostsIncome[categoryType]

            for((index, month) in dates.withIndex()) {
                line.data.add(XYChart.Data(month.formatMonth(), turn?.get(index)) )
            }

            costsIncomeTurnBarChart.data.add(line)
        }
    }

    private fun updateCategoryTurnLineChartData(dates: List<LocalDate>, turnCategories: Map<Category, IntArray>?) {

        categoryTurnLineChart.data.removeAll(categoryTurnLineChart.data)

        turnCategories ?: return

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