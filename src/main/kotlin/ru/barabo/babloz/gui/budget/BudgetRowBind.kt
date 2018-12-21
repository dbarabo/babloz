package ru.barabo.babloz.gui.budget

import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.SimpleStringProperty
import ru.barabo.babloz.db.entity.budget.BudgetRow
import ru.barabo.babloz.gui.binding.BindProperties
import ru.barabo.babloz.gui.formatter.fromFormatToCurrency
import ru.barabo.babloz.gui.formatter.toCurrencyFormat

class BudgetRowBind : BindProperties<BudgetRow> {
    override var editValue: BudgetRow? = null

    val amountProperty = SimpleStringProperty()

    val nameProperty = SimpleStringProperty()

    override fun fromValue(value: BudgetRow?) {
        amountProperty.value = value?.amount.toCurrencyFormat()

        nameProperty.value = value?.name
    }

    override fun toValue(value: BudgetRow) {
        value.name = nameProperty.value

        value.amount = amountProperty.value.fromFormatToCurrency()
    }

    override fun copyToProperties(destination: BindProperties<BudgetRow>) {
        val destBudgetRowBind = destination as BudgetRowBind

        destBudgetRowBind.amountProperty.value = this.amountProperty.value

        destBudgetRowBind.nameProperty.value = this.nameProperty.value
    }

    override fun isEqualsProperties(compare: BindProperties<BudgetRow>): BooleanBinding {
        val compareBudgetRowBind = compare as BudgetRowBind

        return Bindings.and(
                nameProperty.isEqualTo(compareBudgetRowBind.nameProperty),
                amountProperty.isEqualTo(compareBudgetRowBind.amountProperty))
    }
}