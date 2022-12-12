package ru.barabo.babloz.gui.budget

import javafx.application.Platform
import javafx.collections.ObservableList
import javafx.geometry.Orientation
import javafx.scene.control.SplitPane
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.util.Callback
import ru.barabo.babloz.db.entity.budget.BudgetRow
import ru.barabo.db.EditType
import ru.barabo.db.service.StoreListener
import ru.barabo.gui.javafx.tableCellProgressCallback
import tornadofx.addChildIfPossible
import tornadofx.column
import tornadofx.observable
import tornadofx.resizeColumnsToFitContent
import kotlin.reflect.KProperty1


internal object BudgetRowTable : StoreListener<List<BudgetRow>> {

   // private val logger = LoggerFactory.getLogger(BudgetRowTable::class.java)

    private var tableRowBudget: TableView<BudgetRow>? = null

    private var splitPane: SplitPane? = null

    @Volatile private var isEmptyRow = true

    override fun refreshAll(elemRoot: List<BudgetRow>, refreshType: EditType) {

        Platform.runLater {
            run {
                tableRowBudget?.apply {

                    changeResizeColumns(elemRoot)
                    refresh()
                    requestFocus()
                } ?: createPanelRowBudget(elemRoot)

                BudgetRow.budgetRowSelected = if (elemRoot.isEmpty()) null else tableRowBudget?.selectionModel?.selectedItem
            }
        }
    }

    private fun createPanelRowBudget(elemRoot: List<BudgetRow>) {

        splitPane = SplitPane().apply {
            orientation = Orientation.VERTICAL

            VBox.setVgrow(this, Priority.ALWAYS)
        }

        tableRowBudget = table(elemRoot)

        splitPane?.addChildIfPossible(tableRowBudget!!)

        splitPane?.addChildIfPossible(BudgetRowEdit)

        splitPane?.setDividerPositions(0.6)

        BudgetList.splitPane?.addChildIfPossible(splitPane!!)
    }

    private fun table(rootGroup: List<BudgetRow>): TableView<BudgetRow> {
        return TableView<BudgetRow>(rootGroup as ObservableList).apply {
            column("Строка бюджета", BudgetRow::name)

            column("Выделено", BudgetRow::amountFormat )

            column("Освоено", BudgetRow::amountRealFormat)

            columns.add(progressColumn() )

            columns.add(progressColumnPartPan() )

            columns.add(progressColumnPartReal() )

            selectionModel?.selectedItemProperty()?.addListener { _, _, newSelection ->
                        BudgetRow.budgetRowSelected = newSelection
                    }

            changeResizeColumns(rootGroup)
        }
    }

    private fun TableView<BudgetRow>.changeResizeColumns(rootGroup: List<BudgetRow>) {
        if(rootGroup.isNotEmpty() && isEmptyRow) {
            isEmptyRow = false
            this.resizeColumnsToFitContent()
        }
    }

    private fun progressColumn(): TableColumn<BudgetRow, Double?> = progressColumnAny("Процент", BudgetRow::percentAll)
    /*{
        return TableColumn<BudgetRow, Double?>("Процент").apply {

            cellValueFactory = Callback { observable(it.value, BudgetRow::percentAll) } //PropertyValueFactory<BudgetRow, Double?>("percentAll")

            cellFactory = tableCellProgressCallback()
        }
    }*/

    private fun progressColumnPartPan(): TableColumn<BudgetRow, Double?> = progressColumnAny("Доля плана", BudgetRow::partPlan)

    private fun progressColumnPartReal(): TableColumn<BudgetRow, Double?> = progressColumnAny("Доля реал", BudgetRow::partReal)

    private fun progressColumnAny(labelColumn: String, columnValue: KProperty1<BudgetRow, Double>): TableColumn<BudgetRow, Double?> {
        return TableColumn<BudgetRow, Double?>(labelColumn).apply {

            cellValueFactory = Callback { observable(it.value, columnValue) }

            cellFactory = tableCellProgressCallback()
        }
    }
}