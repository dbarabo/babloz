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

        BudgetMain.selectedBudget = budgetMain
        BudgetMainService.save(budgetMain)

        BudgetRowService.save(BudgetRow.createOthersRow(budgetMain) )
    }

    private fun showNewBudgetRow() {

        BudgetMain.selectedBudget?.let { BudgetRowService.save(BudgetRow.createNewEmptyRow(it)) }
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

            splitPane?.addElemByLeft(this, 0.3)
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

    private val logger = LoggerFactory.getLogger(BudgetRowTable::class.java)

    private var tableRowBudget: TableView<BudgetRow>? = null

    private var splitPane: SplitPane? = null

    override fun refreshAll(elemRoot: List<BudgetRow>, refreshType: EditType) {

        Platform.runLater({
            run {
                tableRowBudget?.refresh() ?: createPanelRowBudget(elemRoot)

                BudgetRow.budgetRowSelected = if (elemRoot.isEmpty()) null else tableRowBudget?.selectionModel?.selectedItem
            }
        })
    }

    private fun createPanelRowBudget(elemRoot: List<BudgetRow>) {

        splitPane = SplitPane().apply {
            orientation = Orientation.HORIZONTAL

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

            selectionModel?.selectedItemProperty()?.addListener(
                    { _, _, newSelection ->
                        BudgetRow.budgetRowSelected = newSelection
                    })

            this.resizeColumnsToFitContent()
        }
    }
}