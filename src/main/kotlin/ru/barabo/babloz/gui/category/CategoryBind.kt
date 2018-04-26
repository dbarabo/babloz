package ru.barabo.babloz.gui.category

import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import ru.barabo.babloz.db.entity.Category
import ru.barabo.babloz.db.entity.CategoryType
import ru.barabo.babloz.db.service.CategoryService
import ru.barabo.babloz.gui.binding.BindProperties

internal class CategoryBind : BindProperties<Category> {

    val nameProperty = SimpleStringProperty()

    val parentCategoryProperty = SimpleObjectProperty<Category>()

    val typeProperty = SimpleObjectProperty<CategoryType>()

    override var editValue: Category? = null

    override fun fromValue(value: Category?) {
        nameProperty.value = value?.name

        parentCategoryProperty.value = CategoryService.findCategoryById(value?.parent)

        typeProperty.value = value?.type
    }

    override fun toValue(value: Category) {
        value.name = nameProperty.value

        value.parent = parentCategoryProperty.value?.id

        value.type = typeProperty.value
    }

    override fun copyToProperties(destination: BindProperties<Category>) {
        val destinationCategory = destination as CategoryBind

        destinationCategory.nameProperty.value = nameProperty.value

        destinationCategory.parentCategoryProperty.value = parentCategoryProperty.value

        destinationCategory.typeProperty.value = typeProperty.value
    }

    override fun isEqualsProperties(compare: BindProperties<Category>): BooleanBinding {

        val compareCategory = compare as CategoryBind

        return Bindings.and(
                nameProperty.isEqualTo(compareCategory.nameProperty),
                parentCategoryProperty.isEqualTo(compareCategory.parentCategoryProperty)
                        .and(typeProperty.isEqualTo(compareCategory.typeProperty)) )
     }
}