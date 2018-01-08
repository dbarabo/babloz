package ru.barabo.babloz.gui

import javafx.application.Platform
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
    private var forma: Form? = null

    private var table: TableView<Pay>? = null

    private var selectPay: Pay? = null

    init {
        this.graphic = ResourcesManager.icon("tree.png")

        forma = form {
            toolbar {
                button ("Новый платеж", ResourcesManager.icon("new.png")).setOnAction { showNewPay() }

                button ("Правка платежа", ResourcesManager.icon("edit.png")).setOnAction { showEditPay() }
            }
        }
        PayService.addListener(this)
    }

    private fun showPay() {
//        if(!tabPane.tabs.contains(CategoryEdit)) {
//            tabPane.tabs.add(CategoryEdit)
//        }
    }

    private fun showNewPay() {
        showPay()

        //CategoryEdit.editCategory(selectPay!!..copy(id = null, name = ""))
    }

    private val ALERT_CATEGORY_NOT_SELECT = "Встаньте на изменеямую категорию в таблице категорий"

    private fun showEditPay() {
//        selectGroupCategory?.category?.id
//                ?. let { showCategory()
//                    CategoryEdit.editCategory(selectGroupCategory!!.category) }
//
//                ?: alert(Alert.AlertType.ERROR, ALERT_CATEGORY_NOT_SELECT)
    }

    override fun refreshAll(elemRoot: List<Pay>) {

        Platform.runLater({
            run {
                table?.removeFromParent()

                synchronized(elemRoot) {
                    table = table(elemRoot)
                }

                //treeTable?.root?.children?.forEach { it.expandedProperty().set(true) }

                table?.selectionModel?.selectedItemProperty()?.addListener(
                        { _, _, newSelection ->
                            selectPay = newSelection
                        })

                forma?.addChildIfPossible(table!!)

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