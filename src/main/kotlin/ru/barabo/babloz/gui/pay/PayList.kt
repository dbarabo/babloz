package ru.barabo.babloz.gui.pay

import javafx.application.Platform
import javafx.geometry.Orientation
import javafx.scene.control.SplitPane
import javafx.scene.control.Tab
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import ru.barabo.babloz.db.entity.Pay
import ru.barabo.babloz.db.service.PayService
import ru.barabo.babloz.gui.custom.ChangeSelectEdit
import ru.barabo.babloz.main.ResourcesManager
import ru.barabo.db.service.StoreListener
import tornadofx.*

object PayList : Tab("Платежи", VBox()), StoreListener<List<Pay>> {

    private var splitPane: SplitPane? = null

    private var table: TableView<Pay>? = null

    private var selectPay: Pay? = null

    private var findTextField: TextField? = null

    init {
        //this.graphic = ResourcesManager.icon("tree.png")

        form {
            toolbar {
                button ("Новый", ResourcesManager.icon("new.png")).apply {
                    setOnAction { showNewPay() }

                    disableProperty().bind(PayEdit.isDisableEdit().not())
                }

                button ("Дублировать", ResourcesManager.icon("new.png")).apply {
                    setOnAction { twinPay() }

                    disableProperty().bind(PayEdit.isDisableEdit().not())
                }

                button ("Сохранить", ResourcesManager.icon("save.png")).apply {
                    setOnAction { savePay() }

                    disableProperty().bind(PayEdit.isDisableEdit())
                }

                button ("Отменить", ResourcesManager.icon("cancel.png")).apply {
                    setOnAction { cancelPay() }

                    disableProperty().bind(PayEdit.isDisableEdit())
                }

                textfield().apply {
                    findTextField = this


//                    focusedProperty().addListener(
//                            { _, _, isNewFocus ->
//
//                                if(!isNewFocus) {
//                                    findPay()
//                                }
//                            })
                }

                button ("", ResourcesManager.icon("find.png")).apply {

                    this.setOnAction { findPay() }

                    disableProperty().bind(PayEdit.isDisableEdit().not())
                }
            }

            splitpane(Orientation.HORIZONTAL, PayEdit).apply { splitPane = this }
        }
        VBox.setVgrow(splitPane, Priority.ALWAYS)

        PayService.addListener(this)
    }

    private fun findPay() {

        findTextField?.text?.let {PayService.setCriteria(it) }
    }

    private fun cancelPay() {

        PayEdit.saveOrCancelEditPay(ChangeSelectEdit.CANCEL)

        table?.requestFocus()
    }

    private fun savePay() {

        PayEdit.saveOrCancelEditPay(ChangeSelectEdit.SAVE)

        table?.requestFocus()
    }

    private fun twinPay() {
        PayEdit.changeSelectEditPay(selectPay!!.copy(id = null))
    }

    private fun showNewPay() {

        PayEdit.changeSelectEditPay(Pay())
    }

    override fun refreshAll(elemRoot: List<Pay>) {

        table?.let {
            Platform.runLater({ run {it.refresh()} })
            return
        }

        Platform.runLater({
            run {
                table?.let { splitPane?.items?.remove(it) }

                synchronized(elemRoot) {
                    table = table(elemRoot)
                }

                table?.selectionModel?.selectedItemProperty()?.addListener(
                        { _, _, newSelection ->
                            selectPay = newSelection

                            selectPay?.let { PayEdit.changeSelectEditPay(it) }

                            table?.requestFocus()
                        })

                splitPane?.items?.add(table)

                VBox.setVgrow(table, Priority.ALWAYS)
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