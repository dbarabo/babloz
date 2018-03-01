package ru.barabo.babloz.db.entity

import org.slf4j.LoggerFactory
import ru.barabo.babloz.gui.PayEdit
import tornadofx.observable

data class GroupCategory(var category: Category = Category(),
                     private var parent :GroupCategory? = null,
                     val child: MutableList<GroupCategory> = ArrayList<GroupCategory>().observable() ) {

    companion object {
        private val logger = LoggerFactory.getLogger(GroupCategory::class.java)

        val root = GroupCategory()

        private var lastParent = root

        fun rootClear() {
            synchronized(root.child) { root.child.clear() }
        }

        fun addCategory(category: Category): GroupCategory {

            val groupCategory = category.parent
                    ?.let { GroupCategory(category, lastParent) }
                    ?: GroupCategory(category, root).apply { lastParent = this }

            groupCategory.parent?.child?.add(groupCategory)

            return groupCategory
        }

        fun findByCategory(category: Category): GroupCategory? {

            logger.error("findByCategory=$category")

            return root.findByCategory(category)
        }
    }

    private fun findByCategory(findCategory: Category): GroupCategory? {
        if(findCategory.id == category.id) return this

        //logger.error("category=$category")

        for (group in child) {
            val find = group.findByCategory(findCategory)

            if(find != null) {
                //logger.error("find=$find")
                return find
            }
        }

        return null
    }

    val name: String get() = category.name?.let { it } ?: ""

    val type: String get() = category.type.label

    override fun toString(): String = name
}