package ru.barabo.babloz.db.service

import ru.barabo.babloz.db.BablozOrm
import ru.barabo.babloz.db.entity.Category
import ru.barabo.babloz.db.entity.GroupCategory
import ru.barabo.db.service.StoreService

object CategoryService: StoreService<Category, GroupCategory>(BablozOrm) {
    override fun elemRoot(): GroupCategory = GroupCategory.root

    override fun clazz(): Class<Category> = Category::class.java

    override fun beforeRead() {
        GroupCategory.rootClear()
    }

    override fun processInsert(item: Category) {

        GroupCategory.addCategory(item)
    }
}