package ru.barabo.babloz.gui.budget

import javafx.application.Platform
import javafx.geometry.Orientation
import javafx.scene.control.SplitPane
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.util.Callback
import org.slf4j.LoggerFactory
import ru.barabo.babloz.db.entity.budget.BudgetRow
import ru.barabo.db.EditType
import ru.barabo.db.service.StoreListener
import ru.barabo.gui.javafx.TableCellProgress
import ru.barabo.gui.javafx.tableCellProgressCallback
import tornadofx.*


internal object BudgetRowTable : StoreListener<List<BudgetRow>> {

    private val logger = LoggerFactory.getLogger(BudgetRowTable::class.java)

    private var tableRowBudget: TableView<BudgetRow>? = null

    private var splitPane: SplitPane? = null

    override fun refreshAll(elemRoot: List<BudgetRow>, refreshType: EditType) {

        Platform.runLater({
            run {
                tableRowBudget?.apply {

                    refresh()

                    requestFocus()

                } ?: createPanelRowBudget(elemRoot)

                BudgetRow.budgetRowSelected = if (elemRoot.isEmpty()) null else tableRowBudget?.selectionModel?.selectedItem
            }
        })
    }

    private fun createPanelRowBudget(elemRoot: List<BudgetRow>) {

        splitPane = SplitPane().apply {
            orientation = Orientation.VERTICAL

            VBox.setVgrow(this, Priority.ALWAYS)
        }

        tableRowBudget = table(elemRoot)

        splitPane?.addChildIfPossible(tableRowBudget!!)

        splitPane?.addChildIfPossible(BudgetRowEdit)

        BudgetList.splitPane?.addChildIfPossible(splitPane!!)
    }

    private fun table(rootGroup: List<BudgetRow>): TableView<BudgetRow> {
        return TableView<BudgetRow>(rootGroup.observable()).apply {
            column("Строка бюджета", BudgetRow::name)

            column("Выделено", BudgetRow::amount)

            column("Освоено", BudgetRow::amountReal)

            columns.add(progressColumn() )

            selectionModel?.selectedItemProperty()?.addListener(
                    { _, _, newSelection ->
                        BudgetRow.budgetRowSelected = newSelection
                    })

            this.resizeColumnsToFitContent()
        }
    }

    private fun TableView<BudgetRow>.progressColumn(): TableColumn<BudgetRow, Double?> {
        return TableColumn<BudgetRow, Double?>("Процент").apply {

            cellValueFactory = Callback { observable(it.value, BudgetRow::percentAll) } //PropertyValueFactory<BudgetRow, Double?>("percentAll")

            cellFactory = tableCellProgressCallback()
        }
    }
}