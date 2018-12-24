package ru.barabo.babloz.db.service.report

import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class PeriodType(val label: String,
                      val convertDate: (LocalDate)-> LocalDate,
                      val nextDate: (LocalDate)-> LocalDate,
                      val format: (LocalDate)-> String) {

    MONTH("месяц", { it.withDayOfMonth(1) }, { it.plusMonths(1) }, { it.formatMonth() } ),
    DAY("день", { it }, { it.plusDays(1) }, { it.formatDay() } ),
    WEEK("неделя", { it.with(java.time.DayOfWeek.MONDAY) }, { it.plusWeeks(1) }, { it.formatDay() } ),
    WEEK2("2 недели", { it.with(java.time.DayOfWeek.MONDAY) }, { it.plusWeeks(2) }, { it.formatDay() } ),
    MONTH2("2 месяца", { it.withDayOfMonth(1) }, { it.plusMonths(2) }, { it.formatMonth() } ),
    QUARTER("квартал", { it.firstDayOfQuarter() }, { it.plusMonths(3) }, { it.formatMonth() } ),
    HALF_YEAR("полугодие", { it.firstDayOfHalfYear() }, { it.plusMonths(6) }, { it.formatMonth() } ),
    YEAR("год", { it.withDayOfYear(1) }, { it.plusYears(1) }, { it.formatYear() } );

    override fun toString(): String = label
}

fun LocalDate.formatMonth() = DateTimeFormatter.ofPattern("yy.MM").format(this)!!

fun LocalDate.formatYear() = DateTimeFormatter.ofPattern("yyyy").format(this)!!

fun LocalDate.formatDay() = DateTimeFormatter.ofPattern("dd.MM.yy").format(this)!!

fun LocalDate.firstDayOfQuarter() = withDayOfYear(1)
        .plusMonths(3L * (get(java.time.temporal.IsoFields.QUARTER_OF_YEAR) - 1) )!!

fun LocalDate.firstDayOfHalfYear() = withDayOfYear(1)
        .plusMonths(6L * ((get(java.time.temporal.IsoFields.QUARTER_OF_YEAR) - 1)/2) )!!

