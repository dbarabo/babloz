package ru.barabo.babloz.gui

import javafx.application.Platform
import javafx.geometry.Orientation
import javafx.scene.control.Alert
import javafx.scene.control.SplitPane
import javafx.scene.control.Tab
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import ru.barabo.babloz.db.entity.Pay
import ru.barabo.babloz.db.service.PayService
import ru.barabo.babloz.main.ResourcesManager
import ru.barabo.db.service.StoreListener
import tornadofx.*

object PayList : Tab("Платежи", VBox()), StoreListener<List<Pay>> {

    private var splitPane: SplitPane? = null

    private var table: TableView<Pay>? = null

    private var selectPay: Pay? = null

    init {
        this.graphic = ResourcesManager.icon("tree.png")

        form {
            toolbar {
                button ("Новый платеж", ResourcesManager.icon("new.png")).setOnAction { showNewPay() }

                button ("Правка платежа", ResourcesManager.icon("edit.png")).setOnAction { showEditPay() }
            }

            splitpane(Orientation.HORIZONTAL, PayEdit).apply { splitPane = this }
        }
        PayService.addListener(this)
    }

    private fun showPay() {
    }

    private fun showNewPay() {
        showPay()

        PayEdit.editPay(selectPay!!.copy(id = null))
    }

    private const val ALERT_PAY_NOT_SELECT = "Встаньте на изменеямый платеж в таблице платежей"

    private fun showEditPay() {
        selectPay?.id?. let {
            showPay()
            PayEdit.editPay(selectPay!!) }

            ?: alert(Alert.AlertType.ERROR, ALERT_PAY_NOT_SELECT)
    }

    override fun refreshAll(elemRoot: List<Pay>) {

        Platform.runLater({
            run {
                table?.removeFromParent()

                synchronized(elemRoot) {
                    table = table(elemRoot)
                }

                table?.selectionModel?.selectedItemProperty()?.addListener(
                        { _, _, newSelection ->
                            selectPay = newSelection

                            selectPay?.let { PayEdit.editPay(it) }
                        })

                splitPane?.addChildIfPossible(table!!)
            }
        })
    }

    private fun table(rootGroup: List<Pay>): TableView<Pay> {
        return TableView<Pay>(rootGroup.observable()).apply {
            column("Создан", Pay::createPay)

            column("Счет", Pay::fromAccountPay)

            column("Назначение", Pay::namePay)

            column("Сумма", Pay::sumPay)

            column("Описание", Pay::descriptionPay)

            this.resizeColumnsToFitContent()
        }
    }
}