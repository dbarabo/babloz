package ru.barabo.babloz.gui.account

import javafx.scene.layout.VBox
import ru.barabo.babloz.db.entity.AccountType
import ru.barabo.babloz.db.entity.Currency
import ru.barabo.babloz.db.service.CurrencyService
import tornadofx.*

object AccountEdit : VBox() {

    init {
        form {
            fieldset {
                field("Наименование") {
                   textfield(property = AccountSaver.editBind.nameProperty)
                }
                field("Тип счета") {
                    combobox<AccountType>(property = AccountSaver.editBind.accountTypeProperty, values = AccountType.values().toList())
                }
                field("Валюта") {
                    combobox<Currency>(property = AccountSaver.editBind.currencyProperty, values = CurrencyService.currencyList())
                }
                field("Начальный баланс") {
                    textfield()
                }
                field("Ремарка") {
                    textarea(property = AccountSaver.editBind.descriptionProperty).prefRowCount = 2
                }
            }
        }
    }
}