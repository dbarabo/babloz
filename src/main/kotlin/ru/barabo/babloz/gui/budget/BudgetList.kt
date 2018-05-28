package ru.barabo.babloz.gui.budget

import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventTarget
import javafx.geometry.Orientation
import javafx.scene.control.ComboBox
import javafx.scene.control.SplitPane
import javafx.scene.control.Tab
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import ru.barabo.babloz.db.BudgetTypePeriod
import ru.barabo.babloz.db.entity.BudgetMain
import ru.barabo.babloz.db.entity.BudgetRow
import ru.barabo.babloz.db.service.BudgetMainService
import ru.barabo.babloz.db.service.BudgetRowService
import ru.barabo.babloz.gui.account.addElemByLeft
import ru.barabo.babloz.main.ResourcesManager
import ru.barabo.db.EditType
import ru.barabo.db.service.StoreListener
import tornadofx.*

object BudgetList : Tab("Бюджет", VBox()), StoreListener<List<BudgetMain>> {

    private var tableMainBudget: TableView<BudgetMain>? = null

    private var budgetRowTable: BudgetRowTable? = BudgetRowTable::class.objectInstance

    internal var splitPane: SplitPane? = null

    init {
        form {
            toolbar {
                button ("Новый Бюджет", ResourcesManager.icon("new.png")).apply {
                    setOnAction { showNewBudgetMain() }
                }
                button ("Новая строка Бюджета", ResourcesManager.icon("new.png")).apply {
                    setOnAction { showNewBudgetRow() }
                }
                separator {  }

                comboBoxBudgetTypes({newType -> BudgetMain.budgetTypePeriod = newType})

            }
            splitpane(Orientation.HORIZONTAL).apply { splitPane = this}
        }

        VBox.setVgrow(splitPane, Priority.ALWAYS)

        BudgetMainService.addListener(this)

        BudgetRowService.addListener(budgetRowTable!!)
    }

    private fun showNewBudgetMain() {
        val result = CreateBudgetMain.showAndWait().orElseGet { null  }?.let { it } ?: return

        val (start, end) = BudgetMain.budgetTypePeriod.getStartEndByDate(result.first)

        val budgetMain = BudgetMain(name = BudgetMain.budgetTypePeriod.nameByTypeDate(start),
                typePeriod = BudgetMain.budgetTypePeriod.dbValue, startPeriod = start, endPeriod = end)

        BudgetMainService.save(budgetMain)
    }

    private fun showNewBudgetRow() {

    }

    override fun refreshAll(elemRoot: List<BudgetMain>, refreshType: EditType) {
        Platform.runLater({
            run {

                tableMainBudget?.refresh() ?: run { tableMainBudget = createTableMainBudget(elemRoot) }
            }
        })
    }

    private fun createTableMainBudget(elemRoot: List<BudgetMain>) = table(elemRoot).apply {

            selectionModel?.selectedItemProperty()?.addListener(
                { _, _, newSelection ->
                    BudgetMain.selectedBudget = newSelection
                })

            splitPane?.addElemByLeft(this, 0.5)
        }

    private fun table(rootGroup: List<BudgetMain>): TableView<BudgetMain> {
        return TableView<BudgetMain>(rootGroup.observable()).apply {
            column("Название", BudgetMain::name)

            column("Выделено", BudgetMain::amountBudget)

            column("Освоено", BudgetMain::amountReal)

            this.resizeColumnsToFitContent()
        }
    }
}

fun EventTarget.comboBoxBudgetTypes(processBudjectTypePeriod: (BudgetTypePeriod)->Unit): ComboBox<BudgetTypePeriod> {

    val budgetTypePeriodProperty = SimpleObjectProperty<BudgetTypePeriod>(BudgetMain.budgetTypePeriod)

    return combobox<BudgetTypePeriod>(property = budgetTypePeriodProperty, values = BudgetTypePeriod.values().toList()).apply {

        selectionModel?.selectedItemProperty()?.addListener(
                { _, _, newSelection ->

                    processBudjectTypePeriod(newSelection)
                })
    }
}

internal object BudgetRowTable : StoreListener<List<BudgetRow>> {

    private var tableRowBudget: TableView<BudgetRow>? = null

    override fun refreshAll(elemRoot: List<BudgetRow>, refreshType: EditType) {
        Platform.runLater({
            run {
                tableRowBudget?.refresh() ?: run {tableRowBudget = createTableRowBudget(elemRoot) }
            }
        })
    }

    private fun createTableRowBudget(elemRoot: List<BudgetRow>) = table(elemRoot).apply {

        BudgetList.splitPane?.addChildIfPossible(this)
    }

    private fun table(rootGroup: List<BudgetRow>): TableView<BudgetRow> {
        return TableView<BudgetRow>(rootGroup.observable()).apply {
            column("Строка бюджета", BudgetRow::name)

            column("Выделено", BudgetRow::amount)

            column("Освоено", BudgetRow::amountReal)

            this.resizeColumnsToFitContent()
        }
    }

}