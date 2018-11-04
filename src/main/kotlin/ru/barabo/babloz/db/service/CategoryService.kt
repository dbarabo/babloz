package ru.barabo.babloz.db.service

import ru.barabo.babloz.db.BablozOrm
import ru.barabo.babloz.db.entity.Category
import ru.barabo.babloz.db.entity.group.GroupCategory
import ru.barabo.db.service.StoreService

object CategoryService: StoreService<Category, GroupCategory>(BablozOrm, Category::class.java) {

    override fun elemRoot(): GroupCategory = GroupCategory.root

    override fun beforeRead() {
        GroupCategory.rootClear()
    }

    override fun processInsert(item: Category) {

        GroupCategory.addCategory(item)
    }

    fun categoryRoot() = elemRoot()

    fun parentList() :List<Category> {

        val list = ArrayList<Category>()

        list.add(CATEGORY_NONE)

        list.addAll( GroupCategory.root.child.map { it.category } )

        return list
    }

    private val CATEGORY_NONE = Category(name = "НЕТ")

    val ALL_CATEGORY = Category(name = "ВСЕ Категории")

    fun findCategoryById(id :Int?): Category {

        val groupCategory = id?.let{ GroupCategory.root.child.firstOrNull { it.category.id == id } }

        return groupCategory?.category ?: CATEGORY_NONE
    }

    fun findByName(nameCategory :String) :Category? = dataList.firstOrNull { nameCategory.equals(it.name, true)}

    fun categoryAllList(): List<Category> {
        val result = ArrayList<Category>()

        result += ALL_CATEGORY

        result.addAll(dataList)

        return result
    }

    fun categoryList() = dataList

}