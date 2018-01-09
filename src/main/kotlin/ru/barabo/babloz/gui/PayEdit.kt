package ru.barabo.babloz.gui

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Alert
import javafx.scene.control.Tab
import javafx.scene.layout.VBox
import org.slf4j.LoggerFactory
import ru.barabo.babloz.db.entity.Account
import ru.barabo.babloz.db.entity.AccountType
import ru.barabo.babloz.db.entity.Currency
import ru.barabo.babloz.db.entity.Pay
import ru.barabo.babloz.db.service.AccountService
import ru.barabo.babloz.db.service.CurrencyService
import ru.barabo.babloz.main.ResourcesManager
import tornadofx.*

object PayEdit: Tab("Правка платежа", VBox()) {

    private val logger = LoggerFactory.getLogger(PayEdit::class.java)

    private val nameProperty = SimpleStringProperty()

    private val currencyProperty = SimpleObjectProperty<Currency>()

    private val accountTypeProperty = SimpleObjectProperty<AccountType>()

    private lateinit var editPay : Pay

    init {
        this.graphic = ResourcesManager.icon("edit.png")

        form {
            toolbar {
                button ("Сохранить", ResourcesManager.icon("save.png")).setOnAction { save() }

                button ("Отменить", ResourcesManager.icon("cancel.png")).setOnAction { cancel() }
            }

            fieldset {
                field("Наименование") {
                    textfield(nameProperty)
                }
                field("Тип счета") {
                    combobox<AccountType>(property = accountTypeProperty, values = AccountType.values().toList())
                }
                field("Валюта") {
                    combobox<Currency>(property = currencyProperty, values = CurrencyService.currencyList())
                }
                field("Начальный баланс") {
                    textfield()
                }
                field("Ремарка") {
                    textarea().prefRowCount = 2
                }
            }
        }
    }

    private fun cancel() {
        tabPane.tabs.remove(AccountEdit)
    }

    private val ALERT_ERROR_SAVE = "Ошибка при сохранении"

    private fun save() {

        //editAccount = setAccountFromProperty(editAccount)

        try {
            //editAccount = AccountService.save(editAccount)

            tabPane.tabs.remove(AccountEdit)
        } catch (e :Exception) {
            logger.error("save", e)

            alert(Alert.AlertType.ERROR, e.message ?: ALERT_ERROR_SAVE)
        }
    }

    private fun setAccountFromProperty(account : Account) : Account {

        account.name = nameProperty.value

        account.currency = currencyProperty.value

        account.type = accountTypeProperty.value

        return account
    }

    fun editAccount(pay : Pay) {

        editPay = pay

        tabPane.selectionModel.select(AccountEdit)

        this.text = pay.id ?. let { "Правка платежа" } ?: "Новый платеж"

        //nameProperty.value = pay.name ?. let { account.name } ?: ""

        //currencyProperty.value = pay.currency ?. let { account.currency } ?: CurrencyService.defaultCurrency()

        //accountTypeProperty.value = pay.type ?. let { account.type } ?: AccountType.CURRENT
    }
}