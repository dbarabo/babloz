package ru.barabo.babloz.db.entity

import tornadofx.observable

data class GroupCategory(var category: Category = Category(),
                     private var parent :GroupCategory? = null,
                     val child: MutableList<GroupCategory> = ArrayList<GroupCategory>().observable() ) {

    companion object {
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
    }

    val name: String get() = category.name?.let { it } ?: ""

    val type: String get() = category.type.label
}