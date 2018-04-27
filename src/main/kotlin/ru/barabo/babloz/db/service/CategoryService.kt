package ru.barabo.babloz.db.service

import ru.barabo.babloz.db.BablozOrm
import ru.barabo.babloz.db.entity.Category
import ru.barabo.babloz.db.entity.group.GroupCategory
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

    fun categoryRoot() = elemRoot()

    fun parentList() :List<Category> {

        val list = ArrayList<Category>()

        list.add(Category(name = "НЕТ"))

        list.addAll( GroupCategory.root.child.map { it.category } )

        return list
    }

    fun findCategoryById(id :Int?) :Category {

        val groupCategory = id?.let{ GroupCategory.root.child.firstOrNull { it.category.id == id } }

        return groupCategory?.category ?: Category(name = "НЕТ")
    }
}