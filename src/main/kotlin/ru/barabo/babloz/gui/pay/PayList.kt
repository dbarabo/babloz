package ru.barabo.babloz.gui.pay

import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.geometry.Orientation
import javafx.scene.control.*
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.controlsfx.control.CheckComboBox
import org.slf4j.LoggerFactory
import ru.barabo.babloz.db.entity.Account
import ru.barabo.babloz.db.entity.Category
import ru.barabo.babloz.db.entity.Pay
import ru.barabo.babloz.db.entity.Project
import ru.barabo.babloz.db.service.AccountService
import ru.barabo.babloz.db.service.AccountService.ALL_ACCOUNT
import ru.barabo.babloz.db.service.AccountService.accountAllList
import ru.barabo.babloz.db.service.CategoryService
import ru.barabo.babloz.db.service.CategoryService.ALL_CATEGORY
import ru.barabo.babloz.db.service.CategoryService.categoryAllList
import ru.barabo.babloz.db.service.PayService
import ru.barabo.babloz.db.service.ProjectService
import ru.barabo.babloz.db.service.ProjectService.ALL_PROJECT
import ru.barabo.babloz.db.service.ProjectService.projectAllList
import ru.barabo.babloz.gui.MainView.Companion.selectPayTab
import ru.barabo.babloz.gui.account.addElemByLeft
import ru.barabo.babloz.gui.pay.filter.ComboxFilter
import ru.barabo.babloz.gui.pay.filter.DateSelect
import ru.barabo.babloz.gui.pay.filter.ModalDateSelect
import ru.barabo.babloz.main.ResourcesManager
import ru.barabo.db.EditType
import ru.barabo.db.service.StoreListener
import tornadofx.*
import java.time.LocalDate
import java.time.LocalDateTime

object PayList : Tab("Платежи", VBox()), StoreListener<List<Pay>> {

    private val logger = LoggerFactory.getLogger(PayList::class.java)!!

    private var splitPane: SplitPane? = null

    private var table: TableView<Pay>? = null

    private var selectPay: Pay? = null

    private var findTextField: TextField? = null

    init {
        form {
            toolbar {
                button ("", ResourcesManager.icon("new.png")).apply {
                    setOnAction { showNewPay() }

                    tooltip("Создать Новую запись")

                    disableProperty().bind(PaySaver.isDisableEdit().not())
                }

                button ("", ResourcesManager.icon("duple.png")).apply {
                    setOnAction { twinPay() }

                    tooltip("Дублировать текущую запись")

                    disableProperty().bind(PaySaver.isDisableEdit().not())
                }

                button ("", ResourcesManager.icon("save.png")).apply {
                    setOnAction { savePay() }

                    tooltip("Сохранить запись")

                    disableProperty().bind(PaySaver.isDisableEdit())
                }

                button ("", ResourcesManager.icon("cancel.png")).apply {

                    tooltip("Отменить сохранение записи")

                    setOnAction { cancelPay() }

                    disableProperty().bind(PaySaver.isDisableEdit())
                }

                separator()

                button ("", ResourcesManager.icon("delete.png")).apply {

                    tooltip("Удалить запись!")

                    setOnAction { deletePay() }

                    disableProperty().bind(PaySaver.isDisableEdit().not())
                }

                textfield().apply {
                    findTextField = this
                    maxWidth = 100.0
                }

                button ("", ResourcesManager.icon("find.png")).apply {

                    this.setOnAction { findPay() }

                    //disableProperty().bind(PaySaver.isDisableEdit().not())
                }

                comboBoxDates(PayService::setDateFilter)

                addChildIfPossible( checkComboAccountList() )

                addChildIfPossible( checkComboCategoryList() )

                addChildIfPossible( checkComboProjectList() )
            }

            splitpane(Orientation.HORIZONTAL, PayEdit).apply { splitPane = this }
        }
        VBox.setVgrow(splitPane, Priority.ALWAYS)

        PayService.addListener(this)
    }

    private fun checkComboProjectList(): CheckComboBox<Project> = ComboxFilter(
            projectAllList().observable(), ALL_PROJECT, PayService::setProjectFilter, ProjectService::projectList)

    private fun checkComboCategoryList(): CheckComboBox<Category> = ComboxFilter(
           categoryAllList().observable(), ALL_CATEGORY, PayService::setCategoryFilter, CategoryService::categoryList)

    private fun checkComboAccountList(): CheckComboBox<Account> =  ComboxFilter(
            accountAllList().observable(), ALL_ACCOUNT, PayService::setAccountFilter, AccountService::accountList)

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

    private fun deletePay() {

        selectPay?.let { PayService.delete(it) }

        table?.requestFocus()
    }

    private fun twinPay() {
        PaySaver.changeSelectEditValue(selectPay!!.copy(id = null, created = LocalDateTime.now() ))
    }

    private fun showNewPay() {

        PaySaver.changeSelectEditValue(Pay())
    }

    override fun refreshAll(elemRoot: List<Pay>, refreshType: EditType) {

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

                splitPane?.addElemByLeft(table!!, 0.5)
             }
        })
    }

    private fun table(rootGroup: List<Pay>): TableView<Pay> {
        return TableView<Pay>(rootGroup as ObservableList<Pay>).apply {
            column("Создан", Pay::createPay)

            column("Счет", Pay::fromAccountPay)

            column("Назначение", Pay::namePay)

            column("Сумма", Pay::sumPay)

            column("Описание", Pay::descriptionPay)

            column("Проект", Pay::projectPay)

            column("Субъект", Pay::personPay)

            this.resizeColumnsToFitContent()
        }
    }
}

fun EventTarget.comboBoxDates(processDates: (LocalDate, LocalDate)->Unit): ComboBox<DateSelect> {

    val dateSelectProperty = SimpleObjectProperty<DateSelect>(DateSelect.ALL_PERIOD)

    return combobox<DateSelect>(property = dateSelectProperty, values = DateSelect.values().toList()).apply {

        selectionModel?.selectedItemProperty()?.addListener(
                { _, _, newSelection ->

                    if(newSelection === DateSelect.DATE_PERIOD) {
                        val result = ModalDateSelect.showAndWait()
                        if(result.isPresent) {
                            DateSelect.startDate = result.get().first
                            DateSelect.endDate = result.get().second
                        }
                    }

                    processDates(newSelection.start(), newSelection.end() )
                })
    }
}

fun gotoPayListByDateCategory(start: LocalDate, end: LocalDate, categories: List<Category>) {

    PayService.setDateFilter(start, end)

    PayService.setCategoryFilter(categories)

    selectPayTab()

    //ru.barabo.babloz.gui.MainView.selectedTab(PayList)
}