package ru.barabo.babloz.db.service.report.categoryturn

enum class CategoryView(val label: String) {

    ALL("Все категории"),

    PARENT_ONLY("Только Корневые"),

    CHILD_ONLY("Только подкатегории");

    override fun toString(): String = label
}