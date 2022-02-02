package ru.barabo.babloz.db.entity.group

import ru.barabo.babloz.db.entity.Category
import tornadofx.asObservable
import java.text.DecimalFormat

data class GroupCategory(var category: Category = Category(),
                         private var parent: GroupCategory? = null,
                         val child: MutableList<GroupCategory> = ArrayList<GroupCategory>().asObservable()
) {

    companion object {
        val root = GroupCategory()

        private var lastParent = root

        var TRANSFER_CATEGORY: GroupCategory? = null

        fun rootClear() {
            synchronized(root.child) {
                root.child.clear()

                TRANSFER_CATEGORY = TRANSFER_CATEGORY ?:GroupCategory(Category.TRANSFER_CATEGORY, root)

                root.child.add(TRANSFER_CATEGORY!!)
            }
            lastParent = root
        }

        fun countRoot() = root.child.size

        fun addCategory(category: Category, rootCategory: GroupCategory = root, lastGroupGet: GroupCategory = lastParent,
                        lastGroupSet: (GroupCategory)->Unit = {lastParent = it}): GroupCategory {

            val groupCategory = category.parent
                    ?.let { addCategoryByParent(category, it, lastGroupGet, rootCategory) }
                    ?: GroupCategory(category, rootCategory).apply { lastGroupSet(this) }

            groupCategory.parent?.child?.add(groupCategory)

            return groupCategory
        }

        fun findByCategory(category: Category): GroupCategory? {

            return root.findByCategory(category)
        }

        private fun addCategoryByParent(category: Category, parentId: Int,
                                        lastGroupGet: GroupCategory, rootCategory: GroupCategory) =

            if(parentId == lastGroupGet.category.id) GroupCategory(category, lastGroupGet)
            else GroupCategory(category, rootCategory)
    }

    private fun findByCategory(findCategory: Category): GroupCategory? {

        if(findCategory.id == category.id) return this

        for (group in child) {

            return group.findByCategory(findCategory)?:continue
        }
        return null
    }

    fun selectedCategoryList(): List<Category> = selectedCategoryList(ArrayList())

    private fun selectedCategoryList(fillList: MutableList<Category>): List<Category> {

        if(category.isSelect) fillList += category

        child.forEach { it.selectedCategoryList(fillList) }

        return fillList
    }

    val name: String get() = category.name ?: ""

    val type: String get() = category.type.label

    val turn: String get() = category.turn?.let { DecimalFormat("0.00").format(it) }?:""

    override fun toString(): String = name

    override fun equals(other: Any?): Boolean {

        if(this === other) return true

        if(other === null || other !is GroupCategory) return false

        return (this.category == other.category)
    }

    override fun hashCode(): Int = category.hashCode()
}