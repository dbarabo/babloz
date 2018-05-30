package ru.barabo.babloz.gui.budget

import javafx.beans.binding.BooleanBinding
import ru.barabo.babloz.db.entity.Category
import ru.barabo.babloz.gui.binding.BindProperties

class BudgetCategoryBind() : BindProperties<Category> {

    override var editValue: Category? = null

    override fun fromValue(value: Category?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toValue(value: Category) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun copyToProperties(destination: BindProperties<Category>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isEqualsProperties(compare: BindProperties<Category>): BooleanBinding {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}