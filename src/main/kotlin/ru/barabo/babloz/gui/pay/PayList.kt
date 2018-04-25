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
import ru.barabo.babloz.main.ResourcesManager
import ru.barabo.db.service.StoreListener
import tornadofx.*

object PayList : Tab("Платежи", VBox()), StoreListener<List<Pay>> {

    private var splitPane: SplitPane? = null

    private var table: TableView<Pay>? = null

    private var selectPay: Pay? = null

    private var findTextField: TextField? = null

    init {
        form {
            toolbar {
                button ("Новый", ResourcesManager.icon("new.png")).apply {
                    setOnAction { showNewPay() }

                    disableProperty().bind(PaySaver.isDisableEdit().not())
                }

                button ("Дублировать", ResourcesManager.icon("new.png")).apply {
                    setOnAction { twinPay() }

                    disableProperty().bind(PaySaver.isDisableEdit().not())
                }

                button ("Сохранить", ResourcesManager.icon("save.png")).apply {
                    setOnAction { savePay() }

                    disableProperty().bind(PaySaver.isDisableEdit())
                }

                button ("Отменить", ResourcesManager.icon("cancel.png")).apply {
                    setOnAction { cancelPay() }

                    disableProperty().bind(PaySaver.isDisableEdit())
                }

                textfield().apply {
                    findTextField = this

                }

                button ("", ResourcesManager.icon("find.png")).apply {

                    this.setOnAction { findPay() }

                    disableProperty().bind(PaySaver.isDisableEdit().not())
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

        PaySaver.cancel()

        table?.requestFocus()
    }

    private fun savePay() {

        PaySaver.save()

        table?.requestFocus()
    }

    private fun twinPay() {
        PaySaver.changeSelectEditValue(selectPay!!.copy(id = null))
    }

    private fun showNewPay() {

        PaySaver.changeSelectEditValue(Pay())
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

                            selectPay?.let { PaySaver.changeSelectEditValue(it) }

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