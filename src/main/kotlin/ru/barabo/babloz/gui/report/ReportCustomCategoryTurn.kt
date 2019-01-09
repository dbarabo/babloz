package ru.barabo.babloz.gui.report

import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventTarget
import javafx.scene.chart.XYChart
import javafx.scene.control.CheckBoxTreeItem
import javafx.scene.control.ComboBox
import javafx.scene.control.Tab
import javafx.scene.layout.VBox
import org.controlsfx.control.CheckTreeView
import ru.barabo.babloz.db.entity.Category
import ru.barabo.babloz.db.entity.group.GroupCategory
import ru.barabo.babloz.db.service.CategoryService
import ru.barabo.babloz.db.service.report.ChangeEntity
import ru.barabo.babloz.db.service.report.categoryturn.CategoryView
import ru.barabo.babloz.db.service.report.categoryturn.ReportServiceCategoryTurn
import ru.barabo.babloz.db.service.report.restaccount.ReportServiceRestAccounts
import ru.barabo.babloz.gui.pay.PayEdit.rowHeight
import ru.barabo.babloz.gui.pay.comboBoxDates
import ru.barabo.babloz.gui.pay.defaultRowCount
import tornadofx.*

object ReportCustomCategoryTurn : Tab("Динамика расходов по категориям", VBox()), ChangeTabSelected {
    init {
        form {

            fieldset("Категории отчета") {
                addChildIfPossible(checkTreeCategory(ReportServiceCategoryTurn))
            }

            fieldset("Даты начала-конца отчета") {
                comboBoxDates(ReportServiceCategoryTurn::setDateRange)
            }

            fieldset("Рисовать категории") {
                comboBoxCustomCategory(ReportServiceCategoryTurn::setCategoryView)
            }

            fieldset("Вид диаграммы") {
                comboBoxDiagramViewType(ReportCategoryTurnDrawChart::diagramViewType)
            }

            fieldset("Период дат") {
                comboBoxPeriodTypes(ReportServiceCategoryTurn::periodType)
            }
        }
    }

    override fun processSelect() {
        ReportServiceCategoryTurn.updateInfoListeners()
    }
}

object ReportCategoryTurnDrawChart {

    var diagramViewType: DiagramViewType = DiagramViewType.LINE_CHART
        set(value) {
            field = value

            ReportServiceCategoryTurn.updateInfoListeners()
        }

    fun drawChart(chartMap: Map<DiagramViewType, XYChart<String, Number>>): XYChart<String, Number> {

        val chart = chartMap[diagramViewType]!!

        val dates = ReportServiceCategoryTurn.dateRangeByList()

        val categoryTurn = ReportServiceCategoryTurn.infoMap()

        return chart.drawChart(dates, categoryTurn, { it.name }, ReportServiceCategoryTurn.periodType)
    }
}

fun EventTarget.comboBoxCustomCategory(processCategoryView: (CategoryView)->Unit): ComboBox<CategoryView> {

    val dateSelectProperty = SimpleObjectProperty<CategoryView>(CategoryView.ALL)

    return combobox<CategoryView>(property = dateSelectProperty, values = CategoryView.values().toList()).apply {

        selectionModel?.selectedItemProperty()?.addListener { _, _, newSelection ->

            processCategoryView(newSelection)
        }
    }
}

internal fun checkTreeCategory(changeEntity: ChangeEntity<Category>) =
    CheckTreeView<GroupCategory>(CheckBoxTreeItem<GroupCategory>(CategoryService.categoryRoot()) ).apply {

        this.populate (itemFactory = {
            CheckBoxTreeItem(it, null, it.category.isSelected ?:0 != 0).apply {

                selectedProperty().addListener { _, _, newValue ->

                    if(newValue == true) {
                        changeEntity.addEntity(this.value.category)
                    } else {
                        changeEntity.removeEntity(this.value.category)
                    }
                }
            }
        },

        childFactory = { it.value.child })

        this.isShowRoot = false

        this.root.isExpanded = true

        prefHeight = rowHeight() * defaultRowCount(GroupCategory.countRoot()) * 2
    }
