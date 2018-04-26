package ru.barabo.babloz.gui.category

import javafx.scene.layout.VBox
import ru.barabo.babloz.db.entity.Category
import ru.barabo.babloz.db.entity.CategoryType
import ru.barabo.babloz.db.service.CategoryService
import tornadofx.*

internal object CategoryEdit : VBox() {
    init {
        form {
            fieldset {
                field("Наименование") {
                    textfield(property = CategorySaver.editBind.nameProperty)
                }
                field("Тип категории") {
                    combobox<CategoryType>(property = CategorySaver.editBind.typeProperty, values = CategoryType.values().toList())
                }
                field("Родительская категория") {
                    combobox<Category>(property = CategorySaver.editBind.parentCategoryProperty, values = CategoryService.parentList())
                }
            }
        }
    }
}