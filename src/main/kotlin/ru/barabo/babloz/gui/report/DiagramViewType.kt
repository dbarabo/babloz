package ru.barabo.babloz.gui.report

enum class DiagramViewType(val label: String) {

    LINE_CHART("График"),

    BAR_CHART("Гистограмма");

    override fun toString(): String = label
}