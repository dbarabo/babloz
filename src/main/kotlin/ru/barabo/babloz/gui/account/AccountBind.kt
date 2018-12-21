package ru.barabo.babloz.gui.account

import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import ru.barabo.babloz.db.entity.Account
import ru.barabo.babloz.db.entity.AccountType
import ru.barabo.babloz.db.entity.Currency
import ru.barabo.babloz.gui.binding.BindProperties

internal class AccountBind : BindProperties<Account> {

    override var editValue: Account? = null

    val nameProperty = SimpleStringProperty()

    val currencyProperty = SimpleObjectProperty<Currency>()

    val accountTypeProperty = SimpleObjectProperty<AccountType>()

    val descriptionProperty = SimpleStringProperty()

    val isUseDebtSubject = SimpleBooleanProperty()

    override fun fromValue(value: Account?) {

        nameProperty.value = value?.name

        currencyProperty.value = value?.currency

        accountTypeProperty.value = value?.type

        descriptionProperty.value = value?.description

        isUseDebtSubject.value = value?.isUseDebt
    }

    override fun toValue(value: Account) {

        value.name = nameProperty.value

        value.currency = currencyProperty.value

        value.type = accountTypeProperty.value

        value.description = descriptionProperty.value

        value.isUseDebt = isUseDebtSubject.value
    }

    override fun copyToProperties(destination: BindProperties<Account>) {

        val destinationAccount = destination as AccountBind

        destinationAccount.nameProperty.value = this.nameProperty.value

        destinationAccount.currencyProperty.value = this.currencyProperty.value

        destinationAccount.accountTypeProperty.value = this.accountTypeProperty.value

        destinationAccount.descriptionProperty.value = this.descriptionProperty.value

        destinationAccount.isUseDebtSubject.value = this.isUseDebtSubject.value
    }

    override fun isEqualsProperties(compare: BindProperties<Account>): BooleanBinding {

        val compareAccount = compare as AccountBind

        return Bindings.and(
            nameProperty.isEqualTo(compareAccount.nameProperty),
            currencyProperty.isEqualTo(compareAccount.currencyProperty)
                    .and(accountTypeProperty.isEqualTo(compareAccount.accountTypeProperty))
                    .and(descriptionProperty.isEqualTo(compareAccount.descriptionProperty))
                    .and(isUseDebtSubject.isEqualTo(compareAccount.isUseDebtSubject)) )
    }
}