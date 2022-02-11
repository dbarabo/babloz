package ru.barabo.babloz.gui.account

import javafx.scene.layout.VBox
import ru.barabo.babloz.db.entity.AccountType
import ru.barabo.babloz.db.entity.Currency
import ru.barabo.babloz.db.service.CurrencyService
import ru.barabo.babloz.gui.formatter.currencyTextFormatter
import ru.barabo.babloz.gui.pay.PaySaver
import tornadofx.*

internal object AccountEdit : VBox() {

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
                field("Расчеты с субъектом") {
                    checkbox(property = AccountSaver.editBind.isUseDebtSubject)
                }
                field("Начальный баланс") {
                    textfield()
                }
                field("Ремарка") {
                    textarea(property = AccountSaver.editBind.descriptionProperty).prefRowCount = 2
                }

                field("%% Ставка основная") {
                    textfield().apply {
                        bind(AccountSaver.editBind.simplePercentProperty)
                        textFormatter = currencyTextFormatter()
                    }
                }
                field("Дата выплаты по основной %% ставке") {
                    datepicker(property = AccountSaver.editBind.simpleDateProperty)
                }
                field("%% Ставка добавочная") {
                    textfield().apply {
                        bind(AccountSaver.editBind.addPercentProperty)
                        textFormatter = currencyTextFormatter()
                    }
                }
                field("Дата начала добавочной %% ставки") {
                    datepicker(property = AccountSaver.editBind.startAddProperty)
                }
                field("Дата окончания добавочной %% ставки") {
                    datepicker(property = AccountSaver.editBind.endAddProperty)
                }
            }
        }
    }
}