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
import ru.barabo.babloz.gui.formatter.fromFormatToCurrency
import ru.barabo.babloz.gui.formatter.toCurrencyFormat
import java.time.LocalDate
import java.time.LocalDateTime

internal class AccountBind : BindProperties<Account> {

    override var editValue: Account? = null

    val nameProperty = SimpleStringProperty()

    val currencyProperty = SimpleObjectProperty<Currency>()

    val accountTypeProperty = SimpleObjectProperty<AccountType>()

    val descriptionProperty = SimpleStringProperty()

    val isUseDebtSubject = SimpleBooleanProperty()

    val simplePercentProperty = SimpleStringProperty()
    val addPercentProperty = SimpleStringProperty()
    val simpleDateProperty = SimpleObjectProperty<LocalDate>()
    val startAddProperty = SimpleObjectProperty<LocalDate>()
    val endAddProperty = SimpleObjectProperty<LocalDate>()

    override fun fromValue(value: Account?) {

        nameProperty.value = value?.name

        currencyProperty.value = value?.currency

        accountTypeProperty.value = value?.type

        descriptionProperty.value = value?.description

        isUseDebtSubject.value = value?.isUseDebt

        simplePercentProperty.value =  value?.simplePercent.toCurrencyFormat()
        addPercentProperty.value =  value?.addPercent.toCurrencyFormat()
        simpleDateProperty.value = value?.daySimplePercent
        startAddProperty.value = value?.startAdd
        endAddProperty.value = value?.endAdd
    }

    override fun toValue(value: Account) {

        value.name = nameProperty.value

        value.currency = currencyProperty.value

        value.type = accountTypeProperty.value

        value.description = descriptionProperty.value

        value.isUseDebt = isUseDebtSubject.value

        value.simplePercent = simplePercentProperty.value?.fromFormatToCurrency()
        value.addPercent = addPercentProperty.value?.fromFormatToCurrency()
        value.daySimplePercent = simpleDateProperty.value
        value.startAdd = startAddProperty.value
        value.endAdd = endAddProperty.value
    }

    override fun copyToProperties(destination: BindProperties<Account>) {

        val destinationAccount = destination as AccountBind

        destinationAccount.nameProperty.value = this.nameProperty.value

        destinationAccount.currencyProperty.value = this.currencyProperty.value

        destinationAccount.accountTypeProperty.value = this.accountTypeProperty.value

        destinationAccount.descriptionProperty.value = this.descriptionProperty.value

        destinationAccount.isUseDebtSubject.value = this.isUseDebtSubject.value

        destinationAccount.addPercentProperty.value = destinationAccount.addPercentProperty.value
        destinationAccount.simplePercentProperty.value = destinationAccount.simplePercentProperty.value
        destinationAccount.simpleDateProperty.value = destinationAccount.simpleDateProperty.value
        destinationAccount.startAddProperty.value = destinationAccount.startAddProperty.value
        destinationAccount.endAddProperty.value = destinationAccount.endAddProperty.value
    }

    override fun isEqualsProperties(compare: BindProperties<Account>): BooleanBinding {

        val compareAccount = compare as AccountBind

        return Bindings.and(
            nameProperty.isEqualTo(compareAccount.nameProperty),
            currencyProperty.isEqualTo(compareAccount.currencyProperty)

                .and(accountTypeProperty.isEqualTo(compareAccount.accountTypeProperty))
                .and(descriptionProperty.isEqualTo(compareAccount.descriptionProperty))
                .and(isUseDebtSubject.isEqualTo(compareAccount.isUseDebtSubject))

                .and(addPercentProperty.isEqualTo(compareAccount.addPercentProperty))
                .and(simplePercentProperty.isEqualTo(compareAccount.simplePercentProperty))
                .and(simpleDateProperty.isEqualTo(compareAccount.simpleDateProperty))
                .and(startAddProperty.isEqualTo(compareAccount.startAddProperty))
                .and(endAddProperty.isEqualTo(compareAccount.endAddProperty))
        )
    }
}
