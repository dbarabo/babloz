package ru.barabo.babloz.gui.budget

import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.geometry.Orientation
import javafx.scene.control.*
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.util.Callback
import org.slf4j.LoggerFactory
import ru.barabo.babloz.db.BudgetTypePeriod
import ru.barabo.babloz.db.entity.budget.BudgetMain
import ru.barabo.babloz.db.entity.budget.BudgetRow
import ru.barabo.babloz.db.service.budget.BudgetMainService
import ru.barabo.babloz.db.service.budget.BudgetRowService
import ru.barabo.babloz.gui.account.addElemByLeft
import ru.barabo.babloz.main.ResourcesManager
import ru.barabo.db.EditType
import ru.barabo.db.service.StoreListener
import ru.barabo.gui.javafx.tableCellProgressCallback
import tornadofx.*

object BudgetList : Tab("Бюджет", VBox()), StoreListener<List<BudgetMain>> {

    private val logger = LoggerFactory.getLogger(BudgetList::class.java)

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
                button ("Удалить строку Бюджета", ResourcesManager.icon("delete.png")).apply {
                    setOnAction { deleteBudgetRow() }
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

        BudgetMainService.createNewBudget(result)
    }

    private fun showNewBudgetRow() {

        BudgetMain.selectedBudget?.let { BudgetRowService.save(BudgetRow.createNewEmptyRow(it)) }
    }

    private fun deleteBudgetRow() {
        BudgetRow.budgetRowSelected?.let { BudgetRowService.delete(it) }
    }

    override fun refreshAll(elemRoot: List<BudgetMain>, refreshType: EditType) {

        Platform.runLater({
            run {

                tableMainBudget?.refresh() ?: run { tableMainBudget = createTableMainBudget(elemRoot) }

                BudgetMain.selectedBudget = if (elemRoot.isEmpty()) null else tableMainBudget?.selectionModel?.selectedItem
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

        return TableView<BudgetMain>(rootGroup as ObservableList).apply {
            column("Название", BudgetMain::name)

            column("Выделено", BudgetMain::amountBudgetFormat)

            column("Освоено", BudgetMain::amountRealFormat)

            columns.add(progressColumn() )

            this.resizeColumnsToFitContent()
        }
    }
}

private fun TableView<BudgetMain>.progressColumn(): TableColumn<BudgetMain, Double?> {
    return TableColumn<BudgetMain, Double?>("Процент").apply {

        cellValueFactory = Callback { observable(it.value, BudgetMain::percentAll) }

        cellFactory = tableCellProgressCallback()
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

