package ru.barabo.babloz.gui.pay.filter

import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class DateSelect(val label: String, val start: ()->LocalDate, val end: ()->LocalDate = start) {
    ALL_PERIOD("ВЕСЬ Период",  {LocalDate.MIN}),
    TODAY("За сегодня", {LocalDate.now()}),
    YESTERDAY("За вчера", {LocalDate.now().minusDays(1)}),
    MONTH_NOW("С начала месяца", {LocalDate.now().withDayOfMonth(1) }, {LocalDate.now()} ),
    MONTH_PRIOR("Прошлый месяц", {LocalDate.now().minusMonths(1).withDayOfMonth(1)},
            { LocalDate.now().withDayOfMonth(1).minusDays(1)}),
    YEAR_NOW("С начала года", { LocalDate.now().withDayOfYear(1) }, {LocalDate.now()}),
    YEAR("За год", {LocalDate.now().minusMonths(12) },  {LocalDate.now()}),
    DATE_PERIOD("Задать период...", {startDate}, {endDate} );

    companion object {
        var startDate: LocalDate = LocalDate.MIN

        var endDate: LocalDate = LocalDate.MIN
    }

    override fun toString(): String {
        if(this !== DATE_PERIOD) return label

        if(startDate == LocalDate.MIN && endDate == LocalDate.MIN) return label

        return "Период:${startDate.shortFormat()}-${endDate.shortFormat()}"
    }
}

private fun LocalDate.shortFormat() = DateTimeFormatter.ofPattern("dd.MM.yy").format(this)