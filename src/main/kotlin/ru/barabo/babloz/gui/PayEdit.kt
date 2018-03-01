package ru.barabo.babloz.gui

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.*
import javafx.scene.layout.VBox
import org.slf4j.LoggerFactory
import ru.barabo.babloz.db.entity.*
import ru.barabo.babloz.db.service.AccountService
import ru.barabo.babloz.db.service.CategoryService
import ru.barabo.babloz.db.service.PayService
import ru.barabo.babloz.main.ResourcesManager
import tornadofx.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

object PayEdit: Tab("Правка платежа", VBox()) {

    private val logger = LoggerFactory.getLogger(PayEdit::class.java)

    private val accountProperty = SimpleObjectProperty<Account>()

    private val dateProperty = SimpleObjectProperty<LocalDate>()

    private val amountProperty =  SimpleObjectProperty<BigDecimal>()

    private val descriptionProperty = SimpleStringProperty()

    private lateinit var editPay : Pay

    private var selectCategory: GroupCategory? = null

    init {

        this.graphic = ResourcesManager.icon("edit.png")

        form {
            toolbar {
                button ("Сохранить", ResourcesManager.icon("save.png")).setOnAction { save() }

                button ("Отменить", ResourcesManager.icon("cancel.png")).setOnAction { cancel() }
            }

            fieldset {
                field("Счет") {
                    combobox<Account>(property = accountProperty, values = AccountService.accountList())
                }
                field("Дата") {
                    datepicker(property = dateProperty)
                }
                field("Категория") {

                    treeview(TreeItem(CategoryService.categoryRoot())).apply {
                        populate { it.value.child }

                        root.isExpanded = true

                        this.isShowRoot = false

                        val selectedItem = selectCategory?.let { root.findTreeItem(selectCategory) }

                        selectedItem?.apply {
                            selectionModel?.select(this)
                        }

                        selectionModel?.selectedItemProperty()?.addListener(
                                { _, _, newSelection ->
                                    selectCategory = newSelection?.value
                                })
                    }.prefHeight = 100.0
                }
                field("Сумма") {
                    textfield().apply {
                        bind(amountProperty)
                    }
                 }
                field("Ремарка") {
                    textarea(descriptionProperty).prefRowCount = 3
                }
            }
        }
    }

    private fun <T> TreeItem<T>.findTreeItem(childItem: T): TreeItem<T>? {

        if(this.value === childItem) return this

        for (child in children) {
            val find = child.findTreeItem(childItem)

            if(find != null) {
                return find
            }
        }
        return null
    }

    private fun cancel() {
        tabPane.tabs.remove(PayEdit)
    }

    private const val ALERT_ERROR_SAVE = "Ошибка при сохранении"

    private fun save() {

        editPay = setPayFromProperty(editPay)

        try {
            editPay = PayService.save(editPay)

            tabPane.tabs.remove(AccountEdit)
        } catch (e :Exception) {
            logger.error("save", e)

            alert(Alert.AlertType.ERROR, e.message ?: ALERT_ERROR_SAVE)
        }
    }

    private fun setPayFromProperty(pay: Pay): Pay {

        pay.account = accountProperty.value

        pay.created = if(dateProperty.value == LocalDate.now()) LocalDateTime.now() else dateProperty.value.atStartOfDay()

        pay.category = selectCategory?.category

        pay.amount = amountProperty.value

        pay.description = descriptionProperty.value

        return pay
    }

    fun editPay(pay : Pay) {

        editPay = pay

        tabPane.selectionModel.select(PayEdit)

        this.text = pay.id ?. let { "Правка платежа" } ?: "Новый платеж"

        accountProperty.value = pay.account

        dateProperty.value = pay.created?.toLocalDate()?:LocalDate.now()

        selectCategory = pay.category?.let { GroupCategory.findByCategory(it) }

        logger.error("editPay selectCategory=$selectCategory")

        amountProperty.value =  pay.amount

        descriptionProperty.value = pay.description
    }
}