package ru.barabo.babloz.gui.category

import ru.barabo.babloz.db.entity.Category
import ru.barabo.babloz.db.service.CategoryService
import ru.barabo.babloz.gui.binding.AbstractSaver

internal object CategorySaver : AbstractSaver<Category, CategoryBind>(CategoryBind::class.java) {

    override fun serviceSave(value: Category) {
        CategoryService.save(value)
    }
}